package coffee.cypher.hexbound.mixins.client;

import coffee.cypher.hexbound.util.rendering.TimedShaderProgram;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.ShaderProgram;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexBuffer.class)
abstract class VertexBufferMixin {
    @Inject(
            method = "drawInternal",
            at = @At("HEAD")
    )
    private void hexbound$setExtraShaderUniforms(Matrix4f viewMatrix, Matrix4f projectionMatrix, ShaderProgram shader, CallbackInfo ci) {
        var world = MinecraftClient.getInstance().world;

        if (world != null) {
            if (shader instanceof TimedShaderProgram timed) {
                var uniform = timed.getWorldTime();
                if (uniform != null) {
                    uniform.setFloat(world.getTime() + MinecraftClient.getInstance().getTickDelta());
                }
            }
        }
    }
}
