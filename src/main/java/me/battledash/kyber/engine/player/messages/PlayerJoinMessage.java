package me.battledash.kyber.engine.player.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.battledash.kyber.engine.messages.Cancellable;
import me.battledash.kyber.engine.messages.Message;
import me.battledash.kyber.engine.player.ServerPlayer;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class PlayerJoinMessage extends Message implements Cancellable {

    private final ServerPlayer player;

    private boolean cancelled;

}
