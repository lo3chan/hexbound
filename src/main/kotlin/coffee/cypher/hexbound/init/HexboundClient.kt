package coffee.cypher.hexbound.init

import coffee.cypher.hexbound.feature.construct.broadcasting.BroadcasterActivatedS2CPacket
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.bus.api.SubscribeEvent

@EventBusSubscriber(modid = Hexbound.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
object HexboundClient {
    @SubscribeEvent
    fun onInitializeClient(event: FMLClientSetupEvent) {
        event.enqueueWork {
            initClientRegistries()
        }
    }

    @SubscribeEvent
    fun registerPayloads(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar("hexbound")
        registrar.playToClient(
            BroadcasterActivatedS2CPacket.TYPE,
            BroadcasterActivatedS2CPacket.CODEC,
            BroadcasterActivatedS2CPacket.Receiver::handle
        )
    }
}
