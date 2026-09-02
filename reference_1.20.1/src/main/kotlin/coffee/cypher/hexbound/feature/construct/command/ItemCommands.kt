package coffee.cypher.hexbound.feature.construct.command

import coffee.cypher.hexbound.feature.construct.command.exception.BadTargetConstructCommandException
import coffee.cypher.hexbound.feature.construct.command.exception.ConstructCommandException
import coffee.cypher.hexbound.feature.construct.command.execution.ConstructCommandContext
import coffee.cypher.hexbound.feature.construct.entity.component.ItemHolderComponent
import coffee.cypher.hexbound.init.HexboundData
import coffee.cypher.kettle.scheduler.TaskContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import java.util.*

@Serializable
class PickUp(
    @Contextual val targetUuid: UUID
) : ConstructCommand<PickUp> {
    override fun getType() = HexboundData.ConstructCommandTypes.PICK_UP

    override suspend fun TaskContext<out ConstructCommandContext>.execute() {
        withContext {
            val target = world.getEntity(targetUuid) ?: throw BadTargetConstructCommandException("does_not_exist")

            if (construct.squaredDistanceTo(target) > 6.25) {
                throw BadTargetConstructCommandException(target, "too_far")
            }

            val itemHolder = requireComponent(ItemHolderComponent)

            if (!itemHolder.heldStack.isEmpty) {
                throw ConstructCommandException(
                    Text.translatable(
                        "hexbound.construct.exception.already_has_item",
                        itemHolder.heldStack.name
                    )
                )
            }

            if (target !is ItemEntity) {
                throw BadTargetConstructCommandException(target, "not_an_item")
            }

            if (!target.isAlive) {
                throw BadTargetConstructCommandException(target, "target_expired")
            }

            itemHolder.heldStack = target.stack.copy()
            target.discard()
        }
    }

    override fun display(world: ServerWorld): Text {
        val target = world.getEntity(targetUuid)?.name
                     ?: Text.translatable("hexbound.construct.command.unknown_item")

        return Text.translatable("hexbound.construct.command.pick_up", target)
    }
}

@Serializable
class DropOff : ConstructCommand<DropOff> {
    override fun getType() = HexboundData.ConstructCommandTypes.DROP_OFF

    override suspend fun TaskContext<out ConstructCommandContext>.execute() {
        withContext {
            val itemHolder = requireComponent(ItemHolderComponent)

            if (itemHolder.heldStack.isEmpty) {
                throw ConstructCommandException(Text.translatable("hexbound.construct.exception.no_item"))
            }

            construct.world.spawnEntity(
                ItemEntity(
                    world,
                    construct.x,
                    construct.y,
                    construct.z,
                    itemHolder.heldStack.copy(),
                    0.0,
                    0.0,
                    0.0
                )
            )
            itemHolder.heldStack = ItemStack.EMPTY
        }
    }

    override fun display(world: ServerWorld): Text {
        return Text.translatable("hexbound.construct.command.drop_off")
    }
}
