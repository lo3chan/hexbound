package coffee.cypher.hexbound.feature.construct.casting.env

import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

class ConstructMishapEnv(val construct: AbstractConstructEntity, level: ServerLevel) : MishapEnvironment(level, construct.boundPlayerData?.let { level.getPlayerByUUID(it.uuid) as? ServerPlayer }) {
    override fun yeetHeldItemsTowards(pos: Vec3) {
    }

    override fun dropHeldItems() {
    }

    override fun drown() {
    }

    override fun damage(healthProportion: Float) {
    }

    override fun removeXp(amount: Int) {
    }

    override fun blind(ticks: Int) {
    }
}
