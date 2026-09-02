package coffee.cypher.hexbound.feature.construct.broadcasting

import at.petrak.hexcasting.common.lib.HexBlockEntities
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING
import net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.util.RandomSource
import kotlin.jvm.optionals.getOrNull

@Suppress("OVERRIDE_DEPRECATION")
class ConstructBroadcasterBlock(properties: Properties) : Block(properties) {
    companion object {
        const val broadcastRadius = 16.0
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(HORIZONTAL_FACING, POWERED)
    }

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(HORIZONTAL_FACING, Direction.NORTH)
            .setValue(POWERED, false))
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        return defaultBlockState().setValue(HORIZONTAL_FACING, ctx.horizontalDirection.opposite)
    }

    fun createBroadcastingContext(world: Level, blockState: BlockState, pos: BlockPos): BroadcastingContext {
        val slatePos = pos.relative(blockState.getValue(HORIZONTAL_FACING))
        val pattern = world.getBlockEntity(slatePos, HexBlockEntities.SLATE_TILE).getOrNull()?.pattern

        return BroadcastingContext(
            pos,
            Vec3.atCenterOf(pos),
            broadcastRadius,
            pattern,
            Vec3.atCenterOf(pos).add(0.0, 0.3125, 0.0), // Approximate 0.8125 offset
            0.3125
        )
    }

    fun onActivated(world: Level, pos: BlockPos) {
        val state = world.getBlockState(pos)
        if (!state.getValue(POWERED)) {
            world.setBlock(pos, state.setValue(POWERED, true), 3)
            world.scheduleTick(pos, this, 10)
        }

        world.updateNeighborsAt(pos, this)
    }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)))
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        return state.setValue(HORIZONTAL_FACING, mirror.mirror(state.getValue(HORIZONTAL_FACING)))
    }

    override fun tick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (state.getValue(POWERED)) {
            world.setBlock(pos, state.setValue(POWERED, false), 3)

            world.updateNeighborsAt(pos, this)
        }
    }
}
