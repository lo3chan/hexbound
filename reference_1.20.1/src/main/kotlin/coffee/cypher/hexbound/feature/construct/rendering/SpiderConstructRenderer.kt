package coffee.cypher.hexbound.feature.construct.rendering

import coffee.cypher.hexbound.feature.construct.entity.SpiderConstructEntity
import coffee.cypher.hexbound.init.Hexbound
import coffee.cypher.hexbound.init.config.HexboundConfig
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Axis
import software.bernie.geckolib.cache.`object`.BakedGeoModel
import software.bernie.geckolib.cache.`object`.GeoBone
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoEntityRenderer
import software.bernie.geckolib.renderer.GeoRenderer
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer
import software.bernie.geckolib.renderer.layer.GeoRenderLayer

class SpiderConstructRenderer(
    renderManager: EntityRendererFactory.Context
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

    override fun getModelResource(obj: SpiderConstructEntity): Identifier {
        return if (obj.isAltModelEnabled || HexboundConfig.replaceSpiderConstruct)
            ALT_MODEL_RESOURCE
        else
            MODEL_RESOURCE
    }

    override fun getTextureResource(obj: SpiderConstructEntity): Identifier {
        return if (obj.isAltModelEnabled || HexboundConfig.replaceSpiderConstruct)
            ALT_TEXTURE_RESOURCE
        else
            TEXTURE_RESOURCE
    }

    override fun getAnimationResource(animatable: SpiderConstructEntity): Identifier {
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
        poseStack: MatrixStack,
        bone: GeoBone?,
        stack: ItemStack?,
        animatable: SpiderConstructEntity?,
        bufferSource: VertexConsumerProvider?,
        partialTick: Float,
        packedLight: Int,
        packedOverlay: Int
    ) {
        poseStack.push()
        poseStack.scale(0.25f, 0.25f, 0.25f)
        poseStack.multiply(Axis.X_POSITIVE.rotationDegrees(90f))

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

        poseStack.pop()
    }
}

class SpiderConstructTranslucentLayer(
    renderer: GeoRenderer<SpiderConstructEntity>
) : GeoRenderLayer<SpiderConstructEntity>(renderer) {

    override fun render(
        poseStack: MatrixStack,
        animatable: SpiderConstructEntity,
        bakedModel: BakedGeoModel,
        renderType: RenderLayer,
        bufferSource: VertexConsumerProvider,
        buffer: VertexConsumer,
        partialTick: Float,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val layerTexture = if (animatable.isAltModelEnabled || HexboundConfig.replaceSpiderConstruct)
            SpiderConstructModel.ALT_LAYER_TEXTURE_RESOURCE
        else
            SpiderConstructModel.LAYER_TEXTURE_RESOURCE

        val layer = RenderLayer.getEntityTranslucentCull(layerTexture)

        poseStack.push()
        renderer.preRender(poseStack, animatable, bakedModel, bufferSource, bufferSource.getBuffer(layer), false, partialTick, packedLight, packedOverlay, 1f, 1f, 1f, 1f);
        renderer.actuallyRender(poseStack, animatable, bakedModel, renderType, bufferSource, bufferSource.getBuffer(layer), false, partialTick, packedLight, packedOverlay, 1f, 1f, 1f, 1f)
        renderer.postRender(poseStack, animatable, bakedModel, bufferSource, bufferSource.getBuffer(layer), false, partialTick, packedLight, packedOverlay, 1f, 1f, 1f, 1f)
        poseStack.pop()
    }
}
