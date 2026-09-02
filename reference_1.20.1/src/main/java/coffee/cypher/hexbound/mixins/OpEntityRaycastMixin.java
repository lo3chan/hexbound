package coffee.cypher.hexbound.mixins;

import at.petrak.hexcasting.common.casting.actions.raycast.OpEntityRaycast;
import coffee.cypher.hexbound.feature.combat.shield.ShieldEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(OpEntityRaycast.class)
public class OpEntityRaycastMixin {
    @ModifyArg(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"
            ),
            index = 4
    )
    private Predicate<Entity> hexbound$raycastIgnoresShield(Predicate<Entity> original, @Local(ordinal = 1) Vec3d look) {
        return (e) -> original.test(e) && !ShieldEntity.canBypassShieldForDirection(look, e);
    }
}
