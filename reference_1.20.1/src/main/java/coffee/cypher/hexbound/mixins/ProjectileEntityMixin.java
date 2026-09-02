package coffee.cypher.hexbound.mixins;

import coffee.cypher.hexbound.feature.combat.shield.ShieldEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileEntity.class)
abstract class ProjectileEntityMixin extends Entity {
    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyReturnValue(
            method = "canHit",
            at = @At("RETURN")
    )
    private boolean hexbound$bypassShield(boolean bl, Entity toHit) {
        return bl && !ShieldEntity.canBypassShieldForDirection(getVelocity(), toHit);
    }
}
