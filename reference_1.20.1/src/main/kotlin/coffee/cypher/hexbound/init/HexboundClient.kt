package coffee.cypher.hexbound.init

import coffee.cypher.hexbound.feature.construct.broadcasting.BroadcasterActivatedS2CPacket
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking

object HexboundClient : ClientModInitializer {
    override fun onInitializeClient(mod: ModContainer) {
        initClientRegistries()

        ClientPlayNetworking.registerGlobalReceiver(
            BroadcasterActivatedS2CPacket.CHANNEL,
            BroadcasterActivatedS2CPacket.Receiver
        )

//        registerEvents {
//            onItemTooltip { stack, _, _, lines ->
//                lines as MutableList<Text>
//
//                if (stack.infusions.isNotEmpty()) {
//                    lines += stack.infusions.map { (name, level) ->
//                        buildText {
//                            color(Color.LIGHT_PURPLE) {
//                                translatable("hexbound.infusion.$name")
//                                literal(" ")
//                                translatable("enchantment.level.$level")
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }
}
