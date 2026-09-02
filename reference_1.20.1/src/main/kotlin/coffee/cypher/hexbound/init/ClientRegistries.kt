package coffee.cypher.hexbound.init

import coffee.cypher.hexbound.feature.combat.shield.ShieldRenderLayer
import coffee.cypher.hexbound.feature.combat.shield.ShieldRenderer
import coffee.cypher.hexbound.feature.construct.rendering.SpiderConstructRenderer
import coffee.cypher.hexbound.util.rendering.TimedShaderProgram
import com.mojang.blaze3d.vertex.VertexFormats
import com.mojang.datafixers.util.Pair
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.ShaderProgram
import net.minecraft.resource.ResourceFactory
import java.util.function.Consumer

fun initClientRegistries() {
    EntityRendererRegistry.register(HexboundData.EntityTypes.SPIDER_CONSTRUCT, ::SpiderConstructRenderer)
    EntityRendererRegistry.register(HexboundData.EntityTypes.SHIELD, ::ShieldRenderer)
}

fun initShaders(factory: ResourceFactory, shaderConsumer: Consumer<Pair<ShaderProgram, Consumer<ShaderProgram>>>) {
    shaderConsumer.accept(
        Pair(
            TimedShaderProgram(
                factory,
                "hexbound__shield",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
            ),
            Consumer { ShieldRenderLayer.REGULAR_SHADER = it }
        )
    )

    shaderConsumer.accept(
        Pair(
            TimedShaderProgram(
                factory,
                "hexbound__shield_glitchy",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
            ),
            Consumer { ShieldRenderLayer.GLITCHY_SHADER = it }
        )
    )
}
