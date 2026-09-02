package coffee.cypher.hexbound.feature.construct.broadcasting

import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import coffee.cypher.hexbound.init.Hexbound
import coffee.cypher.hexbound.init.config.HexboundConfig
import coffee.cypher.hexbound.util.times
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d
import org.quiltmc.loader.api.minecraft.ClientOnly
import org.quiltmc.qkl.library.math.plus
import org.quiltmc.qkl.library.networking.getPlayersTrackingChunk
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.PacketSender
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking.ChannelReceiver


data class BroadcasterActivatedS2CPacket(
    val particleCenter: Vec3d,
    val particleOffset: Double,
    val color: Int
) {
    fun send(world: ServerWorld) {
        val chunk = ChunkPos(BlockPos(particleCenter.x.toInt(), particleCenter.y.toInt(), particleCenter.z.toInt()))

        val buf = PacketByteBufs.create().also {
            it.writeInt(color)
            it.writeDouble(particleCenter.x)
            it.writeDouble(particleCenter.y)
            it.writeDouble(particleCenter.z)
            it.writeDouble(particleOffset)
        }

        ServerPlayNetworking.send(world.getPlayersTrackingChunk(chunk), CHANNEL, buf)
    }

    companion object {
        val CHANNEL = Hexbound.id("broadcaster_activated_s2c")
    }

    @ClientOnly
    object Receiver : ChannelReceiver {
        override fun receive(
            client: MinecraftClient,
            handler: ClientPlayNetworkHandler,
            buf: PacketByteBuf,
            responseSender: PacketSender
        ) {
            val color = buf.readInt()

            val x = buf.readDouble()
            val y = buf.readDouble()
            val z = buf.readDouble()
            val particleCenter = Vec3d(x, y, z)

            val particleOffset = buf.readDouble()

            client.execute {
                val angleOffset = if (HexboundConfig.broadcasterParticleAmount > 0) {
                    360f / HexboundConfig.broadcasterParticleAmount
                } else {
                    0f
                }

                repeat(HexboundConfig.broadcasterParticleAmount) {
                    val angle = it * angleOffset

                    val particleVec = Vec3d.fromPolar(0f, angle)

                    val particleStart = particleCenter + particleVec * particleOffset
                    val particleVelocity = particleVec * 0.2

                    client.world?.addParticle(
                        ConjureParticleOptions(color),
                        particleStart.x, particleStart.y, particleStart.z,
                        particleVelocity.x, particleVelocity.y, particleVelocity.z,
                    )
                }
            }
        }

    }
}
