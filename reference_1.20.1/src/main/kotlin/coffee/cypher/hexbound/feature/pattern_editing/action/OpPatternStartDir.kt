package coffee.cypher.hexbound.feature.pattern_editing.action

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPattern
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota

object OpPatternStartDir : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        return listOf(
            DoubleIota(
                args.getPattern(0, argc)
                    .startDir
                    .ordinal
                    .toDouble()
            )
        )
    }
}
