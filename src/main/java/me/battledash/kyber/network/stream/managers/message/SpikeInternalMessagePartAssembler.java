package me.battledash.kyber.network.stream.managers.message;

import me.battledash.kyber.types.network.messages.SpikeInternalMessagePartMessage;

import java.util.ArrayList;
import java.util.List;

public class SpikeInternalMessagePartAssembler {

    private final List<SpikeInternalMessagePartMessage> messageParts = new ArrayList<>();

    public NetworkableMessage addMessagePart(NetworkableMessage msg, NetworkableMessageContext ctx) {
        if (msg instanceof SpikeInternalMessagePartMessage msgPart) {
            int partIndex = msgPart.getPartIndex();

            if (this.messageParts.size() <= partIndex || this.messageParts.get(partIndex) == null) {
                for (int i = this.messageParts.size(); i <= partIndex; i++) {
                    this.messageParts.add(null);
                }
                this.messageParts.set(partIndex, msgPart);
                if (msgPart.isLastPart()) {
                    NetworkableMessage newMsg = null;
                    if (this.messageParts.size() == partIndex + 1) {
                        newMsg = SpikeInternalMessagePartMessage.assembleMessage(this.messageParts, ctx);

                        this.messageParts.clear();
                    }

                    return newMsg;
                }

                return null;
            } else {
                throw new IllegalStateException("Message part already exists at index " + partIndex);
            }
        } else if (this.messageParts.size() > 0) {
            this.messageParts.clear();
        }

        return msg;
    }

}
