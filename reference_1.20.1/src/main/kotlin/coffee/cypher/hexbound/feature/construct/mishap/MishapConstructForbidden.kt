package coffee.cypher.hexbound.feature.construct.mishap

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import org.quiltmc.qkl.library.math.minus

class MishapConstructForbidden(val construct: AbstractConstructEntity) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment {
        return dyeColor(DyeColor.BROWN)
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Text {
        return error("construct_forbidden", construct.displayName, construct.boundPlayerData?.displayName ?: "")
    }

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        val caster = ctx.caster ?: return

        val directionVec = caster.pos - construct.pos
        val movementVec = directionVec.normalize()
        caster.addVelocity(movementVec.x, movementVec.y, movementVec.z)
        caster.velocityModified = true
    }
}
