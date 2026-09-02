package coffee.cypher.hexbound.feature.construct.broadcasting

import at.petrak.hexcasting.common.lib.HexBlockEntities
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties.HORIZONTAL_FACING
import net.minecraft.state.property.Properties.POWERED
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.random.RandomGenerator
import net.minecraft.world.World
import org.quiltmc.qkl.library.math.plus
import kotlin.jvm.optionals.getOrNull

@Suppress("OVERRIDE_DEPRECATION")
object ConstructBroadcasterBlock : Block(
    Settings.copy(Blocks.DEEPSLATE).strength(4f, 4f)
) {
    const val broadcastRadius = 16.0

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(HORIZONTAL_FACING).add(POWERED)
    }

    init {
        defaultState = stateManager.defaultState
            .with(HORIZONTAL_FACING, Direction.NORTH)
            .with(POWERED, false)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(HORIZONTAL_FACING, ctx.playerFacing.opposite)
    }

    fun createBroadcastingContext(world: World, blockState: BlockState, pos: BlockPos): BroadcastingContext {
        val slatePos = pos + blockState[HORIZONTAL_FACING].vector
        val pattern = world.getBlockEntity(slatePos, HexBlockEntities.SLATE_TILE).getOrNull()?.pattern

        return BroadcastingContext(
            pos,
            Vec3d.ofCenter(pos),
            broadcastRadius,
            pattern,
            Vec3d.ofCenter(pos, 0.8125),
            0.3125
        )
    }

    fun onActivated(world: World, pos: BlockPos) {
        val state = world.getBlockState(pos)
        if (!state[POWERED]) {
            world.setBlockState(pos, state.with(POWERED, true))
            world.scheduleBlockTick(pos, this, 10)
        }

        world.updateNeighbors(pos, this)
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(HORIZONTAL_FACING, rotation.rotate(state[HORIZONTAL_FACING]))
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.with(HORIZONTAL_FACING, mirror.apply(state[HORIZONTAL_FACING]))
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: RandomGenerator) {
        if (state[POWERED]) {
            world.setBlockState(pos, state.with(POWERED, false))

            world.updateNeighbors(pos, this)
        }
    }
}
