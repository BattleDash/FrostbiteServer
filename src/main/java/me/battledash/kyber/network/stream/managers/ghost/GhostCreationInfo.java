package me.battledash.kyber.network.stream.managers.ghost;

import lombok.Data;
import me.battledash.kyber.streams.BitStreamRead;

@Data
public class GhostCreationInfo {

    private final BitStreamRead stream;
    private final GhostConnection ghostConnection;

}
