package coffee.cypher.hexbound.feature.pattern_editing.action

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPattern
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern

object OpMergePatterns : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val startPattern = args.getPattern(0, argc)
        val extensionPattern = args.getPattern(1, argc)

        val endDir = startPattern.angles.fold(startPattern.startDir) { dir, angle -> dir.rotatedBy(angle) }
        val joinAngle = extensionPattern.startDir.angleFrom(endDir)

        val angles = (startPattern.angles + joinAngle + extensionPattern.angles).take(1600).toMutableList()

        return HexPattern(
            startPattern.startDir,
            angles
        ).asActionResult
    }
}
