package me.battledash.kyber.network.stream.managers.ghost;

import lombok.Data;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;

public interface GhostCreator {

    boolean createGhost(BitStreamRead stream, int id, GhostCreatorResult result);

    void writeGhostHeader(BitStreamWrite stream, ServerGhost ghost);

    @Data
    class GhostCreatorResult {
        private ClientGhost ghost;
        private int userData;
    }

}
