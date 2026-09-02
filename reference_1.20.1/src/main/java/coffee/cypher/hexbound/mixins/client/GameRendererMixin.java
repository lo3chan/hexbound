package coffee.cypher.hexbound.mixins.client;

import coffee.cypher.hexbound.init.ClientRegistriesKt;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.ShaderProgram;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.function.Consumer;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @ModifyExpressionValue(
            method = "loadShaders",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/Lists;newArrayListWithCapacity(I)Ljava/util/ArrayList;",
                    remap = false
            )
    )
    private ArrayList<Pair<ShaderProgram, Consumer<ShaderProgram>>> hexbound$loadShaders(
            ArrayList<Pair<ShaderProgram, Consumer<ShaderProgram>>> programList,
            ResourceManager manager
    ) {
        ClientRegistriesKt.initShaders(manager, programList::add);
        return programList;
    }
}
