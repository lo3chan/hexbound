package coffee.cypher.hexbound.feature.construct.casting.env

import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class ConstructMishapEnv(val construct: AbstractConstructEntity, world: ServerWorld) : MishapEnvironment(world, null) {
    override fun yeetHeldItemsTowards(targetPos: Vec3d?) {
        TODO("Not yet implemented")
    }

    override fun dropHeldItems() {
        TODO("Not yet implemented")
    }

    override fun drown() {
        TODO("Not yet implemented")
    }

    override fun damage(healthProportion: Float) {
        TODO("Not yet implemented")
    }

    override fun removeXp(amount: Int) {
    }

    override fun blind(ticks: Int) {
    }
}
