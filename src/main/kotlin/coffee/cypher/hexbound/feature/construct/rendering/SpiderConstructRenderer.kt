package coffee.cypher.hexbound.feature.construct.rendering

import coffee.cypher.hexbound.feature.construct.entity.SpiderConstructEntity
import coffee.cypher.hexbound.init.Hexbound
import coffee.cypher.hexbound.init.config.HexboundConfig
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.world.item.ItemStack
import net.minecraft.resources.ResourceLocation
import com.mojang.math.Axis
import software.bernie.geckolib.cache.`object`.BakedGeoModel
import software.bernie.geckolib.cache.`object`.GeoBone
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoEntityRenderer
import software.bernie.geckolib.renderer.GeoRenderer
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer
import software.bernie.geckolib.renderer.layer.GeoRenderLayer

class SpiderConstructRenderer(
    renderManager: EntityRendererProvider.Context
) : GeoEntityRenderer<SpiderConstructEntity>(
    renderManager,
    SpiderConstructModel()
) {
    init {
        addRenderLayer(SpiderConstructTranslucentLayer(this))
        addRenderLayer(SpiderConstructItemLayer(this))
    }
}

class SpiderConstructModel : GeoModel<SpiderConstructEntity>() {
    companion object {
        val MODEL_RESOURCE = Hexbound.id("geo/spider_construct.geo.json")
        val TEXTURE_RESOURCE = Hexbound.id("textures/construct/spider_construct.png")
        val ANIMATION_RESOURCE = Hexbound.id("animations/spider_construct.animation.json")
        val LAYER_TEXTURE_RESOURCE = Hexbound.id("textures/construct/spider_construct_translucent.png")

        val ALT_MODEL_RESOURCE = Hexbound.id("geo/robot_construct.geo.json")
        val ALT_TEXTURE_RESOURCE = Hexbound.id("textures/construct/robot_construct.png")
        val ALT_ANIMATION_RESOURCE = Hexbound.id("animations/robot_construct.animation.json")
        val ALT_LAYER_TEXTURE_RESOURCE = Hexbound.id("textures/construct/robot_construct_translucent.png")
    }

    override fun getModelResource(obj: SpiderConstructEntity): ResourceLocation {
        return if (obj.isAltModelEnabled || HexboundConfig.replaceSpiderConstruct)
            ALT_MODEL_RESOURCE
        else
            MODEL_RESOURCE
    }

    override fun getTextureResource(obj: SpiderConstructEntity): ResourceLocation {
        return if (obj.isAltModelEnabled || HexboundConfig.replaceSpiderConstruct)
            ALT_TEXTURE_RESOURCE
        else
            TEXTURE_RESOURCE
    }

    override fun getAnimationResource(animatable: SpiderConstructEntity): ResourceLocation {
        return if (animatable.isAltModelEnabled || HexboundConfig.replaceSpiderConstruct)
            ALT_ANIMATION_RESOURCE
        else
            ANIMATION_RESOURCE
    }
}

class SpiderConstructItemLayer(
    renderer: GeoRenderer<SpiderConstructEntity>
) : BlockAndItemGeoLayer<SpiderConstructEntity>(renderer) {
    override fun getStackForBone(bone: GeoBone, animatable: SpiderConstructEntity): ItemStack? {
        if ("item" in bone.name && !animatable.heldStack.isEmpty) {
            return animatable.heldStack
        }

        return null
    }

    override fun renderStackForBone(
        poseStack: PoseStack,
        bone: GeoBone,
        stack: ItemStack,
        animatable: SpiderConstructEntity,
        bufferSource: MultiBufferSource,
        partialTick: Float,
        packedLight: Int,
        packedOverlay: Int
    ) {
        poseStack.pushPose()
        poseStack.scale(0.25f, 0.25f, 0.25f)
        poseStack.mulPose(Axis.XP.rotationDegrees(90f))

        super.renderStackForBone(
            poseStack,
            bone,
            stack,
            animatable,
            bufferSource,
            partialTick,
            packedLight,
            packedOverlay
        )

        poseStack.popPose()
    }
}

class SpiderConstructTranslucentLayer(
    renderer: GeoRenderer<SpiderConstructEntity>
) : GeoRenderLayer<SpiderConstructEntity>(renderer) {

    override fun render(
        poseStack: PoseStack,
        animatable: SpiderConstructEntity,
        bakedModel: BakedGeoModel,
        renderType: RenderType?,
        bufferSource: MultiBufferSource,
        buffer: VertexConsumer?,
        partialTick: Float,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val layerTexture = if (animatable.isAltModelEnabled || HexboundConfig.replaceSpiderConstruct)
            SpiderConstructModel.ALT_LAYER_TEXTURE_RESOURCE
        else
            SpiderConstructModel.LAYER_TEXTURE_RESOURCE

        val layer = RenderType.entityTranslucentCull(layerTexture)
        val builder = bufferSource.getBuffer(layer)

        poseStack.pushPose()
        renderer.preRender(poseStack, animatable, bakedModel, bufferSource, builder, false, partialTick, packedLight, packedOverlay, packedOverlay)
        renderer.actuallyRender(poseStack, animatable, bakedModel, layer, bufferSource, builder, false, partialTick, packedLight, packedOverlay, packedOverlay)
        renderer.postRender(poseStack, animatable, bakedModel, bufferSource, builder, false, partialTick, packedLight, packedOverlay, packedOverlay)
        poseStack.popPose()
    }
}
