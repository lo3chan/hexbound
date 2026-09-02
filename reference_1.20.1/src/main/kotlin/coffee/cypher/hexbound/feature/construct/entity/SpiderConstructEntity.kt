package coffee.cypher.hexbound.feature.construct.entity

import coffee.cypher.hexbound.feature.construct.entity.component.InteractionComponent
import coffee.cypher.hexbound.feature.construct.entity.component.ItemHolderComponent
import coffee.cypher.hexbound.util.provideDelegate
import coffee.cypher.hexbound.util.redirectSpiderLang
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.quiltmc.qkl.library.nbt.set
import org.quiltmc.qsl.entity.api.QuiltEntityTypeBuilder
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.constant.DefaultAnimations
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.core.animation.AnimatableManager
import software.bernie.geckolib.core.animation.AnimationController
import software.bernie.geckolib.core.animation.RawAnimation
import software.bernie.geckolib.core.`object`.PlayState
import software.bernie.geckolib.util.GeckoLibUtil

class SpiderConstructEntity(
    entityType: EntityType<SpiderConstructEntity>,
    world: World
) : AbstractConstructEntity(entityType, world), GeoEntity, ItemHolderComponent, InteractionComponent {
    override var heldStack by HELD_STACK
    var isAltModelEnabled by ALT_MODEL_ENABLED

    private var songSource: BlockPos? = null
    private val animationCache = GeckoLibUtil.createInstanceCache(this)

    init {
        registerComponent(ItemHolderComponent, this)
        registerComponent(InteractionComponent, this)
        handDropChances[0] = Float.MAX_VALUE
    }

    private val animationController = AnimationController(this, "animation_controller", 0) { state ->
        when {
            state.isMoving -> {
                state.setAndContinue(DefaultAnimations.WALK)
            }

            state.animatable.canDance() -> {
                state.setAndContinue(DANCE_ANIMATION)
            }

            else -> PlayState.STOP
        }
    }

    override fun initDataTracker() {
        super.initDataTracker()
        dataTracker.startTracking(ALT_MODEL_ENABLED, false)
        dataTracker.startTracking(HELD_STACK, ItemStack.EMPTY)
    }

    override fun interactMob(player: PlayerEntity, hand: Hand): ActionResult {
        val item = player.getStackInHand(hand).item

        if (player.isSneaking && (item == Items.IRON_BLOCK || item == Items.AMETHYST_BLOCK)) {
            if (!world.isClient) {
                val serverPlayer = player as ServerPlayerEntity

                if (!serverPlayer.interactionManager.gameMode.isBlockBreakingRestricted) {
                    isAltModelEnabled = item == Items.IRON_BLOCK
                }
            } else {
                player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC)
            }

            return ActionResult.SUCCESS
        }

        return super.interactMob(player, hand)
    }

    override fun tick() {
        super.tick()
        fakePlayer?.setStackInHand(Hand.MAIN_HAND, heldStack)
    }

    override fun getInteractionPlayer(world: ServerWorld): ServerPlayerEntity {
        return fakePlayer!!
    }

    override fun equipStack(slot: EquipmentSlot, stack: ItemStack) {
        if (slot == EquipmentSlot.MAINHAND) {
            heldStack = stack
        }
    }

    override fun getEquippedStack(slot: EquipmentSlot): ItemStack {
        return when (slot) {
            EquipmentSlot.MAINHAND -> heldStack
            else -> ItemStack.EMPTY
        }
    }

    override fun setNearbySongPlaying(songPosition: BlockPos?, playing: Boolean) {
        songSource = if (playing)
            songPosition
        else
            null
    }

    private fun canDance(): Boolean {
        val dist = songSource?.getSquaredDistanceToCenter(x, y, z)

        return dist != null && dist <= 36 && command == null
    }

    override fun getDefaultName(): Text {
        return redirectSpiderLang(super.getDefaultName(), this)
    }

    override fun registerControllers(registar: AnimatableManager.ControllerRegistrar) {
        registar.add(animationController)
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache {
        return animationCache
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)

        nbt["held_stack"] = heldStack.writeNbt(NbtCompound())
        nbt["alt_model"] = isAltModelEnabled
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)

        heldStack = ItemStack.fromNbt(nbt.getCompound("held_stack"))
        isAltModelEnabled = nbt.getBoolean("alt_model")
    }

    companion object {
        fun createType(): EntityType<SpiderConstructEntity> {
            return QuiltEntityTypeBuilder
                .createMob<SpiderConstructEntity>()
                .spawnGroup(SpawnGroup.MISC)
                .entityFactory(::SpiderConstructEntity)
                .setDimensions(EntityDimensions.fixed(1.25f, 0.75f))
                .defaultAttributes(
                    createAttributes()
                        .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                )
                .build()
        }

        private val DANCE_ANIMATION = RawAnimation.begin().thenLoop("dance")

        val ALT_MODEL_ENABLED: TrackedData<Boolean> = DataTracker.registerData(
            SpiderConstructEntity::class.java,
            TrackedDataHandlerRegistry.BOOLEAN
        )

        val HELD_STACK: TrackedData<ItemStack> = DataTracker.registerData(
            SpiderConstructEntity::class.java,
            TrackedDataHandlerRegistry.ITEM_STACK
        )
    }
}
