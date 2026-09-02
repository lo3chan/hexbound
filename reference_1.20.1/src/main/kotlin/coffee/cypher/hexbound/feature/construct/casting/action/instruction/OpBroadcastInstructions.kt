package coffee.cypher.hexbound.feature.construct.casting.action.instruction

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock
import coffee.cypher.hexbound.feature.construct.broadcasting.BroadcastingContext
import coffee.cypher.hexbound.feature.construct.broadcasting.ConstructBroadcasterBlock
import coffee.cypher.hexbound.init.HexboundData
import coffee.cypher.kettle.math.toDoubleVector

object OpBroadcastInstructions : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        val broadcasterPos = args.getBlockPos(0, OpSendInstructions.argc)
        val instructions = args.getList(1, OpSendInstructions.argc)

        ctx.assertVecInRange(broadcasterPos.toDoubleVector())

        val broadcasterState = ctx.world.getBlockState(broadcasterPos)

        if (!broadcasterState.isOf(HexboundData.Blocks.CONSTRUCT_BROADCASTER)) {
            throw MishapBadBlock.of(broadcasterPos, "construct_broadcaster")
        }

        return SpellAction.Result(
            Spell(
                ConstructBroadcasterBlock.createBroadcastingContext(ctx.world, broadcasterState, broadcasterPos),
                instructions.toList()
            ),
            0,
            emptyList()
        )
    }

    private class Spell(val context: BroadcastingContext, val instructions: List<Iota>) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            context.broadcast(instructions, ctx)
        }
    }
}
