package me.battledash.kyber.fs.ebx;

import lombok.Data;

import java.util.UUID;

@Data
public class AssetClassGuid {

    private UUID exportedGuid;
    private int internalId;
    private boolean isExported;

    public AssetClassGuid(UUID guid, int id) {
        this.exportedGuid = guid;
        this.internalId = id;
        this.isExported = guid != null;
    }

}