package coffee.cypher.hexbound.util

import net.minecraft.world.phys.Vec3
import net.minecraft.core.Vec3i

operator fun Vec3.times(factor: Double): Vec3 = this.scale(factor)
operator fun Vec3i.times(factor: Int): Vec3i = Vec3i(this.x * factor, this.y * factor, this.z * factor)
