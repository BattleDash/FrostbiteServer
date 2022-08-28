package me.battledash.kyber.network.stream.managers.message;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.server.GameContext;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamRollback;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.streams.InBitStream;
import me.battledash.kyber.network.PacketConnection;
import me.battledash.kyber.network.stream.StreamManager;
import me.battledash.kyber.network.stream.TransmitResult;
import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StreamManagerMessage extends StreamManager {

    public static final int STREAM_ID_BITS = 3;

    private static final long NUM_MESSAGES_BITS = 6;//MathUtil.getBitsNeeded32(StreamManagerMessage.MAX_MESSAGES_PER_PACKET);
    private static final long CMD_BITS = MathUtil.getBitsNeeded32(4);
    private static final long MAX_STREAMS = MathUtil.getBitsNeeded32(4);

    protected static final int MAX_MESSAGES_PER_PACKET = 127;

    @Getter
    private final List<MessageStream> messageStreams = new ArrayList<>();

    @Getter
    private final String name = "StreamManagerMessageEX";

    public StreamManagerMessage(PacketConnection connection, GhostConnection ghostConnection) {
        NetworkableMessageContext ctx = new NetworkableMessageContext(connection, ghostConnection);

        this.messageStreams.add(new MessageStream(ctx)); // ReliableGame
        this.messageStreams.add(new MessageStream(ctx)); // UnreliableGame

        this.messageStreams.add(new MessageStream(ctx)); // ReliableLevel
        this.messageStreams.add(new MessageStream(ctx)); // UnreliableLevel
    }

    public boolean addMessage(final NetworkableMessage message, int streamId) {
        if (streamId < 0 || streamId >= this.messageStreams.size()) {
            log.error("Invalid stream id {}", streamId);
            return false;
        }

        return this.messageStreams.get(streamId).addMessage(message);
    }

    public boolean addMessage(final NetworkableMessage message) {
        return this.addMessage(message, message.getMessageStream().ordinal());
    }

    @Override
    public TransmitResult transmitPacket(BitStreamWrite stream) {
        if (!this.hasDataToSend()) {
            return TransmitResult.NOTHING_TO_SEND;
        }

        boolean writeStreamStatus = false; // TODO: 5/7/2022 Implement getEncodedStreamCmd

        BitStreamRollback rollback = new BitStreamRollback(stream);

        int headerPos = stream.tell();
        int messagesWritten = 0;
        stream.writeUnsigned(messagesWritten, (int) StreamManagerMessage.NUM_MESSAGES_BITS);
        if (stream.writeBool(writeStreamStatus)) {
            // TODO: 5/7/2022 Implement getEncodedStreamCmd
        }

        if (stream.isOverflown()) {
            return TransmitResult.INCOMPLTE;
        }

        for (MessageStream messageStream : this.messageStreams) {
            messagesWritten += messageStream.processOutgoing(stream, StreamManagerMessage.MAX_MESSAGES_PER_PACKET - messagesWritten);
        }

        if (stream.isOverflown()) {
            return TransmitResult.INCOMPLTE;
        }

        if (messagesWritten > 0 || writeStreamStatus) {
            int currentPos = stream.tell();
            stream.seek(headerPos);
            stream.writeUnsigned(messagesWritten, (int) StreamManagerMessage.NUM_MESSAGES_BITS);
            if (stream.writeBool(writeStreamStatus)) {
                // TODO: 5/7/2022 Implement getEncodedStreamCmd
            }

            stream.seek(currentPos);

            log.info("Transmitted {} messages", messagesWritten);
            return TransmitResult.WHOLE;
        } else {
            rollback.rollback();
            return TransmitResult.NOTHING_TO_SEND;
        }
    }

    @Override
    public boolean processReceivedPacket(BitStreamRead stream) {
        long messagesToRead = stream.readUnsigned(StreamManagerMessage.NUM_MESSAGES_BITS);
        if (stream.readBool()) {
            long streamControl = stream.readUnsigned(12/*StreamManagerMessage.CMD_BITS * StreamManagerMessage.MAX_STREAMS*/);
            if (stream.getStream().isOverflow()) {
                log.error("Invalid stream control received, dropping packet");
                return false;
            }

            // TODO: 5/5/2022 Handle remote commands
        }

        if (messagesToRead > 0) {
            log.debug("Reading {} messages", messagesToRead);
        }

        if (messagesToRead > 0) {
            long messagesRead = 0;
            long messagesInSequence;

            do {
                messagesInSequence = this.deserializeSequence(stream);
                messagesRead += messagesInSequence;
            } while (messagesRead < messagesToRead && messagesInSequence > 0);
        }
        return true;
    }

    private long deserializeSequence(BitStreamRead stream) {
        InBitStream inStream = stream.getStream();

        boolean readMessage = stream.readBool();

        if (readMessage) {
            //long streamId = MathUtil.getBitsNeeded32(4); // TODO: 5/5/2022 Validate this is correct, adapted from MathUtil.getBitsNeeded32(4)
            long streamId = stream.readUnsigned(StreamManagerMessage.STREAM_ID_BITS);

            if (streamId >= 4 || inStream.isOverflow()) {
                log.debug("Invalid message stream received, dropping packet"); // TODO: 6/19/2022 Figure this out, happens on proxies frequently
                return 0;
            }

            return this.messageStreams.get((int) streamId).deserializeSequence(stream);
        }

        return 0;
    }

    private boolean hasDataToSend() {
        boolean hasDataToSend = false;

        for (MessageStream stream : this.messageStreams) {
            hasDataToSend |= stream.hasDataToSend();
        }

        return hasDataToSend;
    }

}
