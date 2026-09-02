package coffee.cypher.hexbound.feature.construct.item

import coffee.cypher.hexbound.init.HexboundData
import net.minecraft.item.Item
import org.quiltmc.qkl.library.items.buildItemSettings

object SpiderConstructCoreItem : Item(
    buildItemSettings {
        group(HexboundData.ItemGroups.HEXBOUND)
    }
)
