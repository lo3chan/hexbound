package coffee.cypher.hexbound.feature.colorizer_storage.component

import at.petrak.hexcasting.api.pigment.FrozenPigment
import dev.onyxstudios.cca.api.v3.component.ComponentV3
import net.minecraft.nbt.NbtCompound

class MemorizedColorizersPlayerComponent(
    val colorizers: MutableMap<String, FrozenPigment>
) : ComponentV3 {
    override fun readFromNbt(tag: NbtCompound) {
        colorizers.clear()
        tag.keys.forEach {
            colorizers[it] = FrozenPigment.fromNBT(tag.getCompound(it))
        }
    }

    override fun writeToNbt(tag: NbtCompound) {
        colorizers.forEach { (k, v) ->
            tag.put(k, v.serializeToNBT())
        }
    }
}
