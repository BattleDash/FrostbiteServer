package me.battledash.kyber.network.stream;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.misc.WeakPtr;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamRollback;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StreamManagerEngine extends StreamManager {

    private final List<StreamManager> streamManagers = new ArrayList<>();

    @Getter
    private final String name = "Engine";

    private TransmitResult calculateTotalResult(TransmitResult currentResult, TransmitResult addedResult) {
        if (addedResult == TransmitResult.INCOMPLTE && currentResult == TransmitResult.WHOLE)
            currentResult = TransmitResult.INCOMPLTE;

        if (addedResult == TransmitResult.INCOMPLETE_CONTINUE)
            currentResult = TransmitResult.INCOMPLETE_CONTINUE;

        return currentResult;
    }

    private TransmitResult sendManager(BitStreamWrite stream, StreamManager manager, int index, TransmitResult totalResult, WeakPtr<Integer> managerSent) {
        TransmitResult result = TransmitResult.NOTHING_TO_SEND;
        BitStreamRollback rollbackDebugMarker = new BitStreamRollback(stream);

        int bitsWritten = 0;
        if (!stream.isOverflown()) {
            int tempPos = stream.tell();
            result = manager.transmitPacket(stream);
            bitsWritten = stream.tell() - tempPos;

            if (result == TransmitResult.NOTHING_TO_SEND && bitsWritten > 0) {
                log.warn("Manager {} returned NOTHING_TO_SEND, but wrote {} bits", manager.getName(), bitsWritten);
            }

            if (result == TransmitResult.WHOLE && manager.isDebugBroadcastProcessing()) {
                log.info("Manager {} successfully transmitted", manager.getName());
            }

            managerSent.set(managerSent.get() | MathUtil.selGtz(bitsWritten, 1 << index, 0));
        }

        if (result == TransmitResult.NOTHING_TO_SEND || stream.isOverflown()) {
            rollbackDebugMarker.rollback();
        } else {
            if (bitsWritten == 0) {
                rollbackDebugMarker.rollback();
            }

            totalResult = calculateTotalResult(totalResult, result);
        }

        return totalResult;
    }

    private boolean receiveManager(BitStreamRead stream, StreamManager manager, long index, long managerReceived) {
        boolean ok = true;
        if (((managerReceived >> index) & 1) > 0) {
            if (manager.isDebugBroadcastProcessing()) {
                log.info("Processing packet through manager {}", manager.getName());
            }
            ok = manager.processReceivedPacket(stream);
        } else {
            log.debug("Skipping packet through manager {} ({})", manager.getName(), (managerReceived >> index) & 1);
        }
        return ok;
    }

    @Override
    public TransmitResult transmitPacket(BitStreamWrite stream) {
        int managerCount = this.streamManagers.size();
        int managerSent = 0;

        int startPos = stream.tell();
        stream.writeUnsigned(managerSent, managerCount);

        TransmitResult totalResult = TransmitResult.WHOLE;

        for (int i = 0; i < this.streamManagers.size(); i++) {
            WeakPtr<Integer> weakPtr = new WeakPtr<>(managerSent);
            totalResult = this.sendManager(stream, this.streamManagers.get(i), i, totalResult, weakPtr);
            managerSent = weakPtr.get();
        }

        int endPos = stream.tell();
        stream.seek(startPos);
        stream.writeUnsigned(managerSent, managerCount);
        stream.seek(endPos);

        return totalResult;
    }

    @Override
    public boolean processReceivedPacket(BitStreamRead stream) {
        final long managerCount = this.streamManagers.size(); // Should be dynamic with the number of managers
        long managerReceived = stream.readUnsigned(managerCount);

        log.debug("Received data packet for manager {} ({})", managerReceived, managerCount);
        long index = 0;
        for (StreamManager manager : this.streamManagers) {
            if (!this.receiveManager(stream, manager, index, managerReceived)) {
                return false;
            }
            index++;
        }
        return true;
    }

    public void addManager(StreamManager manager) {
        this.streamManagers.add(manager);
    }

}
