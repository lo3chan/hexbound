package coffee.cypher.hexbound.feature.construct.command

import coffee.cypher.hexbound.feature.construct.command.execution.ConstructCommandContext
import coffee.cypher.hexbound.init.HexboundData
import coffee.cypher.kettle.scheduler.TaskContext
import com.mojang.serialization.Codec
import kotlinx.serialization.Serializable
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text

interface ConstructCommand<C : ConstructCommand<C>> {
    fun getType(): Type<C>

    fun display(world: ServerWorld): Text

    suspend fun TaskContext<out ConstructCommandContext>.execute()

    data class Type<C : ConstructCommand<C>>(
        val codec: Codec<C>
    )
}

@Serializable
class NoOpCommand : ConstructCommand<NoOpCommand> {
    override fun getType() = HexboundData.ConstructCommandTypes.NO_OP

    override fun display(world: ServerWorld): Text {
        return Text.translatable("hexbound.construct.command.no_op")
    }

    override suspend fun TaskContext<out ConstructCommandContext>.execute() {
    }
}
