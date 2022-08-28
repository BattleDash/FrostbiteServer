package me.battledash.kyber.engine.messages;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MessageManager {

    public static boolean DEBUG = false;

    private final List<Listener> registeredListeners = new ArrayList<>();

    public void registerListener(Listener listener) {
        this.registeredListeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        this.registeredListeners.remove(listener);
    }

    public void dispatchMessage(final Message message) {
        if (MessageManager.DEBUG) {
            log.info("Dispatching message: {}", message);
        }
        for (final Listener listener : this.registeredListeners) {
            listener.onMessage(message);
        }
    }

    public void executeMessage(final Message message) {
        this.dispatchMessage(message);
    }

}
