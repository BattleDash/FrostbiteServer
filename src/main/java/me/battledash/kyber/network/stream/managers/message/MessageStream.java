package me.battledash.kyber.network.stream.managers.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.messages.MessageManager;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamRollback;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.streams.InBitStream;
import me.battledash.kyber.types.network.NetworkableMessageFactory;
import me.battledash.kyber.types.network.messages.SpikeInternalMessagePartMessage;
import me.battledash.kyber.server.ServerGameContext;
import me.battledash.kyber.util.MathUtil;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Data
@Slf4j
public class MessageStream {

    public static final int MAX_MESSAGE_PART_SIZE = 32;
    public static final int MAX_MESSAGE_PART_COUNT = 256;
    public static final int MAX_COMPOSITE_MESSAGE_SIZE = MessageStream.MAX_MESSAGE_PART_SIZE * MessageStream.MAX_MESSAGE_PART_COUNT;
    public static final int MAX_UNSPLIT_MESSAGE_SIZE = 128;

    private static final int SERIAL_NUMBER_BITS = 8;
    private static final int SERIAL_NUMBER_MASK = (1 << MessageStream.SERIAL_NUMBER_BITS) - 1;
    private static final int INVALID_SERIAL = 0xFFFF;

    public static final long NET_MESSAGE_BITS_NEEDED = MathUtil.getBitsNeeded32(188);

    private final SpikeInternalMessagePartAssembler spikeInternalMessagePartAssembler = new SpikeInternalMessagePartAssembler();

    private final NetworkableMessageContext context;

    private final boolean supportsPacketSplit = true;
    private final boolean hasSerial = true;

    private final Queue<Message> outgoingMessages = new LinkedList<>();
    private int messagesAdded = 0;
    private int currentSendSerial = 0;
    private int messagesWritten;
    private List<NetworkableMessage> messagesToDispatch = new ArrayList<>();

    public long deserializeSequence(BitStreamRead stream) {
        IncomingMessage[] messages = new IncomingMessage[StreamManagerMessage.MAX_MESSAGES_PER_PACKET];
        int messagesRead = 0;

        InBitStream inStream = stream.getStream();

        long currentSerial = 0;
        if (this.hasSerial) {
            currentSerial = stream.readUnsigned(MessageStream.SERIAL_NUMBER_BITS);
        }

        boolean error = inStream.isOverflow();
        boolean messageFollows = true;
        while (messageFollows && !error) {
            NetworkableMessage message = this.deserializeMessage(stream);
            if (message != null) {
                messages[messagesRead++] = new IncomingMessage(currentSerial, message);

                currentSerial = (currentSerial + 1) & MessageStream.SERIAL_NUMBER_MASK;

                messageFollows = stream.readBool();
                if (messageFollows && (messagesRead >= StreamManagerMessage.MAX_MESSAGES_PER_PACKET)) {
                    log.warn("Received too many messages in one packet, dropping packet");
                    error = true;
                }
            } else {
                error = true;
            }

            error |= inStream.isOverflow();
        }

        if (error) {
            log.warn("Error deserializing message stream");

            return 0;
        } else {
            MessageManager messageManager = ServerGameContext.context().getMessageManager();
            for (IncomingMessage message : messages) {
                if (message != null) {
                    NetworkableMessage msg = this.spikeInternalMessagePartAssembler.addMessagePart(message.getMessage(), this.context);
                    if (msg != null) {
                        log.debug("Received message: {}", msg);
                        this.messagesToDispatch.add(msg);
                        //messageManager.dispatchMessage(msg);
                    }
                }
            }

            return messagesRead;
        }
    }

    private NetworkableMessage deserializeMessage(BitStreamRead stream) {
        int classId = (int) stream.readUnsigned(MessageStream.NET_MESSAGE_BITS_NEEDED);

        log.debug("Deserializing message with class id: {}, have {} bytes to read ({})", classId, stream.getStream().getRemainingBitCount() / 8,
                MessageStream.NET_MESSAGE_BITS_NEEDED);

        NetworkableMessage msg = NetworkableMessageFactory.createNetworkableMessage(classId);

        if (msg == null) {
            log.error("No networkable message with id {}", classId);
            return null;
        }

        if (!msg.readFrom(stream, this.context)) {
            log.error("Error deserializing {} from connection {}", msg.getClass().getSimpleName(), msg.getServerConnection().getConnectionId());
        }

        return msg;
    }

    public long processOutgoing(BitStreamWrite stream, int maxMessagesToWrite) {
        int messagesToWrite = maxMessagesToWrite;

        int messagesWritten;
        Deque<Message> record = new LinkedList<>();
        while ((messagesWritten = this.generateAndSerializeSequence(stream, messagesToWrite, 0, this.outgoingMessages, record)) > 0) {
            this.currentSendSerial = (record.getLast().getSerial() + 1) & MessageStream.SERIAL_NUMBER_MASK;
            messagesToWrite -= messagesWritten;
            this.messagesWritten += messagesWritten;
        }

        return maxMessagesToWrite - messagesToWrite;
    }

