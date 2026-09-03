package coffee.cypher.hexbound.feature.construct.entity.component

import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel

interface InteractionComponent {
    fun getInteractionPlayer(world: ServerLevel): ServerPlayer

    companion object Key : ConstructComponentKey<InteractionComponent>("interaction")
}
