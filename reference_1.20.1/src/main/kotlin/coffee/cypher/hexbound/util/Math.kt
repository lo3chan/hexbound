package coffee.cypher.hexbound.util

import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i

operator fun Vec3d.times(factor: Double): Vec3d = multiply(factor)
operator fun Vec3i.times(factor: Int): Vec3i = multiply(factor)
