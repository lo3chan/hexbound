package coffee.cypher.hexbound.feature.construct.command

import coffee.cypher.hexbound.feature.construct.command.exception.BadTargetConstructCommandException
import coffee.cypher.hexbound.feature.construct.command.execution.ConstructCommandContext
import coffee.cypher.hexbound.feature.construct.entity.component.InteractionComponent
import coffee.cypher.hexbound.feature.construct.entity.component.ItemHolderComponent
import coffee.cypher.hexbound.init.HexboundData
import coffee.cypher.hexbound.util.formatVector
import coffee.cypher.hexbound.util.localizeSide
import coffee.cypher.hexbound.util.times
import coffee.cypher.kettle.inventory.get
import coffee.cypher.kettle.inventory.set
import coffee.cypher.kettle.math.toDoubleVector
import coffee.cypher.kettle.scheduler.TaskContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.block.*
import net.minecraft.command.argument.EntityAnchorArgumentType
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.state.property.Property
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.quiltmc.qkl.library.math.plus

@Serializable
class Harvest(
    @Contextual val target: BlockPos
) : ConstructCommand<Harvest> {
    override fun getType() = HexboundData.ConstructCommandTypes.HARVEST

    override suspend fun TaskContext<out ConstructCommandContext>.execute() {
        withContext {
            val state = world.getBlockState(target)
            val player = requireComponent(InteractionComponent).getInteractionPlayer(world)
            prepareToInteract(player, target)

            when (val harvest = getHarvestingResult(construct, state)) {
                is HarvestingResult.NotHarvestable -> throw BadTargetConstructCommandException(
                    target,
                    "not_harvestable"
                )

                is HarvestingResult.NotReady -> {}
                is HarvestingResult.BuiltinHarvest -> harvest.harvest(state, world, target)
                is HarvestingResult.StandardHarvest -> {
                    world.setBlockState(target, harvest.replantState)

                    val seed = state.block.getPickStack(world, target, state).item
                    val dropped = Block.getDroppedStacks(state, world, target, null)

                    dropped.firstOrNull { it.isOf(seed) }?.let { it.count-- }
                    dropped.forEach { Block.dropStack(world, target, it) }

                    state.onStacksDropped(world, target, ItemStack.EMPTY, false)

                    world.playSound(null, target, harvest.sound, SoundCategory.BLOCKS, 1f, 1f)
                }
            }
        }
    }

    override fun display(world: ServerWorld): Text {
        return Text.translatable("hexbound.construct.command.harvest", formatVector(target))
    }

    companion object {
        fun getHarvestingResult(picker: Entity, blockState: BlockState): HarvestingResult {
            val block = blockState.block

            if (block is CropBlock) {
                if (!block.isMature(blockState)) {
                    return HarvestingResult.NotReady
                }

                return HarvestingResult.StandardHarvest(block.withAge(0), SoundEvents.ITEM_CROP_PLANT)
            }

            if (block is CocoaBlock) {
                return validateAge(blockState, CocoaBlock.AGE, CocoaBlock.MAX_AGE, SoundEvents.BLOCK_WOOD_PLACE)
            }
            if (block is SweetBerryBushBlock) {
                return validateAge(blockState, SweetBerryBushBlock.AGE, SweetBerryBushBlock.MAX_AGE, SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES)
            }
            if (block is NetherWartBlock) {
                return validateAge(blockState, NetherWartBlock.AGE, NetherWartBlock.MAX_AGE, SoundEvents.ITEM_NETHER_WART_PLANT)
            }

            if (block is CaveVines) {
                if (!blockState[CaveVines.BERRIES]) {
                    return HarvestingResult.NotReady
                }

                return HarvestingResult.BuiltinHarvest { state, world, pos ->
                    CaveVines.pickBerries(picker, state, world, pos)
                }
            }

            return HarvestingResult.NotHarvestable
        }

        fun validateAge(state: BlockState, age: Property<Int>, maxAge: Int, sound: SoundEvent): HarvestingResult {
            if (state[age] < maxAge) {
                return HarvestingResult.NotReady
            }

            return HarvestingResult.StandardHarvest(state.with(age, 0), sound)
        }
    }

    sealed class HarvestingResult {
        data object NotHarvestable : HarvestingResult()
        data object NotReady : HarvestingResult()
        data class StandardHarvest(val replantState: BlockState, val sound: SoundEvent) : HarvestingResult()
        data class BuiltinHarvest(val harvest: (BlockState, World, BlockPos) -> Unit) : HarvestingResult()
    }
}

@Serializable
class UseItemOnBlock(
    @Contextual val target: BlockPos,
    val side: Direction
) : ConstructCommand<UseItemOnBlock> {
    override fun getType() = HexboundData.ConstructCommandTypes.USE_ON_BLOCK

    override suspend fun TaskContext<out ConstructCommandContext>.execute() {
        withContext {
            val player = requireComponent(InteractionComponent).getInteractionPlayer(world)
            val itemHolder = requireComponent(ItemHolderComponent)

            player.setStackInHand(Hand.MAIN_HAND, itemHolder.heldStack)
            prepareToInteract(player, target)

            //TODO consider actually raycasting
            val blockHit = BlockHitResult(
                Vec3d(0.5, 0.5, 0.5) + side.vector.toDoubleVector() * 0.5,
                side,
                target,
                false
            )

            val result = world.server.getPlayerInteractionManager(player)
                .interactBlock(player, world, itemHolder.heldStack, Hand.MAIN_HAND, blockHit)

            if (result == ActionResult.FAIL) {
                //TODO throw maybe
                0 - 0
            }

            (0 until player.inventory.size()).forEach {
                val invStack = player.inventory[it]
                if (invStack !== itemHolder.heldStack) {
                    world.spawnEntity(ItemEntity(world, construct.x, construct.y, construct.z, invStack.copy()))
                    player.inventory[it] = ItemStack.EMPTY
                }
            }
        }
    }

    override fun display(world: ServerWorld): Text {
        return Text.translatable(
            "hexbound.construct.command.use_on_block",
            formatVector(target),
            localizeSide(side)
        )
    }

}

fun ConstructCommandContext.prepareToInteract(player: ServerPlayerEntity, target: BlockPos) {
    val targetCenter = Vec3d.ofCenter(target)

    if (construct.pos.squaredDistanceTo(targetCenter) > 6.25) {
        throw BadTargetConstructCommandException(target, "too_far")
    }

    if (!world.canPlayerModifyAt(player, target)) {
        throw BadTargetConstructCommandException(target, "forbidden")
    }

    construct.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, targetCenter)
    construct.setBodyYaw(construct.getHeadYaw())
}
