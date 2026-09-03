package coffee.cypher.hexbound.mixins;

import coffee.cypher.hexbound.feature.combat.shield.ShieldEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Projectile.class)
abstract class ProjectileEntityMixin extends Entity {
    public ProjectileEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyReturnValue(
            method = "canHitEntity",
            at = @At("RETURN")
    )
    private boolean hexbound$bypassShield(boolean bl, Entity toHit) {
        return bl && !ShieldEntity.canBypassShieldForDirection(getDeltaMovement(), toHit);
    }
}
