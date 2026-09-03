package coffee.cypher.hexbound.init

import coffee.cypher.hexbound.feature.combat.shield.ShieldRenderLayer
import coffee.cypher.hexbound.feature.combat.shield.ShieldRenderer
import coffee.cypher.hexbound.feature.construct.rendering.SpiderConstructRenderer
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.client.renderer.ShaderInstance
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.RegisterShadersEvent

fun initEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
    event.registerEntityRenderer(HexboundData.EntityTypes.SPIDER_CONSTRUCT.get()) { ctx -> SpiderConstructRenderer(ctx) }
    event.registerEntityRenderer(HexboundData.EntityTypes.SHIELD.get()) { ctx -> ShieldRenderer(ctx) as net.minecraft.client.renderer.entity.EntityRenderer<coffee.cypher.hexbound.feature.combat.shield.ShieldEntity> }
}

fun initShaders(event: RegisterShadersEvent) {
    event.registerShader(
        ShaderInstance(
            event.resourceProvider,
            Hexbound.id("rendertype_construct"),
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP
        )
    ) { ShieldRenderLayer.REGULAR_SHADER = it }

    event.registerShader(
        ShaderInstance(
            event.resourceProvider,
            Hexbound.id("rendertype_construct_glitchy"),
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP
        )
    ) { ShieldRenderLayer.GLITCHY_SHADER = it }
}
