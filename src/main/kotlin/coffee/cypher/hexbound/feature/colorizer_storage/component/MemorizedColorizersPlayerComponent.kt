package coffee.cypher.hexbound.feature.colorizer_storage.component

import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.neoforged.neoforge.common.util.INBTSerializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.HolderLookup

class MemorizedColorizersPlayerComponent(
    val colorizers: MutableMap<String, FrozenPigment> = mutableMapOf()
) : INBTSerializable<CompoundTag> {
    override fun deserializeNBT(provider: HolderLookup.Provider, tag: CompoundTag) {
        colorizers.clear()
        tag.allKeys.forEach {
            colorizers[it] = FrozenPigment.fromNBT(tag.getCompound(it))
        }
    }

    override fun serializeNBT(provider: HolderLookup.Provider): CompoundTag {
        val tag = CompoundTag()
        colorizers.forEach { (k, v) ->
            tag.put(k, v.serializeToNBT())
        }
        return tag
    }
}
