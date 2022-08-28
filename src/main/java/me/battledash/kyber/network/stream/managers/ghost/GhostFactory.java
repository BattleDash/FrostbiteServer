package me.battledash.kyber.network.stream.managers.ghost;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.network.ClientSubLevelGhost;
import me.battledash.kyber.engine.entity.network.ServerSubLevelGhost;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.server.GameContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class GhostFactory {

    private static final List<MessageWrapper> TYPES = new ArrayList<>();

    public static void register(int id, Class<? extends Ghost> clazz, Function<GhostCreationInfo, Ghost> supplier) {
        GhostFactory.TYPES.add(new MessageWrapper(id, clazz, supplier));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Ghost> T createGhost(int id, GhostCreationInfo info) {
        return (T) GhostFactory.TYPES.stream()
                .filter(m -> m.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No ghost type with id " + id))
                .getSupplier()
                .apply(info);
    }

    public static int getGhostTypeId(Class<? extends Ghost> clazz) {
        return GhostFactory.TYPES.stream().filter(m -> m.getClazz().equals(clazz)).findFirst().orElseThrow().getId();
    }

    public static boolean ghostTypeExists(int id) {
        return GhostFactory.TYPES.stream().anyMatch(m -> m.getId() == id);
    }

    public static void register() {
        // All ghost types: https://paste.battleda.sh/kofajijute
        if (GameContext.SIMULATE_CLIENT) {
            GhostFactory.register(12, ClientSubLevelGhost.class, ClientSubLevelGhost::createClientSubLevelGhost);
        } else {
            GhostFactory.register(12, ServerSubLevelGhost.class, null);
        }
    }

    @Data
    private static class MessageWrapper {
        private final int id;
        private final Class<? extends Ghost> clazz;
        private final Function<GhostCreationInfo, Ghost> supplier;
    }

}
