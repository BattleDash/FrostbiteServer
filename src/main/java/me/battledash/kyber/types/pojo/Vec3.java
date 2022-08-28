package me.battledash.kyber.types.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Vec3 implements EbxPOJO {

    private float x, y, z;

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

}
