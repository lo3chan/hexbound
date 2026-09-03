package coffee.cypher.hexbound.mixins;

import coffee.cypher.hexbound.init.HexboundData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SweetBerryBushBlock.class)
abstract class SweetBerryBushBlockMixin {
    @Inject(
        method = "entityInside",
        at = @At("HEAD"),
        cancellable = true
    )
    private void hexbound$leaveConstructsAlone(BlockState state, Level world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity.getType() == HexboundData.EntityTypes.INSTANCE.getSPIDER_CONSTRUCT().get()) {
            ci.cancel();
        }
    }
}
