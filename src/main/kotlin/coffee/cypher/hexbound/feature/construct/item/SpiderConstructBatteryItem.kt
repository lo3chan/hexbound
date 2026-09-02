package coffee.cypher.hexbound.feature.construct.item

import at.petrak.hexcasting.api.item.MediaHolderItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.utils.mediaBarColor
import at.petrak.hexcasting.api.utils.mediaBarWidth
import coffee.cypher.hexbound.init.HexboundData
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import coffee.cypher.hexbound.init.config.HexboundConfig
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.CustomData

class SpiderConstructBatteryItem(properties: Properties) : Item(properties.stacksTo(1)), MediaHolderItem {
    val maxCharge: Long
        get() = (HexboundConfig.spiderBatteryChargeRequired * MediaConstants.DUST_UNIT).toLong()

    var ItemStack.charge: Long
        get() {
            val customData = this.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            return if (customData.contains("charge")) customData.copyTag().getLong("charge") else 0L
        }
        set(value) {
            CustomData.update(DataComponents.CUSTOM_DATA, this) { tag ->
                tag.putLong("charge", value)
            }
        }

    override fun getMedia(stack: ItemStack): Long {
        return stack.charge
    }

    override fun getMaxMedia(stack: ItemStack): Long {
        return maxCharge
    }

    override fun setMedia(stack: ItemStack, media: Long) {
        stack.charge = media
    }

    override fun canProvideMedia(stack: ItemStack): Boolean {
        return false
    }

    override fun canRecharge(stack: ItemStack): Boolean {
        return true
    }

    override fun isBarVisible(stack: ItemStack): Boolean {
        return stack.charge < maxCharge
    }

    override fun getBarColor(stack: ItemStack): Int {
        return mediaBarColor(stack.charge, maxCharge)
    }

    override fun getBarWidth(stack: ItemStack): Int {
        return mediaBarWidth(stack.charge, maxCharge)
    }

    override fun getDefaultInstance(): ItemStack {
        return ItemStack(this, 1).also { it.charge = 0L }
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component>,
        flag: TooltipFlag
    ) {
        val hexColor = 0xB38EF3

        if (stack.charge == maxCharge) {
            tooltip.add(Component.translatable("item.hexbound.spider_construct_battery.full_charge").withColor(hexColor))
        } else {
            val percentage = ((stack.charge.toDouble() / maxCharge) * 100).toInt()

            val currentText = Component.literal((stack.charge / MediaConstants.DUST_UNIT).toString()).withColor(hexColor)
            val maxText = Component.literal(HexboundConfig.spiderBatteryChargeRequired.toString()).withColor(hexColor)

            tooltip.add(Component.translatable("item.hexbound.spider_construct_battery.charge", percentage, currentText, maxText))
        }
    }

    companion object {
        fun isFullyCharged(stack: ItemStack): Boolean {
            val battery = stack.item as? SpiderConstructBatteryItem ?: return false
            return stack.charge >= battery.maxCharge
        }
    }
}
