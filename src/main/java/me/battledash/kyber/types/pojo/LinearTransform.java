package me.battledash.kyber.types.pojo;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class LinearTransform implements EbxPOJO {

    private Vec3 right;
    private Vec3 up;
    private Vec3 forward;
    private Vec3 trans;

}
