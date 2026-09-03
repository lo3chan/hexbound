package coffee.cypher.hexbound.feature.colorizer_storage.mishap

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapMissingColorizerKey(private val pattern: HexPattern) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment {
        return dyeColor(DyeColor.WHITE)
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component {
        return error("colorizer.missing_key", pattern.anglesSignature())
    }

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        ctx.caster?.let {
            IXplatAbstractions.INSTANCE.setPigment(it, FrozenPigment.DEFAULT.get())
        }
    }
}
