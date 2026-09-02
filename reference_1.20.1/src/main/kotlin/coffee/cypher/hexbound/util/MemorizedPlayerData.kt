package coffee.cypher.hexbound.util

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import org.quiltmc.qkl.library.nbt.set
import java.util.UUID

data class MemorizedPlayerData(
    val uuid: UUID,
    val name: String,
    val displayName: Text,
    val pigment: FrozenPigment
) {
    fun toNbt(): NbtCompound {
        val result = NbtCompound()

        result.putUuid("uuid", uuid)
        result["name"] = name
        result["displayName"] = Text.Serializer.toJson(displayName)
        result["pigment"] = pigment.serializeToNBT()

        return result
    }

    companion object {
        fun fromNbt(nbt: NbtCompound): MemorizedPlayerData {
            val uuid = nbt.getUuid("uuid")
            val name = nbt.getString("name")
            val displayName = Text.Serializer.fromLenientJson(nbt.getString("displayName")) ?: Text.literal(name)
            val colorizer = FrozenPigment.fromNBT(nbt.getCompound("colorizer"))

            return MemorizedPlayerData(uuid, name, displayName, colorizer)
        }

        fun forPlayer(player: PlayerEntity): MemorizedPlayerData {
            return MemorizedPlayerData(
                player.uuid,
                player.gameProfile.name,
                player.displayName,
                IXplatAbstractions.INSTANCE.getPigment(player)
            )
        }
    }
}
