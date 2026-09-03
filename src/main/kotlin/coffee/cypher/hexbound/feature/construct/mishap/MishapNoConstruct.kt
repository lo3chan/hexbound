package coffee.cypher.hexbound.feature.construct.mishap

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.neoforged.neoforge.common.util.FakePlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapNoConstruct : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment {
        return dyeColor(DyeColor.PURPLE)
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component {
        return error("no_construct", actionName(errorCtx.name))
    }

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        if (ctx.caster !is FakePlayer) {
            ctx.caster?.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2))
        }
    }
}
