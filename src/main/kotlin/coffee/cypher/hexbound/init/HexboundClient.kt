package coffee.cypher.hexbound.init

import coffee.cypher.hexbound.feature.construct.broadcasting.BroadcasterActivatedS2CPacket
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@EventBusSubscriber(modid = Hexbound.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
object HexboundClient {
    init {
        MOD_BUS.addListener(this::onInitializeClient)
        MOD_BUS.addListener(this::registerPayloads)
    }

    fun onInitializeClient(event: FMLClientSetupEvent) {
        event.enqueueWork {
            initClientRegistries()
        }
    }

    fun registerPayloads(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar("hexbound")
        registrar.playToClient(
            BroadcasterActivatedS2CPacket.TYPE,
            BroadcasterActivatedS2CPacket.CODEC,
            BroadcasterActivatedS2CPacket.Receiver::handle
        )
    }
}
