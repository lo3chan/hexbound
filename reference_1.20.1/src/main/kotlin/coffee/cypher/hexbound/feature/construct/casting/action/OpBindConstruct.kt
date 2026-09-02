package coffee.cypher.hexbound.feature.construct.casting.action

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import coffee.cypher.hexbound.feature.construct.mishap.MishapConstructForbidden
import coffee.cypher.hexbound.util.MemorizedPlayerData
import coffee.cypher.hexbound.util.getConstruct
import net.minecraft.entity.player.PlayerEntity

object OpBindConstruct : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        val construct = args.getConstruct(0, argc)
        val req = args[1]

        if (!construct.isPlayerAllowed(ctx.caster)) {
            throw MishapConstructForbidden(construct)
        }

        when (req) {
            is PatternIota -> {
                return SpellAction.Result(PatternSpell(construct, req.pattern), 0, emptyList())
            }
            is EntityIota -> {
                if (req.entity is PlayerEntity) {
                    return SpellAction.Result(PlayerSpell(construct, MemorizedPlayerData.forPlayer(req.entity as PlayerEntity)), 0, emptyList())
                }
            }
            is NullIota -> {
                return SpellAction.Result(UnbindSpell(construct), 0, emptyList())
            }
        }

        throw MishapInvalidIota.of(req, 1, "bindable")
    }

    private class PatternSpell(val construct: AbstractConstructEntity, val pattern: HexPattern) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            construct.boundPattern = pattern
        }
    }

    private class PlayerSpell(val construct: AbstractConstructEntity, val playerData: MemorizedPlayerData) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            construct.boundPlayerData = playerData

        }
    }

    private class UnbindSpell(val construct: AbstractConstructEntity) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            construct.boundPattern = null
            construct.boundPlayerData = null
        }
    }
}