    private int generateAndSerializeSequence(BitStreamWrite stream, int maxMessages, int streamId, Queue<Message> outgoingMessages, Deque<Message> record) {
        if (outgoingMessages.isEmpty() || maxMessages == 0) {
            return 0;
        }

        BitStreamRollback rollback = new BitStreamRollback(stream);

        int markerPos = stream.tell();
        stream.writeBool(true);

        int sequenceSerial = outgoingMessages.peek().getSerial();

        stream.writeUnsigned(streamId, StreamManagerMessage.STREAM_ID_BITS);
        if (this.hasSerial) {
            sequenceSerial = (sequenceSerial == MessageStream.INVALID_SERIAL) ? this.currentSendSerial : sequenceSerial;
            stream.writeUnsigned(sequenceSerial, MessageStream.SERIAL_NUMBER_BITS);
        }

        if (stream.isOverflown()) {
            rollback.rollback();
            return 0;
        }

        long nextSerial = sequenceSerial;
        int messagesWritten = 0;

        while (!outgoingMessages.isEmpty() && (messagesWritten < maxMessages)) {
            Message message = outgoingMessages.peek();

            if (this.hasSerial && message.getSerial() != MessageStream.INVALID_SERIAL && message.getSerial() != nextSerial) {
                log.warn("Message serial number is not in sequence, dropping message");
                break;
            }

            BitStreamRollback sendRollback = new BitStreamRollback(stream);
            this.serializeMessage(stream, message);

            int newMarkerPos = stream.tell();

            stream.writeBool(true);
            if (!stream.isOverflown()) {
                markerPos = newMarkerPos;

                message.setSerial((int) nextSerial);
                nextSerial = (nextSerial + 1) & MessageStream.SERIAL_NUMBER_MASK;

                record.add(message);
                outgoingMessages.remove();

                messagesWritten++;
            } else {
                log.warn("Message stream overflow, dropping message");
                sendRollback.rollback();
                break;
            }
        }

        stream.seek(markerPos);
        stream.writeBool(false);
        stream.flush();

        if (messagesWritten == 0) {
            log.warn("No messages written, rolling back");
            rollback.rollback();
            return 0;
        }

        return messagesWritten;
    }

    private void serializeMessage(BitStreamWrite stream, Message message) {
        if (message.getPartCount() == 1) {
            BitStreamRead readStream = new BitStreamRead();
            readStream.initRead(message.getBuffer(), (int) message.getBitCount());

            stream.writeUnsigned(message.getOriginalId(), (int) MessageStream.NET_MESSAGE_BITS_NEEDED);
            stream.writeStream(readStream, (int) message.getBitCount());
        } else {
            int id = NetworkableMessageFactory.getNetworkableMessageId(SpikeInternalMessagePartMessage.class);
            long bufferSize = ((message.getBitCount() + 7) & ~7) >> 3;

            stream.writeUnsigned(id, (int) MessageStream.NET_MESSAGE_BITS_NEEDED);
            SpikeInternalMessagePartMessage.writePartMessage(stream, message.getPartIndex(), message.getPartCount(),
                    message.getOriginalId(), message.getBuffer(), bufferSize);
        }
    }

    public boolean addMessage(final NetworkableMessage message) {
        BitStreamWrite stream = new BitStreamWrite();
        ByteBuf buffer = Unpooled.buffer(MessageStream.MAX_COMPOSITE_MESSAGE_SIZE, MessageStream.MAX_COMPOSITE_MESSAGE_SIZE);
        stream.initWrite(buffer, MessageStream.MAX_COMPOSITE_MESSAGE_SIZE);

        message.writeTo(stream, new NetworkableMessageContext(message.getServerConnection(), message.getGhostConnection()));

        stream.flush();

        long maxPartSize = this.supportsPacketSplit ? MessageStream.MAX_MESSAGE_PART_SIZE : MessageStream.MAX_UNSPLIT_MESSAGE_SIZE;

        long bitCount = stream.tell();
        long byteCount = ((bitCount + 7) & ~7) >> 3;
        long messageCount = Math.max(1, (byteCount + maxPartSize - 1) / maxPartSize);

        if (!this.supportsPacketSplit && messageCount > 1) {
            log.warn("Message is too large to send in one packet, {} bytes will be dropped. Max message size is {} bytes",
                    byteCount, MessageStream.MAX_UNSPLIT_MESSAGE_SIZE);
            return false;
        }

        log.info("Sending message {}", message);

        int networkableClassId = NetworkableMessageFactory.getNetworkableMessageId(message.getClass());
        int bufOffset = 0;
        for (int i = 0; i < messageCount; i++) {
            Message msg = new Message();

            int bufferSize = (int) Math.abs(Math.min(byteCount - bufOffset, maxPartSize));
            msg.setBuffer(Unpooled.buffer());

            msg.setStream(0);
            msg.setOriginalId(networkableClassId);
            msg.setPartIndex(i);
            msg.setPartCount(messageCount);
            msg.setBitCount(Math.min(bitCount, maxPartSize * 8));
            msg.setSerial(MessageStream.INVALID_SERIAL);

            msg.getBuffer().writeBytes(stream.getStream().getBuffer().retainedSlice(bufOffset, bufferSize));

            bufOffset += bufferSize;
            bitCount -= maxPartSize * 8;

            this.outgoingMessages.add(msg);
            ++this.messagesAdded;
        }

        return true;
    }

    public boolean hasDataToSend() {
        return !this.outgoingMessages.isEmpty();
    }

    @Data
    public static class IncomingMessage {
        private final long serial;
        private final NetworkableMessage message;
    }

    @Data
    private static class Message {
        private int stream;
        private int originalId;
        private int partIndex;
        private long partCount;
        private long bitCount;
        private int serial;
        private ByteBuf buffer;
    }

}
