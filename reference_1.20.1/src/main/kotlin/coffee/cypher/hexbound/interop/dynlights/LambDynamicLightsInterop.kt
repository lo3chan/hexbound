package coffee.cypher.hexbound.interop.dynlights

import coffee.cypher.hexbound.init.HexboundData
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer

class LambDynamicLightsInterop : DynamicLightsInitializer {
    override fun onInitializeDynamicLights() {
        DynamicLightHandlers.registerDynamicLightHandler(HexboundData.EntityTypes.SHIELD) { 8 }
    }
}
