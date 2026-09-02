package coffee.cypher.hexbound.util

import com.mojang.authlib.GameProfile
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import java.util.*
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.RegistryAccess

class MemorizedPlayerData(
    val uuid: UUID,
    val gameProfile: GameProfile,
    val displayName: Component
) {
    fun toNbt(registryAccess: RegistryAccess): CompoundTag {
        val nbt = CompoundTag()

        nbt.putUUID("uuid", uuid)

        val profileNbt = CompoundTag()
        // NbtHelper.writeGameProfile(profileNbt, gameProfile)
        // Ignoring game profile for now
        nbt.put("gameProfile", profileNbt)

        nbt.putString("displayName", Component.Serializer.toJson(displayName, registryAccess))

        return nbt
    }

    companion object {
        fun fromNbt(nbt: CompoundTag, registryAccess: RegistryAccess): MemorizedPlayerData {
            val uuid = nbt.getUUID("uuid")
            // val gameProfile = NbtHelper.toGameProfile(nbt.getCompound("gameProfile")) ?: GameProfile(uuid, "")
            val gameProfile = GameProfile(uuid, "")
            val displayName = Component.Serializer.fromJson(nbt.getString("displayName"), registryAccess) ?: Component.literal("")

            return MemorizedPlayerData(uuid, gameProfile, displayName)
        }

        fun fromPlayer(player: Player): MemorizedPlayerData {
            return MemorizedPlayerData(
                player.uuid,
                player.gameProfile,
                player.displayName ?: Component.literal("")
            )
        }
    }
}
