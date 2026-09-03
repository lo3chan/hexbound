package coffee.cypher.hexbound.feature.media_attachment

import at.petrak.hexcasting.api.misc.MediaConstants
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.StringRepresentable
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.ByteBufCodecs

data class StaticMediaValue(val priority: Int, val amount: Int, val unit: Unit) {
    enum class Unit(private val serializedName: String, val calculate: (Int) -> Int) : StringRepresentable {
        ABSOLUTE("absolute", { it }),
        DUST("dust", { it * MediaConstants.DUST_UNIT.toInt() }),
        SHARD("shard", { it * MediaConstants.SHARD_UNIT.toInt() }),
        CRYSTAL("crystal", { it * MediaConstants.CRYSTAL_UNIT.toInt() });

        override fun getSerializedName(): String = serializedName
    }

    val value get() = unit.calculate(amount)

    companion object {
        val UNIT_CODEC: Codec<Unit> = StringRepresentable.fromEnum({ Unit.values() })
        val UNIT_STREAM_CODEC: StreamCodec<ByteBuf, Unit> = ByteBufCodecs.idMapper({ it.ordinal }, { i -> Unit.values()[i] })

        val CODEC: Codec<StaticMediaValue> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("priority").forGetter(StaticMediaValue::priority),
                Codec.INT.fieldOf("amount").forGetter(StaticMediaValue::amount),
                UNIT_CODEC.fieldOf("unit").forGetter(StaticMediaValue::unit)
            ).apply(instance, ::StaticMediaValue)
        }

        val STREAM_CODEC: StreamCodec<ByteBuf, StaticMediaValue> = StreamCodec.composite(
            ByteBufCodecs.INT, StaticMediaValue::priority,
            ByteBufCodecs.INT, StaticMediaValue::amount,
            UNIT_STREAM_CODEC, StaticMediaValue::unit,
            ::StaticMediaValue
        )
    }
}
