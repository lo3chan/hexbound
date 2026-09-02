package coffee.cypher.hexbound.feature.construct.mishap

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import coffee.cypher.hexbound.feature.construct.entity.component.ConstructComponentKey
import net.minecraft.text.Text
import net.minecraft.util.DyeColor

class MishapMissingConstructComponent(
    val construct: AbstractConstructEntity,
    val key: ConstructComponentKey<*>
) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment {
        return dyeColor(DyeColor.YELLOW)
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Text {
        return Text.translatable("hexbound.construct.exception.component_missing.${key.key}", construct.displayName)
    }

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
    }
}
