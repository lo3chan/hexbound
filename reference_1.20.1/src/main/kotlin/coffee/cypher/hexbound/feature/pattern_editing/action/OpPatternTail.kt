package coffee.cypher.hexbound.feature.pattern_editing.action

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPattern
import at.petrak.hexcasting.api.casting.getPositiveIntUnderInclusive
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota

object OpPatternTail : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val pattern = args.getPattern(0, OpPatternHead.argc)
        val lineCount = args.getPositiveIntUnderInclusive(1, pattern.angles.size + 1, OpPatternHead.argc)

        if (lineCount == 0) {
            throw MishapInvalidIota.of(args[1], 0, "int.positive.less.equal", pattern.angles.size + 1)
        }

        val angleCount = lineCount - 1

        val newStartDir = pattern.angles.dropLast(angleCount).fold(pattern.startDir) { dir, angle -> dir.rotatedBy(angle) }

        val newPattern = HexPattern(newStartDir, pattern.angles.takeLast(angleCount).toMutableList())
        return listOf(PatternIota(newPattern))
    }
}
