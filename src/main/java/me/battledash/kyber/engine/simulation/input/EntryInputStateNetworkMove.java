package me.battledash.kyber.engine.simulation.input;

import me.battledash.kyber.network.common.CharacterEntryInputExtent;
import me.battledash.kyber.network.common.ClientAuthoritativeInputExtent;
import me.battledash.kyber.network.common.ClientVec3ToServerInputExtent;
import me.battledash.kyber.network.common.EmoteCharacterStatePlayerAbilityInputExtent;
import me.battledash.kyber.network.common.EntryInputExtent;
import me.battledash.kyber.network.common.Unknown2InputExtent;
import me.battledash.kyber.network.common.UnknownInputExtent;
import me.battledash.kyber.network.common.VehicleEntryInputExtent;
import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.streams.BitStreamRead;

import java.util.ArrayList;
import java.util.List;

public class EntryInputStateNetworkMove extends MoveObject {

    private static final int ID_BITS = 25;
    private static final int ENTRY_BITS = 6;

    private final List<EntryInputExtent> extents = new ArrayList<>();

    private int ticks;
    private int controllableId;
    private int entryId;

    public EntryInputStateNetworkMove(EntryInputStateNetworkMove inputState) {
        this.setSequenceNumber(inputState.getSequenceNumber());
    }

    public EntryInputStateNetworkMove() {

    }

    {
        this.extents.add(new ClientAuthoritativeInputExtent());
        this.extents.add(new CharacterEntryInputExtent());
        this.extents.add(new ClientVec3ToServerInputExtent());
        this.extents.add(new EmoteCharacterStatePlayerAbilityInputExtent());
        this.extents.add(new UnknownInputExtent());
        this.extents.add(new Unknown2InputExtent());
        this.extents.add(new VehicleEntryInputExtent());
    }

    @Override
    public boolean moveRead(GhostConnection ghostConnection, BitStreamRead stream) {
        System.out.println("moveRead");
        this.ticks = (int) stream.readUnsigned(32);
        this.controllableId = (int) stream.readUnsigned(EntryInputStateNetworkMove.ID_BITS);

        this.entryId = (int) stream.readUnsigned(EntryInputStateNetworkMove.ENTRY_BITS);
        stream.readUnsigned64(EntryInputActionBindings.NumNetworkedDigitalInputs);
        stream.readBitset((int) EntryInputActionBindings.MaxInputs);
        stream.readBitset((int) EntryInputActionBindings.MaxInputs);
        stream.readBitset((int) EntryInputActionBindings.MaxInputs);

        for (int i = 0; i < EntryInputActionBindings.NumNetworkedAnalogInputs; i++) {
            if (EntryInputActionBindings.isSignedAnalogForIndex(i)) {
                stream.readSignedUnitFloat(32);
            } else {
                stream.readUnsignedUnitFloat(32);
            }
        }

        stream.readFloat();
        stream.readBool();

        if (stream.readBool()) {
            stream.readUnsigned(8);
        }

        if (stream.readBool()) {
            stream.readBool();
        }

        for (EntryInputExtent extent : this.extents) {
            extent.readFromStream(ghostConnection, stream, null);
        }

        return true;
    }

    @Override
    public boolean moveReadDelta(GhostConnection ghostConnection, BitStreamRead stream, MoveObject baseObject, MoveObject prevObject, boolean isFirstMove) {
        EntryInputStateNetworkMove baseMove = (EntryInputStateNetworkMove) baseObject;
        EntryInputStateNetworkMove prevMove = (EntryInputStateNetworkMove) prevObject;

        this.ticks = prevMove.ticks + 1;
        boolean shouldReadTicks = true;
        if (!isFirstMove) {
            shouldReadTicks = stream.readBool();
        }
        if (shouldReadTicks) {
            this.ticks = (int) stream.readUnsigned(32);
        }

        this.controllableId = prevMove.controllableId;
        this.entryId = prevMove.entryId;
        if (stream.readBool()) {
            this.controllableId = (int) stream.readUnsigned(EntryInputStateNetworkMove.ID_BITS);
            this.entryId = (int) stream.readUnsigned(EntryInputStateNetworkMove.ENTRY_BITS);
        }

        if (stream.readBool()) {
            stream.readUnsigned64(EntryInputActionBindings.NumNetworkedDigitalInputs);
        }

        if (stream.readBool()) {
            stream.readBitset((int) EntryInputActionBindings.MaxInputs);
        }

        if (stream.readBool()) {
            stream.readBitset((int) EntryInputActionBindings.MaxInputs);
        }

        if (stream.readBool()) {
            stream.readBitset((int) EntryInputActionBindings.MaxInputs);
        }

        boolean readAnalogValues = stream.readBool();
        for (int i = 0; i < EntryInputActionBindings.NumNetworkedAnalogInputs; i++) {
            if (readAnalogValues && stream.readBool()) {
                if (EntryInputActionBindings.isSignedAnalogForIndex(i)) {
                    stream.readSignedUnitFloat(32);
                } else {
                    stream.readUnsignedUnitFloat(32);
                }
            }
        }

        if (stream.readBool()) {
            stream.readFloat();
        }

        stream.readBool();

        if (stream.readBool()) {
            stream.readUnsigned(8);
        }

        if (stream.readBool()) {
            stream.readFloat();
        }

        for (int i = 0; i < this.extents.size(); i++) {
            EntryInputExtent extent = this.extents.get(i);
            EntryInputExtent prevExtent = prevMove.extents.get(i);
            extent.readFromStream(ghostConnection, stream, prevExtent);
        }

        return true;
    }

}
