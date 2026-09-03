package coffee.cypher.hexbound.mixins;

import at.petrak.hexcasting.common.casting.actions.raycast.OpEntityRaycast;
import coffee.cypher.hexbound.feature.combat.shield.ShieldEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
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
                    target = "Lat/petrak/hexcasting/common/casting/actions/raycast/OpEntityRaycast;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"
            ),
            index = 5,
            require = 0
    )
    private Predicate<Entity> hexbound$raycastIgnoresShield(Predicate<Entity> original, @Local(ordinal = 1) Vec3 look) {
        return (e) -> original.test(e) && !ShieldEntity.canBypassShieldForDirection(look, e);
    }
}
