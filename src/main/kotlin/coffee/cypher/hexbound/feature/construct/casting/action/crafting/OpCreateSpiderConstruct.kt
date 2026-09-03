package coffee.cypher.hexbound.feature.construct.casting.action.crafting

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getItemEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import coffee.cypher.hexbound.feature.construct.item.SpiderConstructBatteryItem
import coffee.cypher.hexbound.init.Hexbound
import coffee.cypher.hexbound.init.HexboundData
import coffee.cypher.hexbound.init.HexboundData.Items.SPIDER_CONSTRUCT_BATTERY
import coffee.cypher.hexbound.init.HexboundData.Items.SPIDER_CONSTRUCT_CORE
import coffee.cypher.hexbound.util.getAllay
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.world.entity.animal.allay.Allay
import net.minecraft.world.entity.item.ItemEntity

object OpCreateSpiderConstruct : SpellAction {
    override val argc = 3

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        val allay = args.getAllay(0, argc)
        val coreStack = args.getItemEntity(1, argc)
        val batteryStack = args.getItemEntity(2, argc)

        ctx.assertEntityInRange(allay)
        ctx.assertEntityInRange(coreStack)
        ctx.assertEntityInRange(batteryStack)

        if (coreStack.item.isEmpty || !coreStack.item.`is`(SPIDER_CONSTRUCT_CORE.get())) {
            throw MishapInvalidIota.of(args[1], 1, "spider_component.core")
        }

        if (
            batteryStack.item.isEmpty ||
            !batteryStack.item.`is`(SPIDER_CONSTRUCT_BATTERY.get()) ||
            !SpiderConstructBatteryItem.isFullyCharged(batteryStack.item)
        ) {
            throw MishapInvalidIota.of(args[2], 0, "spider_component.battery")
        }

        return SpellAction.Result(
            Spell(allay, coreStack, batteryStack),
            5 * MediaConstants.CRYSTAL_UNIT,
            listOf(ParticleSpray.cloud(coreStack.position(), 1.0))
        )
    }

    private class Spell(val allay: Allay, val coreStack: ItemEntity, val batteryStack: ItemEntity) :
        RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            coreStack.item.shrink(1)
            if (coreStack.item.isEmpty) {
                coreStack.discard()
            }

            batteryStack.item.shrink(1)
            if (batteryStack.item.isEmpty) {
                batteryStack.discard()
            }

            val pos = allay.position()
            allay.discard()

            val construct = HexboundData.EntityTypes.SPIDER_CONSTRUCT.get().create(ctx.world)

            if (construct == null) {
                Hexbound.LOGGER.error(
                    "Failed to summon Spider Construct at {}, {}, {}",
                    coreStack.x,
                    coreStack.y,
                    coreStack.z
                )

                return
            }

            construct.setPos(pos.x, pos.y + 0.25, pos.z)
            construct.lookAt(EntityAnchorArgument.Anchor.EYES, ctx.mishapSprayPos())
            ctx.world.addFreshEntity(construct)
        }
    }
}
