package coffee.cypher.hexbound.feature.construct.mishap

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapConstructForbidden(val construct: AbstractConstructEntity) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment {
        return dyeColor(DyeColor.BROWN)
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component {
        return error("construct_forbidden", construct.displayName, construct.boundPlayerData?.displayName ?: Component.literal(""))
    }

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        val caster = ctx.caster ?: return

        val directionVec = caster.position().subtract(construct.position())
        val movementVec = directionVec.normalize()
        caster.addDeltaMovement(movementVec)
        caster.hasImpulse = true
    }
}
