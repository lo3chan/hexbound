package coffee.cypher.hexbound.feature.construct.casting.env

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import at.petrak.hexcasting.api.pigment.FrozenPigment
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class ConstructCastEnv(val construct: AbstractConstructEntity) : CastingEnvironment(construct.world as ServerWorld) {
    override fun getCaster(): ServerPlayerEntity? {
        return null
    }

    override fun getMishapEnvironment(): MishapEnvironment {
        TODO("Not yet implemented")
    }

    override fun postExecution(result: CastResult?) {
        TODO("record mishaps")
    }

    override fun mishapSprayPos(): Vec3d {
        return construct.pos
    }

    override fun extractMedia(cost: Long): Long {
        TODO("check free actions or max media")
    }

    override fun isVecInRange(vec: Vec3d?): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasEditPermissionsAt(vec: BlockPos?): Boolean {
        return true //TODO check against a fake player?
    }

    override fun getCastingHand(): Hand {
        return Hand.MAIN_HAND
    }

    override fun getAlternateItem(): ItemStack {
        TODO("Not yet implemented")
    }

    override fun getUsableStacks(mode: StackDiscoveryMode): MutableList<ItemStack> {
        TODO("Not yet implemented")
    }

    override fun getPrimaryStacks(): List<HeldItemInfo> {
        return emptyList()
    }

    override fun getPigment(): FrozenPigment {
        return FrozenPigment.DEFAULT.get()
    }

    override fun setPigment(pigment: FrozenPigment?): FrozenPigment? {
        return null
    }

    override fun produceParticles(particles: ParticleSpray, colorizer: FrozenPigment) {
        particles.sprayParticles(world, colorizer)
    }

    override fun printMessage(message: Text) {
        TODO("Not yet implemented")
    }
}
