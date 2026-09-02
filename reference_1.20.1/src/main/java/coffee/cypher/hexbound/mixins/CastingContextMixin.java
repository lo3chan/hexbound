package coffee.cypher.hexbound.mixins;

import at.petrak.hexcasting.api.spell.casting.CastingContext;
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity;
import coffee.cypher.hexbound.feature.fake_circles.entity.ImpetusFakePlayer;
import coffee.cypher.hexbound.init.HexboundData;
import coffee.cypher.hexbound.util.mixinaccessor.CastingContextConstructAccessor;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(value = CastingContext.class)
abstract class CastingContextMixin implements CastingContextConstructAccessor {
    @Nullable
    private AbstractConstructEntity hexbound$construct;

    @Shadow
    @Final
    private ServerPlayerEntity caster;

    @Nullable
    @Override
    public AbstractConstructEntity getHexbound$construct() {
        return hexbound$construct;
    }

    @Override
    public void setHexbound$construct(@Nullable AbstractConstructEntity constructEntity) {
        hexbound$construct = constructEntity;
    }

    @ModifyReturnValue(
        method = "isCasterEnlightened",
        at = @At("RETURN"),
        remap = false
    )
    private boolean hexbound$fakeImpetusPlayersAreEnlightened(boolean original) {
        return original || caster instanceof ImpetusFakePlayer;
    }

    @ModifyConstant(
            method = "isVecInRange",
            constant = @Constant(
                    doubleValue = 1024.0
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/server/network/ServerPlayerEntity;getEyePos()Lnet/minecraft/util/math/Vec3d;"
                    )
            )
    )
    private double hexbound$ambitGone(double ambitSq) {
        var status = caster.getStatusEffect(HexboundData.StatusEffects.INSTANCE.getREDUCED_AMBIT());

        if (status != null) {
            var factor = (status.getAmplifier() + 1);
            return ambitSq / (factor * factor);
        }

        return ambitSq;
    }

    //TODO I'd take teleport on lvl 2 as well, but the sticky code is too much of a mess so you get to live
    @ModifyExpressionValue(
            method = "isVecInRange",
            at = @At(
                    value = "INVOKE",
                    target = "Lat/petrak/hexcasting/api/player/Sentinel;extendsRange()Z",
                    remap = false
            )
    )
    private boolean hexbound$sentinelGone(boolean original) {
        return original && !caster.hasStatusEffect(HexboundData.StatusEffects.INSTANCE.getREDUCED_AMBIT());
    }
}

//TODO arrow imbuement ideas: event horizon, teleport lock, shimmering (bypasses shields, cannot be zone/entity selected)
