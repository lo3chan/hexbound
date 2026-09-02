package coffee.cypher.hexbound.util.rendering

import com.mojang.blaze3d.shaders.Uniform
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.server.packs.resources.ResourceProvider
import net.minecraft.resources.ResourceLocation

class TimedShaderProgram(
    factory: ResourceProvider,
    name: ResourceLocation,
    format: VertexFormat
) : ShaderInstance(factory, name, format) {
    val timeUniform: Uniform? = getUniform("Time")
}
