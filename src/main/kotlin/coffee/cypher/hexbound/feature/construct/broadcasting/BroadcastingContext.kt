package coffee.cypher.hexbound.feature.construct.broadcasting

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.xplat.IXplatAbstractions
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

data class BroadcastingContext(
    val broadcaster: BlockPos,
    val center: Vec3,
    val radius: Double,
    val pattern: HexPattern?,
    val particleCenter: Vec3,
    val particleOffset: Double
) {
    fun broadcast(instructions: List<Iota>, ctx: CastingEnvironment) {
        val radiusSqr = radius * radius

        ctx.world.getEntitiesOfClass(AbstractConstructEntity::class.java, AABB(center, center).inflate(radius)) {
            it.position().distanceToSqr(center) <= radiusSqr
        }.forEach {
            it.acceptInstructions(instructions, ctx.caster, true, pattern)
        }

        val random = ctx.world.random

        val particleColorizer = IXplatAbstractions.INSTANCE.getPigment(ctx.caster)
        val particleColor = particleColorizer.colorProvider.getColor(
            random.nextFloat() * 16384,
            Vec3(
                random.nextFloat().toDouble(),
                random.nextFloat().toDouble(),
                random.nextFloat().toDouble()
            ).scale((random.nextFloat() * 3).toDouble())
        )

        (ctx.world.getBlockState(broadcaster).block as? ConstructBroadcasterBlock)?.onActivated(ctx.world, broadcaster)

        BroadcasterActivatedS2CPacket(particleCenter, particleOffset, particleColor).send(ctx.world)
    }
}
