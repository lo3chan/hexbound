package coffee.cypher.hexbound.mixins;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.spell.casting.CastingContext;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import at.petrak.hexcasting.api.spell.mishaps.Mishap;
import at.petrak.hexcasting.api.spell.mishaps.MishapDisallowedSpell;
import coffee.cypher.hexbound.init.config.HexboundConfig;
import coffee.cypher.hexbound.util.HexboundFakePlayer;
import coffee.cypher.hexbound.util.mixinaccessor.CastingContextConstructAccessorKt;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = CastingHarness.class)
abstract class CastingHarnessMixin {
    @Shadow(remap = false)
    @Final
    private CastingContext ctx;

    @WrapOperation(
        method = "updateWithPattern",
        at = @At(
            value = "INVOKE",
            target = "Lat/petrak/hexcasting/api/mod/HexConfig$ServerConfigAccess;isActionAllowed(Lnet/minecraft/util/Identifier;)Z"
        )
    )
    private boolean hexbound$failForbiddenConstructSpells(
        HexConfig.ServerConfigAccess target,
        Identifier id,
        Operation<Boolean> op
    ) throws Mishap {
        if (
            (
                CastingContextConstructAccessorKt.getConstruct(ctx) != null ||
                    ctx.getCaster() instanceof HexboundFakePlayer
            ) && HexboundConfig.INSTANCE.isActionForbiddenForConstruct(id)
        ) {
            throw new MishapDisallowedSpell("disallowed_construct");
        }

        return op.call(target, id);
    }

    @ModifyVariable(
        method = "withdrawMedia",
        index = 1,
        argsOnly = true,
        at = @At(
            value = "LOAD"
        ),
        remap = false
    )
    private int hexbound$ignoreTinyCostsForConstructs(int value) {
        if (value <= MediaConstants.DUST_UNIT / 20 && CastingContextConstructAccessorKt.getConstruct(ctx) != null) {
            return 0;
        }

        return value;
    }

    @WrapWithCondition(
        method = "executeIotas",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V",
            remap = true
        )
    )
    private boolean hexbound$suppressConstructCasting(ServerWorld world, PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (CastingContextConstructAccessorKt.getConstruct(ctx) == null) {
            return true;
        }

        return ctx.getSource() != CastingContext.CastSource.STAFF;
    }
}
