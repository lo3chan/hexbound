package coffee.cypher.hexbound.feature.construct.broadcasting

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.xplat.IXplatAbstractions
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

data class BroadcastingContext(
    val broadcaster: BlockPos,
    val center: Vec3d,
    val radius: Double,
    val pattern: HexPattern?,
    val particleCenter: Vec3d,
    val particleOffset: Double
) {
    fun broadcast(instructions: List<Iota>, ctx: CastingEnvironment) {
        val radiusSqr = radius * radius

        ctx.world.getEntitiesByClass(AbstractConstructEntity::class.java, Box(center, center).expand(radius)) {
            it.pos.squaredDistanceTo(center) <= radiusSqr
        }.forEach {
            it.acceptInstructions(instructions, ctx.caster, true, pattern)
        }

        val random = ctx.world.random

        val particleColorizer = IXplatAbstractions.INSTANCE.getPigment(ctx.caster)
        val particleColor = particleColorizer.colorProvider.getColor(
            random.nextFloat() * 16384,
            Vec3d(
                random.nextFloat().toDouble(),
                random.nextFloat().toDouble(),
                random.nextFloat().toDouble()
            ).multiply((random.nextFloat() * 3).toDouble())
        )

        ConstructBroadcasterBlock.onActivated(ctx.world, broadcaster)

        BroadcasterActivatedS2CPacket(particleCenter, particleOffset, particleColor).send(ctx.world)
    }
}
