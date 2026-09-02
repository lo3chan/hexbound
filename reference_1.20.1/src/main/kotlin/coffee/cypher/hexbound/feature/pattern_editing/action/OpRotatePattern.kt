package coffee.cypher.hexbound.feature.pattern_editing.action

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.getPattern
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern

object OpRotatePattern : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val pattern = args.getPattern(0, argc)
        val rotation = args.getInt(1, argc)

        val rawOrdinal = pattern.startDir.ordinal + rotation
        val clampedOrdinal = rawOrdinal - (rawOrdinal / 6) * 6

        return HexPattern(HexDir.values()[clampedOrdinal], pattern.angles).asActionResult
    }
}
