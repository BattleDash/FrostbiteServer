package me.battledash.kyber.engine.player.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.battledash.kyber.engine.messages.Message;
import me.battledash.kyber.engine.player.ServerPlayer;
import me.battledash.kyber.network.ServerConnection;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class PlayerCreatedForConnectionMessage extends Message {

    private final ServerConnection connection;
    private final ServerPlayer player;

}
