package coffee.cypher.hexbound.feature.construct.entity.component

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld

interface InteractionComponent {
    fun getInteractionPlayer(world: ServerWorld): ServerPlayerEntity

    companion object Key : ConstructComponentKey<InteractionComponent>("interaction")
}
