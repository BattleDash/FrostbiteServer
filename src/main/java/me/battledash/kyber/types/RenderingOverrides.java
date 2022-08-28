package me.battledash.kyber.types;

import lombok.Data;

@Data
public class RenderingOverrides {

    private BoolOverride ShadowEnable = BoolOverride.INHERIT;
    private BoolOverride DynamicReflectionEnable = BoolOverride.INHERIT;
    private BoolOverride StaticReflectionEnable = BoolOverride.INHERIT;
    private BoolOverride PlanarShadowEnable = BoolOverride.INHERIT;
    private BoolOverride DistantShadowCacheEnable = BoolOverride.INHERIT;

    public enum RadiosityTypeOverride {
        NONE,
        DYNAMIC,
        LIGHT_PROBE,
        TERRAIN_PROJECTED,
        PROXY
    }

}
