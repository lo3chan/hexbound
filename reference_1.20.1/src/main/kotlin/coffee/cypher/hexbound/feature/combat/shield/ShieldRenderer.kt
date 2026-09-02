package coffee.cypher.hexbound.feature.combat.shield

import coffee.cypher.hexbound.feature.combat.shield.ShieldEntity.VisualType.GLITCHY
import coffee.cypher.hexbound.feature.combat.shield.ShieldEntity.VisualType.REGULAR
import coffee.cypher.hexbound.init.Hexbound
import coffee.cypher.hexbound.util.times
import com.mojang.blaze3d.shader.GlUniform
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.blaze3d.vertex.VertexFormats
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.ShaderProgram
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Axis
import net.minecraft.util.math.MathHelper
import org.quiltmc.loader.api.minecraft.ClientOnly
import org.quiltmc.qkl.library.math.plus

@ClientOnly
class ShieldRenderer(ctx: EntityRendererFactory.Context) : EntityRenderer<ShieldEntity>(ctx) {
    override fun render(
        entity: ShieldEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val worldTime = entity.world.time + tickDelta
        val pigment = entity.pigment
        val colorTime = worldTime * 4
        val (_, up, right) = entity.getBasis()


        GlUniform("time", GlUniform.TYPE_FLOAT, 1, ShieldRenderLayer.REGULAR_SHADER).setFloat(worldTime)

        matrices.push()

        val renderLayer = when (entity.visualType) {
            REGULAR -> ShieldRenderLayer.REGULAR
            GLITCHY -> ShieldRenderLayer.GLITCHY
        }

        val buffer = vertexConsumers.getBuffer(renderLayer)
        matrices.translate(0.0, 1.3125, 0.0)

        matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(180f - entity.yaw))
        matrices.multiply(Axis.X_POSITIVE.rotationDegrees(-entity.pitch))

        if (entity.age < ShieldEntity.DEPLOY_TIME) {
            val deployProgress = (entity.age + tickDelta) / ShieldEntity.DEPLOY_TIME
            matrices.scale(deployProgress, deployProgress, deployProgress)
        }

        val entry = matrices.peek()
        val model = entry.model
        val normal = entry.normal

        fun vertex(
            x: Float,
            y: Float,
            z: Float,
            u: Float,
            v: Float,
            color: Int
        ) {
            buffer
                .vertex(model, x, y, z)
                .color(color)
                .uv(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(normal, 0f, 0f, 1f)
                .next()
        }

        val faceCount = 16

        repeat(faceCount) { xCount ->
            repeat(faceCount) { yCount ->
                val lowerXProgress = xCount / faceCount.toFloat()
                val lowerYProgress = yCount / faceCount.toFloat()
                val upperXProgress = (xCount + 1) / faceCount.toFloat()
                val upperYProgress = (yCount + 1) / faceCount.toFloat()

                val lowerX = MathHelper.lerp(lowerXProgress, -1.5f, 1.5f)
                val upperX = MathHelper.lerp(upperXProgress, -1.5f, 1.5f)
                val lowerY = MathHelper.lerp(lowerYProgress, -1.3125f, 1.3125f)
                val upperY = MathHelper.lerp(upperYProgress, -1.3125f, 1.3125f)

                val lowerU = MathHelper.lerp(lowerXProgress, 0f, 0.75f)
                val upperU = MathHelper.lerp(upperXProgress, 0f, 0.75f)
                val lowerV = MathHelper.lerp(lowerYProgress, 0f, 0.65625f)
                val upperV = MathHelper.lerp(upperYProgress, 0f, 0.65625f)

                val lowerXVec = right * lowerX.toDouble()
                val upperXVec = right * upperX.toDouble()
                val lowerYVec = up * lowerY.toDouble()
                val upperYVec = up * upperY.toDouble()

                val colorizer = pigment.colorProvider

                val lowerLeftColor = colorizer.getColor(colorTime, entity.pos + lowerXVec + lowerYVec)
                val lowerRightColor = colorizer.getColor(colorTime, entity.pos + upperXVec + lowerYVec)
                val upperLeftColor = colorizer.getColor(colorTime, entity.pos + lowerXVec + upperYVec)
                val upperRightColor = colorizer.getColor(colorTime, entity.pos + upperXVec + upperYVec)

                vertex(upperX, upperY, 6.25E-4f, upperU, upperV, upperRightColor)
                vertex(lowerX, upperY, 6.25E-4f, lowerU, upperV, upperLeftColor)
                vertex(lowerX, lowerY, 6.25E-4f, lowerU, lowerV, lowerLeftColor)
                vertex(upperX, lowerY, 6.25E-4f, upperU, lowerV, lowerRightColor)
            }
        }

        matrices.pop()
    }

    companion object {
        val TEXTURE_RESOURCE = Hexbound.id("textures/combat/shield.png")
    }

    override fun getTexture(entity: ShieldEntity): Identifier {
        return TEXTURE_RESOURCE
    }
}

@ClientOnly
class ShieldRenderLayer private constructor(
    string: String,
    vertexFormat: VertexFormat,
    mode: VertexFormat.DrawMode,
    i: Int,
    bl: Boolean,
    bl2: Boolean,
    runnable: Runnable,
    runnable2: Runnable
) : RenderLayer(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2) {
    init {
        throw UnsupportedOperationException("Should not be instantiated")
    }

    @ClientOnly
    companion object {
        val REGULAR = prepareLayer { REGULAR_SHADER }
        val GLITCHY = prepareLayer { GLITCHY_SHADER }

        private fun prepareLayer(shader: () -> ShaderProgram): RenderLayer {
            val multiPhase = MultiPhaseParameters.builder()
                .shader(Shader(shader))
                .texture(Texture(ShieldRenderer.TEXTURE_RESOURCE, false, false))
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .cull(DISABLE_CULLING)
                .lightmap(ENABLE_LIGHTMAP)
                .overlay(ENABLE_OVERLAY_COLOR)
                .build(true)

            @Suppress("INACCESSIBLE_TYPE")
            return of(
                Hexbound.id("shield").toString(),
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS,
                256, true, true,
                multiPhase
            )
        }

        lateinit var REGULAR_SHADER: ShaderProgram
        lateinit var GLITCHY_SHADER: ShaderProgram
    }
}
