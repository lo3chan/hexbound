package coffee.cypher.hexbound.feature.construct.entity.component

import net.minecraft.world.item.ItemStack

interface ItemHolderComponent {
    var heldStack: ItemStack

    companion object Key : ConstructComponentKey<ItemHolderComponent>("item_holder")
}
