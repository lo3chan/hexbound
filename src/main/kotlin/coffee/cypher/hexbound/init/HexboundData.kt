package coffee.cypher.hexbound.init

import coffee.cypher.hexbound.feature.combat.shield.ShieldEntity
import coffee.cypher.hexbound.feature.combat.status_effects.ReducedAmbitStatusEffect
import coffee.cypher.hexbound.feature.construct.broadcasting.ConstructBroadcasterBlock
import coffee.cypher.hexbound.feature.construct.command.*
import coffee.cypher.hexbound.feature.construct.entity.SpiderConstructEntity
import coffee.cypher.hexbound.feature.construct.item.SpiderConstructBatteryItem
import coffee.cypher.hexbound.feature.construct.item.SpiderConstructCoreItem
import com.mojang.serialization.Codec
import net.minecraft.world.entity.EntityType
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.network.chat.Component
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.*
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.core.component.DataComponentType
import coffee.cypher.hexbound.feature.media_attachment.StaticMediaValue

object HexboundData {
    val ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Hexbound.MOD_ID)
    val BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Hexbound.MOD_ID)
    val ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Hexbound.MOD_ID)
    val STATUS_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Hexbound.MOD_ID)
    val TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Hexbound.MOD_ID)
    val DATA_COMPONENTS = DeferredRegister.createDataComponents(Hexbound.MOD_ID)

    val CONSTRUCT_COMMANDS_KEY = ResourceKey.createRegistryKey<ConstructCommand.Type<*>>(Hexbound.id("construct_command"))
    val CONSTRUCT_COMMANDS = DeferredRegister.create(CONSTRUCT_COMMANDS_KEY, Hexbound.MOD_ID)

    val STATIC_MEDIA_VALUE = DATA_COMPONENTS.register("static_media_value") { ->
        DataComponentType.builder<StaticMediaValue>().persistent(StaticMediaValue.CODEC).networkSynchronized(StaticMediaValue.STREAM_CODEC).build()
    }

    fun init(bus: net.neoforged.bus.api.IEventBus) {
        ITEMS.register(bus)
        BLOCKS.register(bus)
        ENTITY_TYPES.register(bus)
        STATUS_EFFECTS.register(bus)
        CONSTRUCT_COMMANDS.register(bus)
        TABS.register(bus)
        DATA_COMPONENTS.register(bus)

        ModRegistries.init()
        ItemGroups.init()
        ConstructCommandTypes.init()
        Blocks.init()
        Items.init()
        EntityTypes.init()
        StatusEffects.init()
    }

    object ModRegistries {
        fun init() {}
    }

    object ItemGroups {
        lateinit var HEXBOUND: net.neoforged.neoforge.registries.DeferredHolder<CreativeModeTab, CreativeModeTab>

        fun init() {
            HEXBOUND = TABS.register("hexbound") { ->
                CreativeModeTab.builder()
                    .title(Component.translatable("hexbound.item_group"))
                    .icon { ItemStack(Items.SPIDER_CONSTRUCT_CORE.get() as net.minecraft.world.level.ItemLike) }
                    .displayItems { _, out ->
                        out.accept(ItemStack(Items.SPIDER_CONSTRUCT_CORE.get() as net.minecraft.world.level.ItemLike))
                        out.accept(ItemStack(Items.SPIDER_CONSTRUCT_BATTERY.get() as net.minecraft.world.level.ItemLike))
                        out.accept(ItemStack(Items.CONSTRUCT_BROADCASTER.get() as net.minecraft.world.level.ItemLike))
                    }
                    .build()
            }
        }
    }

    object EntityTypes {
        val SPIDER_CONSTRUCT = ENTITY_TYPES.register("spider_construct") { ->
            SpiderConstructEntity.createType()
        }

        val SHIELD = ENTITY_TYPES.register("shield") { ->
            ShieldEntity.createType()
        }
        fun init() {}
    }

    object ConstructCommandTypes {
        // Mocked registry, need to actually implement a custom codec in neo Forge
        private inline fun <reified T : ConstructCommand<T>> provideType(id: String): net.neoforged.neoforge.registries.DeferredHolder<ConstructCommand.Type<*>, ConstructCommand.Type<T>> {
            return CONSTRUCT_COMMANDS.register(id) { ->
                ConstructCommand.Type(Codec.unit(null as T?) as Codec<T>) // Fallback for now
            }
        }

        val PICK_UP = provideType<PickUp>("pick_up")
        val DROP_OFF = provideType<DropOff>("drop_off")
        val MOVE_TO = provideType<MoveTo>("move_to")
        val NO_OP = provideType<NoOpCommand>("no_op")
        val HARVEST = provideType<Harvest>("harvest")
        val USE_ON_BLOCK = provideType<UseItemOnBlock>("use_on_block")
        fun init() {}
    }

    object Blocks {
        val CONSTRUCT_BROADCASTER = BLOCKS.register("construct_broadcaster") { ->
            ConstructBroadcasterBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of())
        }
        fun init() {}
    }

    object Items {
        val SPIDER_CONSTRUCT_BATTERY = ITEMS.register("spider_construct_battery") { ->
            SpiderConstructBatteryItem(Item.Properties())
        }

        val SPIDER_CONSTRUCT_CORE = ITEMS.register("spider_construct_core") { ->
            SpiderConstructCoreItem(Item.Properties())
        }

        val CONSTRUCT_BROADCASTER = ITEMS.register("construct_broadcaster") { ->
            BlockItem(Blocks.CONSTRUCT_BROADCASTER.get(), Item.Properties())
        }
        fun init() {}
    }

    object StatusEffects {
        val REDUCED_AMBIT = STATUS_EFFECTS.register("reduced_ambit") { ->
            ReducedAmbitStatusEffect() as MobEffect
        }
        fun init() {}
    }
}
