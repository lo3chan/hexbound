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
import net.minecraft.command.argument.EntityAnchorArgumentType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.passive.AllayEntity
import org.quiltmc.qkl.library.math.component1
import org.quiltmc.qkl.library.math.component2
import org.quiltmc.qkl.library.math.component3

object OpCreateSpiderConstruct : SpellAction {
    override val argc = 3

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        val allay = args.getAllay(0, argc)
        val coreStack = args.getItemEntity(1, argc)
        val batteryStack = args.getItemEntity(2, argc)

        ctx.assertEntityInRange(allay)
        ctx.assertEntityInRange(coreStack)
        ctx.assertEntityInRange(batteryStack)

        if (coreStack.stack.isEmpty || !coreStack.stack.isOf(SPIDER_CONSTRUCT_CORE)) {
            throw MishapInvalidIota.of(args[1], 1, "spider_component.core")
        }

        if (
            batteryStack.stack.isEmpty ||
            !batteryStack.stack.isOf(SPIDER_CONSTRUCT_BATTERY) ||
            !SpiderConstructBatteryItem.isFullyCharged(batteryStack.stack)
        ) {
            throw MishapInvalidIota.of(args[2], 0, "spider_component.battery")
        }

        return SpellAction.Result(
            Spell(allay, coreStack, batteryStack),
            5 * MediaConstants.CRYSTAL_UNIT,
            listOf(ParticleSpray.cloud(coreStack.pos, 1.0))
        )
    }

    private class Spell(val allay: AllayEntity, val coreStack: ItemEntity, val batteryStack: ItemEntity) :
        RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            coreStack.stack.decrement(1)
            if (coreStack.stack.isEmpty) {
                coreStack.kill()
            }

            batteryStack.stack.decrement(1)
            if (batteryStack.stack.isEmpty) {
                coreStack.kill()
            }

            val (x, y, z) = allay.pos

            allay.discard()

            val construct = HexboundData.EntityTypes.SPIDER_CONSTRUCT.create(ctx.world)

            if (construct == null) {
                Hexbound.LOGGER.error(
                    "Failed to summon Spider Construct at {}, {}, {}",
                    coreStack.x,
                    coreStack.y,
                    coreStack.z
                )

                return
            }

            construct.setPosition(x, y + 0.25, z)
            construct.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, ctx.mishapSprayPos())
            ctx.world.spawnEntity(construct)
        }

    }
}
