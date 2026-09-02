@file:Suppress("unused")

package coffee.cypher.hexbound.init

import coffee.cypher.hexbound.feature.combat.shield.ShieldEntity
import coffee.cypher.hexbound.feature.combat.status_effects.ReducedAmbitStatusEffect
import coffee.cypher.hexbound.feature.construct.broadcasting.ConstructBroadcasterBlock
import coffee.cypher.hexbound.feature.construct.command.*
import coffee.cypher.hexbound.feature.construct.entity.SpiderConstructEntity
import coffee.cypher.hexbound.feature.construct.item.SpiderConstructBatteryItem
import coffee.cypher.hexbound.feature.construct.item.SpiderConstructCoreItem
import coffee.cypher.hexbound.feature.media_attachment.STATIC_MEDIA_ATTACHMENT
import com.mojang.serialization.Codec
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.quiltmc.qkl.library.items.itemSettingsOf
import org.quiltmc.qkl.library.registry.RegistryAction
import org.quiltmc.qkl.library.registry.provide
import org.quiltmc.qkl.library.serialization.CodecFactory
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

object HexboundData : DataInitializer() {
    fun init() {
        ModRegistries.init()
        ItemGroups.init()
        initRegistries()
        STATIC_MEDIA_ATTACHMENT
    }

    object ModRegistries {
        lateinit var CONSTRUCT_COMMANDS_KEY: RegistryKey<Registry<ConstructCommand.Type<*>>>
        lateinit var CONSTRUCT_COMMANDS: DefaultedRegistry<ConstructCommand.Type<*>>

        fun init() {
            CONSTRUCT_COMMANDS_KEY = RegistryKey.ofRegistry(Hexbound.id("construct_command"))

            CONSTRUCT_COMMANDS = FabricRegistryBuilder.createDefaulted(
                CONSTRUCT_COMMANDS_KEY,
                Hexbound.id("no_op")
            ).buildAndRegister()
        }
    }

    object ItemGroups {
        lateinit var HEXBOUND: ItemGroup

        fun init() {
            HEXBOUND = FabricItemGroup.builder()
                .name(Text.translatable("hexbound.item_group"))
                .icon(Items.SPIDER_CONSTRUCT_CORE::getDefaultStack)
                .build()
        }
    }

    object EntityTypes : Initializer<EntityType<*>>(Registries.ENTITY_TYPE) {
        val SPIDER_CONSTRUCT: EntityType<SpiderConstructEntity> by registry.provide("spider_construct") {
            SpiderConstructEntity.createType()
        }

        val SHIELD: EntityType<ShieldEntity> by registry.provide("shield") {
            ShieldEntity.createType()
        }
    }

    object ConstructCommandTypes : Initializer<ConstructCommand.Type<*>>(ModRegistries.CONSTRUCT_COMMANDS) {
        private val codecFactory = CodecFactory {
            codecs {
                named(Vec3d.CODEC, "Vec3d")
                named(BlockPos.CODEC, "BlockPos")
                unnamed(Codec.STRING.xmap(UUID::fromString, UUID::toString))
            }
        }

        private inline fun <reified T : ConstructCommand<T>> provideType(id: String): Lazy<ConstructCommand.Type<T>> {
            return registry.provide(id) {
                ConstructCommand.Type(codecFactory.create())
            }
        }

        val PICK_UP by provideType<PickUp>("pick_up")
        val DROP_OFF by provideType<DropOff>("drop_off")
        val MOVE_TO by provideType<MoveTo>("move_to")
        val NO_OP by provideType<NoOpCommand>("no_op")
        val HARVEST by provideType<Harvest>("harvest")
        val USE_ON_BLOCK by provideType<UseItemOnBlock>("use_on_block")
    }

    object Blocks : Initializer<Block>(Registries.BLOCK) {
        val CONSTRUCT_BROADCASTER by registry.provide("construct_broadcaster") {
            ConstructBroadcasterBlock
        }
    }

    object Items : Initializer<Item>(Registries.ITEM) {
        val SPIDER_CONSTRUCT_BATTERY by registry.provide("spider_construct_battery") {
            SpiderConstructBatteryItem
        }

        val SPIDER_CONSTRUCT_CORE by registry.provide("spider_construct_core") {
            SpiderConstructCoreItem
        }

        val CONSTRUCT_BROADCASTER by registry.provide("construct_broadcaster") {
            BlockItem(ConstructBroadcasterBlock, itemSettingsOf(group = ItemGroups.HEXBOUND))
        }
    }

    object StatusEffects : Initializer<StatusEffect>(Registries.STATUS_EFFECT) {
        val REDUCED_AMBIT by registry.provide("reduced_ambit") {
            ReducedAmbitStatusEffect()
        }
    }
}

abstract class DataInitializer {
    private val initializers = mutableListOf<Initializer<*>>()

    fun initRegistries() {
        this::class.nestedClasses.forEach {
            it.objectInstance
        }

        initializers.forEach {
            it.init()
            Hexbound.LOGGER.debug("Registered {}", it)
        }
    }

    abstract inner class Initializer<T>(
        registry: Registry<T>
    ) {
        protected val registry = RegistryAction(Hexbound.MOD_ID, registry)

        init {
            @Suppress("LeakingThis")
            initializers += this
        }

        fun init() {
            initClass(this)
        }

        private fun <T : Any> initClass(instance: T) {
            @Suppress("UNCHECKED_CAST")
            val klass = instance::class as KClass<T>

            klass.declaredMemberProperties.onEach {
                it.isAccessible = true
            }.forEach {
                val delegate = it.getDelegate(instance)

                if (delegate is Lazy<*>) {
                    delegate.getValue(instance, it)
                }
            }
        }
    }
}
