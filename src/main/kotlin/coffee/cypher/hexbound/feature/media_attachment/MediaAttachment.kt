package coffee.cypher.hexbound.feature.media_attachment

import at.petrak.hexcasting.api.addldata.ADMediaHolder
import coffee.cypher.hexbound.init.HexboundData
import net.minecraft.world.item.ItemStack

fun getMediaAttachmentForStack(stack: ItemStack): ADMediaHolder? {
    val attachment = stack.get(HexboundData.STATIC_MEDIA_VALUE.get()) ?: return null

    return object : ADMediaHolder {
        override fun getMedia(): Long = attachment.value.toLong()
        override fun getMaxMedia(): Long = attachment.value.toLong()
        override fun setMedia(media: Long) {}
        override fun canProvide(): Boolean = true
        override fun canRecharge(): Boolean = false
        override fun getConsumptionPriority(): Int = attachment.priority
        override fun canConstructHolder(): Boolean = false
        override fun writeMediaHolder(tag: net.minecraft.nbt.CompoundTag?): net.minecraft.nbt.CompoundTag? = tag
    }
}
