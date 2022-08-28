package me.battledash.kyber.network.state;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.simulation.LevelSetup;
import me.battledash.kyber.types.network.messages.NetworkLoadLevelMessage;
import me.battledash.kyber.network.ServerConnection;
import me.battledash.kyber.network.TinyEvent;

@Data
@Slf4j
@NoArgsConstructor
public abstract class ServerConnectionState {

    private ServerFSM stateMachine;
    private SendStatus sendStatus = SendStatus.DO_NOT_SEND;

    protected void setState(ServerConnectionState state) {
        this.stateMachine.setState(state);
    }

    public abstract void update(ServerConnection connection, float deltaTime, float secondsInState);

    public void enter() {
        log.debug("Entering state {}", this.getClass().getSimpleName());
    }

    public void leave() {}

    public static class IngameState extends ServerConnectionState {

        {
            this.setSendStatus(SendStatus.SEND_ALL);
        }

        @Override
        public void update(ServerConnection connection, float deltaTime, float secondsInState) {
        }

    }

    public static class PreIngameState extends ServerConnectionState {

        @Override
        public void update(ServerConnection connection, float deltaTime, float secondsInState) {
            if (connection.getStateFlowFlags().isAckTimeSyncDone()) {
                // TODO: 5/8/2022 connection.clientLinked
                this.setState(new IngameState());
            }
        }

    }

    public static class LinkLevelState extends ServerConnectionState {

        @Override
        public void update(ServerConnection connection, float deltaTime, float secondsInState) {
            if (connection.getStateFlowFlags().isAckLevelLinked()) {
                connection.getStateFlowFlags().setAckTimeSyncDone(false);
                connection.timeSync(false);
                this.setState(new PreIngameState());
            }
        }

    }

    public static class BeginLinkLevelState extends ServerConnectionState {

        @Override
        public void update(ServerConnection connection, float deltaTime, float secondsInState) {
            connection.sendTinyEvent(TinyEvent.CMD_LINK_LEVEL);
            this.setState(new LinkLevelState());
        }

    }

    public static class LoadingLevelState extends ServerConnectionState {

        @Override
        public void update(ServerConnection connection, float deltaTime, float secondsInState) {
            if (connection.getStateFlowFlags().isAckLevelLoaded()) {
                log.info("Level loaded");
                this.setState(new BeginLinkLevelState());
            }
        }

    }

    public static class SendLoadLevelState extends ServerConnectionState {

        @Override
        public void update(ServerConnection connection, float deltaTime, float secondsInState) {

            NetworkLoadLevelMessage message = new NetworkLoadLevelMessage();

            NetworkLoadLevelMessage.LoadLevelInfo info = message.getInfo();
            info.setLevelSequenceNumber(1);
            LevelSetup setup = info.getSetup();
            setup.setName("S5_1/Levels/MP/Geonosis_01/Geonosis_01");
            setup.setInclusionOption("GameMode", "PlanetaryBattles");

            connection.sendMessage(message);

            log.info("Sending load level message");

            this.setState(new ServerConnectionState.LoadingLevelState());
        }

    }

    public static class TimeSyncState extends ServerConnectionState {

        @Override
        public void update(ServerConnection connection, float deltaTime, float secondsInState) {
            if (connection.getStateFlowFlags().isAckTimeSyncDone()) {
                log.info("Time sync done");
                this.setState(new SendLoadLevelState());
            }
        }

    }

    public static class StartTimeSyncState extends ServerConnectionState {

        @Override
        public void update(ServerConnection connection, float deltaTime, float secondsInState) {
            connection.getStateFlowFlags().setAckTimeSyncDone(false);
            connection.timeSync(true);
            this.setState(new TimeSyncState());
        }

    }

}
