package coffee.cypher.hexbound.feature.construct.command

import coffee.cypher.hexbound.feature.construct.command.exception.BadTargetConstructCommandException
import coffee.cypher.hexbound.feature.construct.command.execution.ConstructCommandContext
import coffee.cypher.hexbound.feature.construct.entity.component.InteractionComponent
import coffee.cypher.hexbound.feature.construct.entity.component.ItemHolderComponent
import coffee.cypher.hexbound.init.HexboundData
import coffee.cypher.hexbound.util.formatVector
import coffee.cypher.hexbound.util.localizeSide
import coffee.cypher.kettle.scheduler.TaskContext
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockState

class Harvest(
    val target: BlockPos
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
                    world.setBlock(target, harvest.replantState, 3)

                    val seed = state.block.asItem()
                    val dropped = Block.getDrops(state, world, target, null)

                    dropped.firstOrNull { it.`is`(seed) }?.let { it.count-- }
                    dropped.forEach { Block.popResource(world, target, it) }

                    world.playSound(null, target, harvest.sound, SoundSource.BLOCKS, 1f, 1f)
                }
            }
        }
    }

    override fun display(world: ServerLevel): Component {
        return Component.translatable("hexbound.construct.command.harvest", formatVector(target))
    }

    companion object {
        fun getHarvestingResult(picker: Entity, blockState: BlockState): HarvestingResult {
            val block = blockState.block

            if (block is CropBlock) {
                if (!block.isMaxAge(blockState)) {
                    return HarvestingResult.NotReady
                }

                return HarvestingResult.StandardHarvest(block.getStateForAge(0), SoundEvents.CROP_BREAK)
            }

            if (block is CocoaBlock) {
                return validateAge(blockState, CocoaBlock.AGE, CocoaBlock.MAX_AGE, SoundEvents.WOOD_PLACE)
            }
            if (block is SweetBerryBushBlock) {
                return validateAge(blockState, SweetBerryBushBlock.AGE, SweetBerryBushBlock.MAX_AGE, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES)
            }
            if (block is NetherWartBlock) {
                return validateAge(blockState, NetherWartBlock.AGE, NetherWartBlock.MAX_AGE, SoundEvents.NETHER_WART_PLANTED)
            }

            if (block is CaveVines) {
                if (blockState.getValue(CaveVines.BERRIES) == false) {
                    return HarvestingResult.NotReady
                }

                return HarvestingResult.BuiltinHarvest { state, world, pos ->
                    CaveVines.use(picker, state, world, pos)
                }
            }

            return HarvestingResult.NotHarvestable
        }

        fun validateAge(state: BlockState, age: IntegerProperty, maxAge: Int, sound: SoundEvent): HarvestingResult {
            if (state.getValue(age) < maxAge) {
                return HarvestingResult.NotReady
            }

            return HarvestingResult.StandardHarvest(state.setValue(age, 0), sound)
        }
    }

    sealed class HarvestingResult {
        object NotHarvestable : HarvestingResult()
        object NotReady : HarvestingResult()
        class StandardHarvest(val replantState: BlockState, val sound: SoundEvent) : HarvestingResult()
        class BuiltinHarvest(val harvest: (BlockState, Level, BlockPos) -> Unit) : HarvestingResult()
    }
}

class UseItemOnBlock(
    val target: BlockPos,
    val side: Direction
) : ConstructCommand<UseItemOnBlock> {
    override fun getType() = HexboundData.ConstructCommandTypes.USE_ON_BLOCK

    override suspend fun TaskContext<out ConstructCommandContext>.execute() {
        withContext {
            val player = requireComponent(InteractionComponent).getInteractionPlayer(world)
            val itemHolder = requireComponent(ItemHolderComponent)

            player.setItemInHand(InteractionHand.MAIN_HAND, itemHolder.heldStack)
            prepareToInteract(player, target)

            val targetVec = Vec3.atCenterOf(target).add(Vec3.atLowerCornerOf(side.normal).scale(0.5))
            val blockHit = BlockHitResult(targetVec, side, target, false)
            val result = player.gameMode.useItemOn(player, world, itemHolder.heldStack, InteractionHand.MAIN_HAND, blockHit)

            if (result == InteractionResult.FAIL) {
                //TODO throw maybe
            }

            (0 until player.inventory.containerSize).forEach {
                val invStack = player.inventory.getItem(it)
                if (invStack !== itemHolder.heldStack) {
                    world.addFreshEntity(ItemEntity(world, construct.x, construct.y, construct.z, invStack.copy()))
                    player.inventory.setItem(it, ItemStack.EMPTY)
                }
            }
        }
    }

    override fun display(world: ServerLevel): Component {
        return Component.translatable(
            "hexbound.construct.command.use_on_block",
            formatVector(target),
            localizeSide(side)
        )
    }

}

fun ConstructCommandContext.prepareToInteract(player: ServerPlayer, target: BlockPos) {
    val targetCenter = Vec3.atCenterOf(target)

    if (construct.position().distanceToSqr(targetCenter) > 6.25) {
        throw BadTargetConstructCommandException(target, "too_far")
    }

    if (!world.mayInteract(player, target)) {
        throw BadTargetConstructCommandException(target, "forbidden")
    }

    construct.lookAt(EntityAnchorArgument.Anchor.EYES, targetCenter)
    construct.yBodyRot = construct.yHeadRot
}
