package coffee.cypher.hexbound.init

import coffee.cypher.hexbound.feature.construct.broadcasting.BroadcasterActivatedS2CPacket
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.RegisterShadersEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.bus.api.SubscribeEvent

@EventBusSubscriber(modid = Hexbound.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
object HexboundClient {
    @JvmStatic
    @SubscribeEvent
    fun registerRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        initEntityRenderers(event)
    }

    @JvmStatic
    @SubscribeEvent
    fun registerShaders(event: RegisterShadersEvent) {
        initShaders(event)
    }

    @JvmStatic
    @SubscribeEvent
    fun registerPayloads(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(Hexbound.MOD_ID)
        registrar.playToClient(
            BroadcasterActivatedS2CPacket.TYPE,
            BroadcasterActivatedS2CPacket.STREAM_CODEC,
            BroadcasterActivatedS2CPacket.Receiver::handle
        )
    }
}
