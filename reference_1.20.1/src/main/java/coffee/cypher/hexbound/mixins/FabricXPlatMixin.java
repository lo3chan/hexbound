package coffee.cypher.hexbound.mixins;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.fabric.xplat.FabricXplatImpl;
import coffee.cypher.hexbound.feature.media_attachment.MediaAttachmentKt;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FabricXplatImpl.class)
abstract class FabricXPlatMixin {
    @ModifyReturnValue(
        method = "findMediaHolder",
        at = @At("RETURN")
    )
    private ADMediaHolder hexbound$tryMediaAttachment(ADMediaHolder base, ItemStack stack) {
        var attachment = MediaAttachmentKt.getMediaAttachmentForStack(stack);

        if (attachment != null) {
            return attachment;
        }

        return base;
    }
}
