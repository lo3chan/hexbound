package coffee.cypher.hexbound.feature.construct.casting.env

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import at.petrak.hexcasting.api.pigment.FrozenPigment
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

class ConstructCastEnv(val construct: AbstractConstructEntity) : CastingEnvironment(construct.level() as ServerLevel) {
    override fun getCastingEntity(): LivingEntity? {
        return construct
    }

    override fun getMishapEnvironment(): MishapEnvironment {
        return ConstructMishapEnv(construct, world)
    }

    override fun postExecution(result: CastResult) {
        super.postExecution(result)
    }

    override fun mishapSprayPos(): Vec3 {
        return construct.position()
    }

    override fun extractMediaEnvironment(cost: Long, simulate: Boolean): Long {
        return 0L
    }

    override fun isVecInRangeEnvironment(vec: Vec3): Boolean {
        return vec.distanceToSqr(construct.position()) <= 32.0 * 32.0
    }

    override fun hasEditPermissionsAtEnvironment(pos: BlockPos): Boolean {
        return true
    }

    override fun getCastingHand(): InteractionHand {
        return InteractionHand.MAIN_HAND
    }

    override fun getUsableStacks(mode: StackDiscoveryMode): MutableList<ItemStack> {
        return mutableListOf()
    }

    override fun getPrimaryStacks(): MutableList<HeldItemInfo> {
        return mutableListOf()
    }

    override fun getPigment(): FrozenPigment {
        return FrozenPigment.DEFAULT.get()
    }

    override fun setPigment(pigment: FrozenPigment?): FrozenPigment? {
        return null
    }

    override fun produceParticles(particles: ParticleSpray, pigment: FrozenPigment) {
        particles.sprayParticles(world, pigment)
    }

    override fun printMessage(message: Component) {
    }
}
