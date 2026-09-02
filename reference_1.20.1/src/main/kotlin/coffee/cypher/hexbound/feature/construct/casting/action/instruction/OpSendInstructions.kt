package coffee.cypher.hexbound.feature.construct.casting.action.instruction

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import coffee.cypher.hexbound.feature.construct.mishap.MishapConstructForbidden
import coffee.cypher.hexbound.util.getConstruct

object OpSendInstructions : SpellAction {
    override val argc = 2

    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): SpellAction.Result {
        val construct = args.getConstruct(0, argc)
        val instructions = args.getList(1, argc)

        ctx.assertEntityInRange(construct)

        if (!construct.isPlayerAllowed(ctx.caster)) {
            throw MishapConstructForbidden(construct)
        }

        return SpellAction.Result(
            Spell(construct, instructions.toList()),
            0,
            emptyList()
        )
    }

    private class Spell(
        val constructEntity: AbstractConstructEntity,
        val instructions: List<Iota>
    ) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            constructEntity.acceptInstructions(instructions, ctx.caster, false, null)
        }
    }
}
