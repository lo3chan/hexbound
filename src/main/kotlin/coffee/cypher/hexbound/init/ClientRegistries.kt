package coffee.cypher.hexbound.init

import coffee.cypher.hexbound.feature.combat.shield.ShieldRenderLayer
import coffee.cypher.hexbound.feature.combat.shield.ShieldRenderer
import coffee.cypher.hexbound.feature.construct.rendering.SpiderConstructRenderer
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.client.renderer.ShaderInstance
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.RegisterShadersEvent

fun initEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
    event.registerEntityRenderer(HexboundData.EntityTypes.SPIDER_CONSTRUCT.get(), ::SpiderConstructRenderer)
    event.registerEntityRenderer(HexboundData.EntityTypes.SHIELD.get(), ::ShieldRenderer)
}

fun initShaders(event: RegisterShadersEvent) {
    try {
        event.registerShader(
            ShaderInstance(
                event.resourceProvider,
                "hexbound__shield",
                DefaultVertexFormat.NEW_ENTITY
            )
        ) { ShieldRenderLayer.REGULAR_SHADER = it }
    } catch (e: Exception) {
        Hexbound.LOGGER.error("Failed to register hexbound__shield shader", e)
    }

    try {
        event.registerShader(
            ShaderInstance(
                event.resourceProvider,
                "hexbound__shield_glitchy",
                DefaultVertexFormat.NEW_ENTITY
            )
        ) { ShieldRenderLayer.GLITCHY_SHADER = it }
    } catch (e: Exception) {
        Hexbound.LOGGER.error("Failed to register hexbound__shield_glitchy shader", e)
    }
}
