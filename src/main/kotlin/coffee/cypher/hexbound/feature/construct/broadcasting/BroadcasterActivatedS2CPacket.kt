package coffee.cypher.hexbound.feature.construct.broadcasting

import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import coffee.cypher.hexbound.init.Hexbound
import coffee.cypher.hexbound.init.config.HexboundConfig
import io.netty.buffer.ByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.handling.IPayloadContext

data class BroadcasterActivatedS2CPacket(
    val particleCenter: Vec3,
    val particleOffset: Double,
    val color: Int
) : CustomPacketPayload {

    override fun type(): CustomPacketPayload.Type<BroadcasterActivatedS2CPacket> = TYPE

    fun send(world: ServerLevel) {
        val chunk = ChunkPos(BlockPos(particleCenter.x.toInt(), particleCenter.y.toInt(), particleCenter.z.toInt()))
        PacketDistributor.sendToPlayersTrackingChunk(world, chunk, this)
    }

    companion object {
        val TYPE = CustomPacketPayload.Type<BroadcasterActivatedS2CPacket>(Hexbound.id("broadcaster_activated_s2c"))

        val STREAM_CODEC: StreamCodec<ByteBuf, BroadcasterActivatedS2CPacket> = StreamCodec.of(
            { buf, packet ->
                buf.writeInt(packet.color)
                buf.writeDouble(packet.particleCenter.x)
                buf.writeDouble(packet.particleCenter.y)
                buf.writeDouble(packet.particleCenter.z)
                buf.writeDouble(packet.particleOffset)
            },
            { buf ->
                val color = buf.readInt()
                val x = buf.readDouble()
                val y = buf.readDouble()
                val z = buf.readDouble()
                val offset = buf.readDouble()
                BroadcasterActivatedS2CPacket(Vec3(x, y, z), offset, color)
            }
        )
    }

    object Receiver {
        fun handle(payload: BroadcasterActivatedS2CPacket, context: IPayloadContext) {
            context.enqueueWork {
                val player = context.player()
                val level = player.level()
                val amount = HexboundConfig.broadcasterParticleAmount
                val angleOffset = if (amount > 0) 360f / amount else 0f

                repeat(amount) {
                    val angle = Math.toRadians((it * angleOffset).toDouble())
                    val particleVec = Vec3(Math.cos(angle), 0.0, Math.sin(angle))
                    val particleStart = payload.particleCenter.add(particleVec.scale(payload.particleOffset))
                    val particleVelocity = particleVec.scale(0.2)

                    level.addParticle(
                        ConjureParticleOptions(payload.color),
                        particleStart.x, particleStart.y, particleStart.z,
                        particleVelocity.x, particleVelocity.y, particleVelocity.z
                    )
                }
            }
        }
    }
}
