package me.battledash.kyber.types.network;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.types.network.messages.NetworkCreatePlayerMessage;
import me.battledash.kyber.types.network.messages.NetworkFirstPlayerEnteredMessage;
import me.battledash.kyber.types.network.messages.NetworkLevelLoadedAckMessage;
import me.battledash.kyber.types.network.messages.NetworkLoadLevelMessage;
import me.battledash.kyber.types.network.messages.NetworkOnPlayerSpawnedMessage;
import me.battledash.kyber.types.network.messages.NetworkSelectTeamMessage;
import me.battledash.kyber.types.network.messages.NetworkSettingsMessage;
import me.battledash.kyber.types.network.messages.NetworkTimeSyncMessage;
import me.battledash.kyber.types.network.messages.NetworkTinyEventMessage;
import me.battledash.kyber.types.network.messages.OnlineNetworkPlayerGameGroupChangedMessage;
import me.battledash.kyber.types.network.messages.SpikeInternalMessagePartMessage;
import me.battledash.kyber.types.network.messages.StatImmutableMessage;
import me.battledash.kyber.types.network.messages.SubLevelFromClientRequestBundleBaselineMessage;
import me.battledash.kyber.types.network.messages.SubLevelToClientDropBundleBaselineMessage;
import me.battledash.kyber.types.network.messages.SubLevelToClientLoadRequestsMessage;
import me.battledash.kyber.types.network.messages.SubLevelToClientSubLevelNameMessage;
import me.battledash.kyber.types.network.messages.WSNetworkKitLimitationLockKitsResultMessage;
import me.battledash.kyber.types.network.messages.WSNetworkServerInfoMessage;
import me.battledash.kyber.types.network.messages.internal.NetworkLogicFireEventMessage;
import me.battledash.kyber.types.network.messages.internal.NetworkLogicFirePlayerEventMessage;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

@Slf4j
public class NetworkableMessageFactory {

    private static final List<MessageWrapper> MESSAGES = new ArrayList<>();

    public static void register(int id, Class<? extends NetworkableMessage> clazz) {
        Constructor<? extends NetworkableMessage> constructor = null;
        try {
            constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            log.error("Failed to create networkable message {}, please ensure there is a default constructor",
                    clazz.getSimpleName(), e);
        }
        Constructor<? extends NetworkableMessage> finalConstructor = constructor;
        Supplier<NetworkableMessage> supplier = () -> {
            try {
                return finalConstructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("Failed to create networkable message {}, please ensure there is a default constructor",
                        clazz.getSimpleName(), e);
                return null;
            }
        };
        NetworkableMessageFactory.MESSAGES.add(new MessageWrapper(id, clazz, supplier));
    }

    @SuppressWarnings("unchecked")
    public static <T extends NetworkableMessage> T createNetworkableMessage(int id) {
        MessageWrapper messageWrapper = NetworkableMessageFactory.MESSAGES.stream()
                .filter(m -> m.getId() == id)
                .findFirst()
                .orElse(null);
        return messageWrapper == null ? null : (T) messageWrapper.getSupplier().get();
    }

    public static int getNetworkableMessageId(Class<? extends NetworkableMessage> clazz) {
        return NetworkableMessageFactory.MESSAGES.stream().filter(m -> m.getClazz().equals(clazz)).findFirst().orElseThrow().getId();
    }

    public static boolean networkableMessageExists(int id) {
        return NetworkableMessageFactory.MESSAGES.stream().anyMatch(m -> m.getId() == id);
    }

    public static void register() {
        // All networkable messages: https://paste.battleda.sh/natihehoci
        NetworkableMessageFactory.register(15, NetworkSelectTeamMessage.class);
        NetworkableMessageFactory.register(26, NetworkSettingsMessage.class);
        NetworkableMessageFactory.register(35, WSNetworkKitLimitationLockKitsResultMessage.class);
        NetworkableMessageFactory.register(36, NetworkFirstPlayerEnteredMessage.class);
        NetworkableMessageFactory.register(38, SubLevelToClientSubLevelNameMessage.class);
        NetworkableMessageFactory.register(49, NetworkOnPlayerSpawnedMessage.class);
        NetworkableMessageFactory.register(52, NetworkCreatePlayerMessage.class);
        NetworkableMessageFactory.register(70, WSNetworkServerInfoMessage.class);
        NetworkableMessageFactory.register(77, OnlineNetworkPlayerGameGroupChangedMessage.class);
        NetworkableMessageFactory.register(85, NetworkLevelLoadedAckMessage.class);
        NetworkableMessageFactory.register(92, SubLevelToClientDropBundleBaselineMessage.class);
        NetworkableMessageFactory.register(106, StatImmutableMessage.class);
        NetworkableMessageFactory.register(119, SubLevelToClientLoadRequestsMessage.class);
        NetworkableMessageFactory.register(129, SubLevelFromClientRequestBundleBaselineMessage.class);
        NetworkableMessageFactory.register(138, SpikeInternalMessagePartMessage.class);
        NetworkableMessageFactory.register(142, NetworkTimeSyncMessage.class);
        NetworkableMessageFactory.register(149, NetworkLoadLevelMessage.class);
        NetworkableMessageFactory.register(150, NetworkLogicFireEventMessage.class);
        NetworkableMessageFactory.register(157, NetworkTinyEventMessage.class);
        NetworkableMessageFactory.register(163, NetworkLogicFirePlayerEventMessage.class);
    }

    @Data
    private static class MessageWrapper {
        private final int id;
        private final Class<? extends NetworkableMessage> clazz;
        private final Supplier<NetworkableMessage> supplier;
    }

}
