package coffee.cypher.hexbound.util.rendering

import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.render.ShaderProgram
import net.minecraft.resource.ResourceFactory

class TimedShaderProgram(factory: ResourceFactory, name: String, format: VertexFormat) :
    ShaderProgram(factory, name, format)
{
        val worldTime = getUniform("Hexbound_WorldTime")
}
