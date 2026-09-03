package coffee.cypher.hexbound.feature.construct.command

import coffee.cypher.hexbound.feature.construct.command.execution.ConstructCommandContext
import coffee.cypher.hexbound.init.HexboundData
import coffee.cypher.kettle.scheduler.TaskContext
import com.mojang.serialization.Codec
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel

interface ConstructCommand<C : ConstructCommand<C>> {
    fun getType(): Type<C>

    fun display(world: ServerLevel): Component

    suspend fun TaskContext<out ConstructCommandContext>.execute()

    data class Type<C : ConstructCommand<C>>(
        val codec: Codec<C>
    )
}

class NoOpCommand : ConstructCommand<NoOpCommand> {
    override fun getType() = HexboundData.ConstructCommandTypes.NO_OP.get()

    override fun display(world: ServerLevel): Component {
        return Component.translatable("hexbound.construct.command.no_op")
    }

    override suspend fun TaskContext<out ConstructCommandContext>.execute() {
    }
}
