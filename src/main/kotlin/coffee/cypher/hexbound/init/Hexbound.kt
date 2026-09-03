package coffee.cypher.hexbound.init

import at.petrak.hexcasting.api.item.HexHolderItem
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.common.lib.HexItems
import coffee.cypher.hexbound.init.config.HexboundConfig
//import coffee.cypher.hexbound.interop.InteropManager
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import coffee.cypher.hexbound.feature.construct.broadcasting.BroadcasterActivatedS2CPacket
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.bus.api.IEventBus

@Mod(Hexbound.MOD_ID)
class HexboundForge(modBus: IEventBus) {
    init {
        modBus.addListener(Hexbound::onInitialize)
        modBus.addListener(Hexbound::registerPayloads)
        HexboundData.init(modBus)

        // Uncomment once InteropManager is fully migrated
        // InteropManager.init()

        // Debug features
        NeoForge.EVENT_BUS.addListener(Hexbound::onCommandRegistration)
    }
}

object Hexbound {
    const val MOD_ID = "hexbound"

    val LOGGER: Logger by lazy {
        LoggerFactory.getLogger(MOD_ID)
    }

    fun id(name: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name)
    }

    fun onInitialize(event: FMLCommonSetupEvent) {
        event.enqueueWork {
            HexboundConfig.init()
            HexboundPatterns.register()
        }
    }

    fun registerPayloads(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(MOD_ID)
        registrar.playToClient(
            BroadcasterActivatedS2CPacket.TYPE,
            BroadcasterActivatedS2CPacket.STREAM_CODEC,
            BroadcasterActivatedS2CPacket.Receiver::handle
        )
    }

    fun onCommandRegistration(event: RegisterCommandsEvent) {
        // We will skip registering debug commands for now as Brigadier registration is complex
        // and requires standard dispatcher usage rather than Quilt's helpers
    }
}
