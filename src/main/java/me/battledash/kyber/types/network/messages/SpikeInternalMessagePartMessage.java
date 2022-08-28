package me.battledash.kyber.types.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.types.network.NetworkableMessageFactory;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;
import me.battledash.kyber.network.stream.managers.message.MessageStream;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessageContext;
import me.battledash.kyber.network.stream.managers.message.StreamType;

import java.util.List;

@Slf4j
@Setter
@Getter
@ToString
@NoArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.ANY, messageStream = StreamType.GAME_RELIABLE)
public class SpikeInternalMessagePartMessage extends NetworkableMessage {

    private int bufferSize;
    private int partCount;
    private int partIndex;
    private int originalId;
    private ByteBuf buffer;

    public boolean isLastPart() {
        return this.partIndex == this.partCount - 1;
    }

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        this.partIndex = (int) stream.readUnsignedLimit(0, MessageStream.MAX_MESSAGE_PART_COUNT - 1);
        if (this.partIndex == 0) {
            this.originalId = (int) stream.readUnsigned(MessageStream.NET_MESSAGE_BITS_NEEDED);
            if (!NetworkableMessageFactory.networkableMessageExists(this.originalId)) {
                log.warn("Received message with unknown id: {}", this.originalId);
                return false;
            }
        }

        this.bufferSize = MessageStream.MAX_MESSAGE_PART_SIZE;
        if (stream.readBool()) {
            this.bufferSize = (int) (1L + stream.readUnsignedLimit(0, MessageStream.MAX_MESSAGE_PART_SIZE - 1));
            this.partCount = this.partIndex + 1;
        }

        this.buffer = Unpooled.wrappedBuffer(stream.getStream().readOctets(this.bufferSize));
        return true;
    }

    @Override
    public boolean generatedWriteTo(BitStreamWrite stream) {
        return true;
    }

    public static void writePartMessage(BitStreamWrite stream, int partIndex, long partCount,
                                        int originalId, ByteBuf buffer, long bufferSize) {
        stream.writeUnsignedLimit(partIndex, 0, MessageStream.MAX_MESSAGE_PART_COUNT - 1);
        if (partIndex == 0) {
            stream.writeUnsigned(originalId, (int) MessageStream.NET_MESSAGE_BITS_NEEDED);
        }

        if (stream.writeBool(partIndex == partCount - 1)) {
            stream.writeUnsignedLimit(bufferSize - 1, 0, MessageStream.MAX_MESSAGE_PART_SIZE - 1);
            stream.getStream().writeOctets(buffer, (int) bufferSize);
        } else {
            stream.getStream().writeOctets(buffer, MessageStream.MAX_MESSAGE_PART_SIZE);
        }
    }

    public static NetworkableMessage assembleMessage(List<SpikeInternalMessagePartMessage> messages, NetworkableMessageContext ctx) {
        ByteBuf buffer = Unpooled.buffer(MessageStream.MAX_COMPOSITE_MESSAGE_SIZE);
        int bufferSize = 0;
        for (SpikeInternalMessagePartMessage msgPart : messages) {
            buffer.writeBytes(msgPart.getBuffer(), msgPart.getBufferSize());
            bufferSize += msgPart.getBufferSize();
        }

        BitStreamRead stream = new BitStreamRead();
        stream.initRead(buffer, bufferSize * 8);

        SpikeInternalMessagePartMessage firstMessage = messages.get(0);
        NetworkableMessage msg = NetworkableMessageFactory.createNetworkableMessage(firstMessage.getOriginalId());

        if (msg == null) {
            log.error("No networkable message with id {}", firstMessage.getOriginalId());
            return null;
        }

        boolean isReadSuccessful = msg.readFrom(stream, ctx);

        if (!isReadSuccessful || stream.isOverflown()) {
            log.error("Failed to read message from stream");
            return null;
        }

        return msg;
    }

}
