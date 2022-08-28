package me.battledash.kyber.types.pojo;

import lombok.Getter;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Getter
public class Vec4 implements EbxPOJO {

    private float x, y, z, w;

    public void set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

}
