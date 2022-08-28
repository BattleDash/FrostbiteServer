package me.battledash.kyber.types.pojo;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

import java.util.UUID;

@Data
public class Asset implements EbxPOJO {

    private String Name;
    private int AssetFormat = 1;
    private UUID SpecializationSignature;
    private boolean IsSpecialized = false;
    private boolean UnzipSandbox = true;
    private boolean IsBundleRoot = false;
    private UUID BaseAssetGuid;

}
