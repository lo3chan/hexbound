package coffee.cypher.hexbound.mixins;

import coffee.cypher.hexbound.init.HexboundData;
import net.minecraft.block.BlockState;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SweetBerryBushBlock.class)
abstract class SweetBerryBushBlockMixin {
    @Inject(
        method = "onEntityCollision",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;slowMovement(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Vec3d;)V"
        ),
        cancellable = true
    )
    private void hexbound$leaveConstructsAlone(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity.getType() == HexboundData.EntityTypes.INSTANCE.getSPIDER_CONSTRUCT()) {
            ci.cancel();
        }
    }
}
