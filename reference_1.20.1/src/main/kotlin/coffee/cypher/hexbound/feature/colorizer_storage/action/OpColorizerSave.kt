package coffee.cypher.hexbound.feature.colorizer_storage.action

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPattern
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents
import coffee.cypher.hexbound.feature.colorizer_storage.mishap.MishapColorizerNotSet
import coffee.cypher.hexbound.feature.colorizer_storage.mishap.MishapTooManyColorizers
import coffee.cypher.hexbound.init.memorizedColorizers
import coffee.cypher.hexbound.util.nonBlankSignature
import coffee.cypher.hexbound.util.requireCaster
import net.minecraft.server.network.ServerPlayerEntity

object OpColorizerSave : SpellAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): SpellAction.Result {
        val pattern = args.getPattern(0, 1)
        val caster = ctx.requireCaster()

        val currentColorizer = HexCardinalComponents.FAVORED_PIGMENT[caster].pigment

        if (caster.memorizedColorizers.size >= 64 && pattern.nonBlankSignature !in caster.memorizedColorizers) {
            throw MishapTooManyColorizers()
        }

        if (currentColorizer == FrozenPigment.DEFAULT.get()) {
            throw MishapColorizerNotSet()
        }

        return SpellAction.Result(
            Spell(pattern, currentColorizer, caster),
            0,
            emptyList()
        )
    }

    private data class Spell(
        val key: HexPattern,
        val value: FrozenPigment,
        val caster: ServerPlayerEntity
    ) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            caster.memorizedColorizers[key.nonBlankSignature] = value
        }
    }
}
