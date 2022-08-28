package me.battledash.kyber.engine.messages;

public interface Cancellable {

    boolean isCancelled();

    void setCancelled(boolean cancelled);

}
