package coffee.cypher.hexbound.init

import at.petrak.hexcasting.api.pigment.FrozenPigment
import coffee.cypher.hexbound.feature.colorizer_storage.component.MemorizedColorizersPlayerComponent
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.attachment.IAttachmentSerializer
import net.neoforged.neoforge.attachment.IAttachmentHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import java.util.function.Supplier

object HexboundAttachments {
    val ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Hexbound.MOD_ID)

    val MEMORIZED_COLORIZERS = ATTACHMENT_TYPES.register("memorized_colorizers", Supplier {
        AttachmentType.builder(Supplier { MemorizedColorizersPlayerComponent(mutableMapOf()) })
            .serialize(object : IAttachmentSerializer<CompoundTag, MemorizedColorizersPlayerComponent> {
                override fun write(attachment: MemorizedColorizersPlayerComponent, provider: HolderLookup.Provider): CompoundTag {
                    return attachment.serializeNBT(provider)
                }

                override fun read(holder: IAttachmentHolder, tag: CompoundTag, provider: HolderLookup.Provider): MemorizedColorizersPlayerComponent {
                    val component = MemorizedColorizersPlayerComponent(mutableMapOf())
                    component.deserializeNBT(provider, tag)
                    return component
                }
            })
            .copyOnDeath()
            .build()
    })
}

val Player.memorizedColorizers: MutableMap<String, FrozenPigment>
    get() = this.getData(HexboundAttachments.MEMORIZED_COLORIZERS).colorizers
