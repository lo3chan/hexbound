package coffee.cypher.hexbound.feature.combat.shield

import coffee.cypher.hexbound.feature.combat.shield.ShieldEntity.VisualType.GLITCHY
import coffee.cypher.hexbound.feature.combat.shield.ShieldEntity.VisualType.REGULAR
import coffee.cypher.hexbound.init.Hexbound
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth

class ShieldRenderer(ctx: EntityRendererProvider.Context) : EntityRenderer<ShieldEntity>(ctx) {
    override fun render(
        entity: ShieldEntity,
        yaw: Float,
        tickDelta: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        light: Int
    ) {
        val worldTime = entity.level().gameTime + tickDelta
        val pigment = entity.pigment
        val colorTime = worldTime * 4
        val (_, up, right) = entity.getBasis()

        poseStack.pushPose()

        val renderType = when (entity.visualType) {
            REGULAR -> ShieldRenderLayer.REGULAR
            GLITCHY -> ShieldRenderLayer.GLITCHY
        }

        val buffer = bufferSource.getBuffer(renderType)
        poseStack.translate(0.0, 1.3125, 0.0)

        poseStack.mulPose(Axis.YP.rotationDegrees(180f - entity.yRot))
        poseStack.mulPose(Axis.XP.rotationDegrees(-entity.xRot))

        if (entity.tickCount < ShieldEntity.DEPLOY_TIME) {
            val deployProgress = (entity.tickCount + tickDelta) / ShieldEntity.DEPLOY_TIME
            poseStack.scale(deployProgress, deployProgress, deployProgress)
        }

        val entry = poseStack.last()
        val model = entry.pose()

        fun vertex(
            x: Float,
            y: Float,
            z: Float,
            u: Float,
            v: Float,
            color: Int
        ) {
            buffer
                .addVertex(model, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(entry, 0f, 0f, 1f)
        }

        val faceCount = 16

        repeat(faceCount) { xCount ->
            repeat(faceCount) { yCount ->
                val lowerXProgress = xCount / faceCount.toFloat()
                val lowerYProgress = yCount / faceCount.toFloat()
                val upperXProgress = (xCount + 1) / faceCount.toFloat()
                val upperYProgress = (yCount + 1) / faceCount.toFloat()

                val lowerX = Mth.lerp(lowerXProgress, -1.5f, 1.5f)
                val upperX = Mth.lerp(upperXProgress, -1.5f, 1.5f)
                val lowerY = Mth.lerp(lowerYProgress, -1.3125f, 1.3125f)
                val upperY = Mth.lerp(upperYProgress, -1.3125f, 1.3125f)

                val lowerU = Mth.lerp(lowerXProgress, 0f, 0.75f)
                val upperU = Mth.lerp(upperXProgress, 0f, 0.75f)
                val lowerV = Mth.lerp(lowerYProgress, 0f, 0.65625f)
                val upperV = Mth.lerp(upperYProgress, 0f, 0.65625f)

                val lowerXVec = right.scale(lowerX.toDouble())
                val upperXVec = right.scale(upperX.toDouble())
                val lowerYVec = up.scale(lowerY.toDouble())
                val upperYVec = up.scale(upperY.toDouble())

                val colorizer = pigment.colorProvider

                val lowerLeftColor = colorizer.getColor(colorTime, entity.position().add(lowerXVec).add(lowerYVec))
                val lowerRightColor = colorizer.getColor(colorTime, entity.position().add(upperXVec).add(lowerYVec))
                val upperLeftColor = colorizer.getColor(colorTime, entity.position().add(lowerXVec).add(upperYVec))
                val upperRightColor = colorizer.getColor(colorTime, entity.position().add(upperXVec).add(upperYVec))

                // Front face
                vertex(upperX, upperY, 6.25E-4f, upperU, upperV, upperRightColor)
                vertex(lowerX, upperY, 6.25E-4f, lowerU, upperV, upperLeftColor)
                vertex(lowerX, lowerY, 6.25E-4f, lowerU, lowerV, lowerLeftColor)
                vertex(upperX, lowerY, 6.25E-4f, upperU, lowerV, lowerRightColor)

                // Back face (ensures visibility from behind and prevents backface culling by external shaderpacks)
                vertex(upperX, upperY, -6.25E-4f, upperU, upperV, upperRightColor)
                vertex(upperX, lowerY, -6.25E-4f, upperU, lowerV, lowerRightColor)
                vertex(lowerX, lowerY, -6.25E-4f, lowerU, lowerV, lowerLeftColor)
                vertex(lowerX, upperY, -6.25E-4f, lowerU, upperV, upperLeftColor)
            }
        }

        poseStack.popPose()
    }

    companion object {
        val TEXTURE_RESOURCE = Hexbound.id("textures/combat/shield.png")
    }

    override fun getTextureLocation(entity: ShieldEntity): ResourceLocation {
        return TEXTURE_RESOURCE
    }
}

class ShieldRenderLayer private constructor(
    string: String,
    vertexFormat: VertexFormat,
    mode: VertexFormat.Mode,
    i: Int,
    bl: Boolean,
    bl2: Boolean,
    runnable: Runnable,
    runnable2: Runnable
) : RenderType(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2) {
    companion object {
        val REGULAR: RenderType = RenderType.entityTranslucent(ShieldRenderer.TEXTURE_RESOURCE)
        val GLITCHY: RenderType = RenderType.entityTranslucent(ShieldRenderer.TEXTURE_RESOURCE)

        lateinit var REGULAR_SHADER: ShaderInstance
        lateinit var GLITCHY_SHADER: ShaderInstance
    }
}
