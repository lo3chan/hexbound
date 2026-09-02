package coffee.cypher.hexbound.init

import at.petrak.hexcasting.api.pigment.FrozenPigment
import coffee.cypher.hexbound.feature.colorizer_storage.component.MemorizedColorizersPlayerComponent
import coffee.cypher.hexbound.interop.InteropManager
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import net.minecraft.server.network.ServerPlayerEntity

object HexboundComponents : EntityComponentInitializer {
    val MEMORIZED_COLORIZERS: ComponentKey<MemorizedColorizersPlayerComponent> =
        ComponentRegistryV3.INSTANCE.getOrCreate(
            Hexbound.id("memorized_colorizers"),
            MemorizedColorizersPlayerComponent::class.java
        )

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(
            MEMORIZED_COLORIZERS,
            { MemorizedColorizersPlayerComponent(mutableMapOf()) },
            RespawnCopyStrategy.ALWAYS_COPY
        )

        InteropManager.registerEntityComponents(registry)
    }
}

val ServerPlayerEntity.memorizedColorizers: MutableMap<String, FrozenPigment>
    get() = HexboundComponents.MEMORIZED_COLORIZERS[this].colorizers
