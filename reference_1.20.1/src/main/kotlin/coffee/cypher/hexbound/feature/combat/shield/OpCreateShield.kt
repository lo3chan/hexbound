package coffee.cypher.hexbound.feature.combat.shield

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPositiveDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import coffee.cypher.hexbound.init.HexboundData
import net.minecraft.command.argument.EntityAnchorArgumentType
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import org.quiltmc.qkl.library.math.minus
import org.quiltmc.qkl.library.math.plus
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.min

class OpCreateShield(val visualType: ShieldEntity.VisualType) : SpellAction {
    override val argc = 3

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        val position = args.getVec3(0, argc)
        val direction = args.getVec3(1, argc).normalize()
        val durationSeconds = args.getPositiveDouble(2, argc)
        val durationTicks = (durationSeconds * 20).toInt()

        val (actualDurationTicks, cost) = if (ctx.caster == null) {
            val cappedDuration = min(durationTicks, 60)
            val cappedDurationSeconds = min(durationSeconds, 3.0)

            cappedDuration to (cappedDurationSeconds * MediaConstants.DUST_UNIT * 4).toInt()
        } else {
            durationTicks to (durationSeconds * MediaConstants.DUST_UNIT * 2).toInt()
        }

        return SpellAction.Result(
            Spell(position, direction, visualType, actualDurationTicks),
            cost,
            listOf(ParticleSpray.burst(position, 3.0))
        )
    }

    private class Spell(
        val position: Vec3d,
        val direction: Vec3d,
        val visualType: ShieldEntity.VisualType,
        val maxAge: Int
    ) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            val shield = ShieldEntity(
                HexboundData.EntityTypes.SHIELD,
                ctx.world,
                ctx.caster,
                maxAge,
                visualType
            )

            ctx.caster?.let {
                val shields = cleanAndGetShieldsFor(it)

                if (shields.size >= MAX_SHIELDS) {
                    repeat(shields.size + 1 - MAX_SHIELDS) {
                        shields.removeFirst().discard()
                    }
                }
                shields.addLast(shield)
            }

            val basePos = position - Vec3d(0.0, 1.3125, 0.0)
            shield.setPosition(basePos)
            shield.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, basePos + direction)
            ctx.world.spawnEntity(shield)
            shield.lockPosition()
        }
    }

    companion object {
        const val MAX_SHIELDS = 10

        private val shieldMap = WeakHashMap<Entity, ArrayDeque<ShieldEntity>>()

        fun cleanAndGetShieldsFor(owner: Entity): ArrayDeque<ShieldEntity> {
            return shieldMap.getOrPut(owner, ::ArrayDeque).apply { removeIf { it.isRemoved || !it.isAlive } }
        }
    }
}
