package me.battledash.kyber.network;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.streams.InBitStream;
import me.battledash.kyber.streams.OutBitStream;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PacketNotification {

    public static final int SIZE = 128;
    public static final int SEQUENCE_NUMBER_BITS = 12;
    public static final int SEQUENCE_NUMBER_MASK = ((1 << PacketNotification.SEQUENCE_NUMBER_BITS) - 1);

    private long lastSequenceNumber = 0;
    private long sendSequenceNumber = 0;
    private long deferredSequenceNumber = 0;
    private long deferredLastReceived = 0;
    private long lastSequenceNumberThatPeerReceived = 0;
    private long lastProcessedSequenceNumber = 0;
    private boolean deferredWaiting;
    private boolean lastReadPacketHeaderWasValid;

    private final PacketNotificationBits packetNotificationBits = new PacketNotificationBits();
    private final PacketNotificationBits deferredNotificationBits = new PacketNotificationBits();
    private final List<PacketDeliveryStatus> packetNotificationQueue = new ArrayList<>();

    public void writeHeader(OutBitStream stream) {
        stream.write(this.sendSequenceNumber & PacketNotification.SEQUENCE_NUMBER_MASK, PacketNotification.SEQUENCE_NUMBER_BITS);
        stream.write(this.deferredSequenceNumber & PacketNotification.SEQUENCE_NUMBER_MASK, PacketNotification.SEQUENCE_NUMBER_BITS);
        this.packetNotificationBits.write(stream);
    }

    public PacketSequenceStatus readHeader(InBitStream stream) {
        this.deferredSequenceNumber = stream.read(PacketNotification.SEQUENCE_NUMBER_BITS);
        this.deferredLastReceived = stream.read(PacketNotification.SEQUENCE_NUMBER_BITS);
        this.deferredNotificationBits.read(stream);
        this.deferredWaiting = true;

        PacketSequenceStatus status = this.getPacketSequenceStatus(this.deferredSequenceNumber);

        this.lastReadPacketHeaderWasValid = status != PacketSequenceStatus.INVALID;

        return status;
    }

    public void advancePacketSequenceNumber() {
        this.sendSequenceNumber = (this.sendSequenceNumber + 1) & PacketNotification.SEQUENCE_NUMBER_MASK;
    }

    public int packetReceivedAndVerified(boolean verified) {
        this.deferredWaiting = false;
        if (!this.lastReadPacketHeaderWasValid) {
            //log.error("Packet header was not valid");
            return 0;
        }
        int lostIncomingPacketCount = this.setLocalPacketNotificationBits(this.deferredSequenceNumber, verified);
        this.packetsConfirmedByPeer(this.deferredLastReceived, this.deferredNotificationBits);
        return lostIncomingPacketCount;
    }

    private void packetsConfirmedByPeer(long sequenceNumberReceivedByPeer, PacketNotificationBits receivedBits) {
        long numConfirmed = (sequenceNumberReceivedByPeer - this.lastSequenceNumberThatPeerReceived) & PacketNotification.SEQUENCE_NUMBER_MASK;

        if (numConfirmed <= 0) {
            return;
        }

        this.lastSequenceNumberThatPeerReceived = sequenceNumberReceivedByPeer;

        for (int i = (int) (numConfirmed - 1); i >= 0; --i)
        {
            this.packetNotificationQueue.add((receivedBits.isBitIndexSet(i) ? PacketDeliveryStatus.SUCCEEDED : PacketDeliveryStatus.FAILED));
        }
    }

    private void setPacketNotificationBitsLost(int packetCount) {
        this.packetNotificationBits.lshGeneric(packetCount);
    }

    private void setPacketNotificationBitsSuccess(boolean receivedOk) {
        this.packetNotificationBits.lshSingle(1);
        if (receivedOk) {
            this.packetNotificationBits.setBitIndex(0);
        }
    }

    private int setLocalPacketNotificationBits(long sequenceNumber, boolean verified) {
        int delta = (int) ((sequenceNumber - this.lastProcessedSequenceNumber) & PacketNotification.SEQUENCE_NUMBER_MASK);
        if (delta <= 0) {
            return 0;
        }

        int packetsLost = delta - 1;
        if (packetsLost > 0) {
            this.setPacketNotificationBitsLost(packetsLost);
        }

        this.setPacketNotificationBitsSuccess(verified);
        this.lastProcessedSequenceNumber = sequenceNumber;
        return Math.max(0, packetsLost);
    }

    public PacketSequenceStatus getPacketSequenceStatus(long sequenceNumber) {
        long remoteDelta = (this.sendSequenceNumber - this.deferredLastReceived) & PacketNotification.SEQUENCE_NUMBER_MASK;
        if (remoteDelta > this.getNumberOfPacketsInFlight()) {
            return PacketSequenceStatus.INVALID;
        }

        int delta = this.getSequenceDelta(sequenceNumber);

        if (delta == 1) {
            return PacketSequenceStatus.NEXT;
        } else if (delta > 1 && delta <= PacketNotification.SIZE) {
            return PacketSequenceStatus.FUTURE;
        }

        return PacketSequenceStatus.INVALID;
    }

    public long getNumberOfPacketsInFlight() {
        return (this.sendSequenceNumber - this.lastSequenceNumberThatPeerReceived) & PacketNotification.SEQUENCE_NUMBER_MASK;
    }

    public int getSequenceDelta(long sequenceNumber) {
        return this.getSequenceDelta(this.lastSequenceNumber, sequenceNumber);
    }

    public int getSequenceDelta(long lastSequenceNumber, long newSequenceNumber) {
        int delta = (int) ((newSequenceNumber - lastSequenceNumber) & PacketNotification.SEQUENCE_NUMBER_MASK);
        if ((long) delta <= PacketNotification.SIZE) {
            return delta;
        } else {
            return -(PacketNotification.SEQUENCE_NUMBER_MASK + 1 - delta);
        }
    }

    public static class PacketNotificationBits {

        private final long[] bits = new long[PacketNotification.SIZE >> 5];

        public void write(OutBitStream stream) {
            for (long bit : this.bits) {
                stream.write(bit, 32);
            }
        }

        public void read(InBitStream stream) {
            for (int i = 0; i < this.bits.length; i++) {
                this.bits[i] = stream.read(32);
            }
        }

        public void lshSingle(long shiftCount) {
            for (int i = this.bits.length - 1; i > 0; i--) {
                this.bits[i] = ((this.bits[i] << 32 | this.bits[i - 1]) << shiftCount) >> 32;
            }
            bits[0] = bits[0] << shiftCount;
        }

        public void lshGeneric(long shiftCount) {
            for (long i = shiftCount; i > 0; i -= 32) {
                this.lshSingle(Math.min(i, 32));
            }
        }

        public void setBitIndex(int num) {
            this.bits[num >> 5] |= 1L << (num & 31);
        }

        public boolean isBitIndexSet(int num) {
            return (this.bits[num >> 5] & (1L << (num & 31))) != 0;
        }

    }

    public enum PacketSequenceStatus {
        NEXT,
        FUTURE,
        INVALID
    }

    public enum PacketDeliveryStatus {
        FAILED,
        SUCCEEDED,
        DISCARDED
    }

}
