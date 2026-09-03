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
    // Unused core shaders bypassed; native double-sided translucent entity rendering is used instead
}
