package me.battledash.kyber.network.state;

import lombok.Data;
import me.battledash.kyber.engine.Tickable;
import me.battledash.kyber.network.ServerConnection;
import me.battledash.kyber.server.ServerGameContext;

@Data
public class ServerFSM implements Tickable {

    private final ServerConnection connection;

    private ServerConnectionState currentState;

    private float deltaTime;
    private float currentTime;
    private float totalTimeInState;

    public void tick() {
        float seconds = ServerGameContext.context().getServer().getSecondsPerTick();
        this.totalTimeInState += seconds;
        this.currentTime -= seconds;
        if (this.currentState != null && this.currentTime <= 0.f) {
            this.currentTime = this.deltaTime;
            this.currentState.update(this.connection, seconds, this.totalTimeInState);
        }
    }

    public void setState(ServerConnectionState state, float deltaTime) {
        this.currentTime = 0;
        this.deltaTime = deltaTime;

        if (this.currentState != null) {
            this.currentState.leave();
        }
        this.totalTimeInState = 0;
        this.currentState = state;

        if (state != null) {
            state.setStateMachine(this);
            state.enter();
        }
    }

    public void setState(ServerConnectionState state) {
        this.setState(state, 0.f);
    }

}
