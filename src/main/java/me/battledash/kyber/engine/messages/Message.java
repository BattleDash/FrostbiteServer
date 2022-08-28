package me.battledash.kyber.engine.messages;

import me.battledash.kyber.server.ServerGameContext;

public class Message {

    /**
     * Executes this message using the game context's message manager.
     *
     * @return true if the message was executed successfully,
     *         false if the message was cancelled.
     */
    public boolean callMessage() {
        ServerGameContext.context().getMessageManager().executeMessage(this);
        if (this instanceof Cancellable cancellable) {
            return !cancellable.isCancelled();
        }
        return true;
    }

}
