package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.types.pojo.LinearTransform;
import me.battledash.kyber.types.RenderingOverrides;
import me.battledash.kyber.fs.ebx.EbxPOJO;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class StaticModelGroupEntityData extends GamePhysicsEntityData implements EbxPOJO {

    private StaticModelGroupMemberData[] MemberDatas;
    private long NetworkIdCount;
    private UUID HackToSolveRealTimeTweakingIssue;
    private boolean SecondaryLane;

    @Data
    public static class StaticModelGroupMemberData {
        private LinearTransform[] instanceTransforms;

        private long[] InstanceObjectVariation;
        private long[] InstanceObjectSubVariation;
        private RenderingOverrides[] InstanceRenderingOverrides;
        private RenderingOverrides.RadiosityTypeOverride[] InstanceRadiosityTypeOverride;

        private boolean[] InstanceTerrainShaderNodesEnable;
    }

}
