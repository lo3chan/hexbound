package coffee.cypher.hexbound.feature.construct.command

import coffee.cypher.hexbound.feature.construct.command.exception.BadTargetConstructCommandException
import coffee.cypher.hexbound.feature.construct.command.exception.ConstructCommandException
import coffee.cypher.hexbound.feature.construct.command.execution.ConstructCommandContext
import coffee.cypher.hexbound.feature.construct.entity.component.ItemHolderComponent
import coffee.cypher.hexbound.init.HexboundData
import coffee.cypher.kettle.scheduler.TaskContext
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import java.util.*

class PickUp(
    val targetUuid: UUID
) : ConstructCommand<PickUp> {
    override fun getType() = HexboundData.ConstructCommandTypes.PICK_UP.get()

    override suspend fun TaskContext<out ConstructCommandContext>.execute() {
        withContext {
            val target = world.getEntity(targetUuid) ?: throw BadTargetConstructCommandException("does_not_exist")

            if (construct.distanceToSqr(target) > 6.25) {
                throw BadTargetConstructCommandException(target, "too_far")
            }

            val itemHolder = requireComponent(ItemHolderComponent)

            if (!itemHolder.heldStack.isEmpty) {
                throw ConstructCommandException(
                    Component.translatable(
                        "hexbound.construct.exception.already_has_item",
                        itemHolder.heldStack.hoverName
                    )
                )
            }

            if (target !is ItemEntity) {
                throw BadTargetConstructCommandException(target, "not_an_item")
            }

            if (!target.isAlive) {
                throw BadTargetConstructCommandException(target, "target_expired")
            }

            itemHolder.heldStack = target.item.copy()
            target.discard()
        }
    }

    override fun display(world: ServerLevel): Component {
        val target = world.getEntity(targetUuid)?.displayName
                     ?: Component.translatable("hexbound.construct.command.unknown_item")

        return Component.translatable("hexbound.construct.command.pick_up", target)
    }
}

class DropOff : ConstructCommand<DropOff> {
    override fun getType() = HexboundData.ConstructCommandTypes.DROP_OFF.get()

    override suspend fun TaskContext<out ConstructCommandContext>.execute() {
        withContext {
            val itemHolder = requireComponent(ItemHolderComponent)

            if (itemHolder.heldStack.isEmpty) {
                throw ConstructCommandException(Component.translatable("hexbound.construct.exception.no_item"))
            }

            construct.level().addFreshEntity(
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

    override fun display(world: ServerLevel): Component {
        return Component.translatable("hexbound.construct.command.drop_off")
    }
}
