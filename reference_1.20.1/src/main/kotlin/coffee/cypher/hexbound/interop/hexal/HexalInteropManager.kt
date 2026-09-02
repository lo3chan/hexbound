package coffee.cypher.hexbound.interop.hexal

import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import coffee.cypher.hexbound.init.Hexbound
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import ram.talia.hexal.api.linkable.LinkableRegistry

object HexalInteropManager {
    val CONSTRUCT_LINKABLE: ComponentKey<ConstructLinkable> =
        ComponentRegistryV3.INSTANCE.getOrCreate(
            Hexbound.id("construct_linkable"),
            ConstructLinkable::class.java
        )

    fun init() {
        LinkableRegistry.registerLinkableType(ConstructLinkableType)
    }

    fun registerEntityComponents(registry: EntityComponentFactoryRegistry) {
        registry.registerFor(AbstractConstructEntity::class.java, CONSTRUCT_LINKABLE, ::ConstructLinkable)
    }
}

fun AbstractConstructEntity.getLinkable(): ConstructLinkable =
    HexalInteropManager.CONSTRUCT_LINKABLE[this]
