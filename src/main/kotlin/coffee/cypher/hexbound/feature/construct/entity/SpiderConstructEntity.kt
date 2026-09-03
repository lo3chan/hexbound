package coffee.cypher.hexbound.feature.construct.entity

import coffee.cypher.hexbound.feature.construct.entity.component.InteractionComponent
import coffee.cypher.hexbound.feature.construct.entity.component.ItemHolderComponent
import coffee.cypher.hexbound.util.provideDelegate
import coffee.cypher.hexbound.util.redirectSpiderLang
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionHand
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.constant.DefaultAnimations
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.util.GeckoLibUtil

class SpiderConstructEntity(
    entityType: EntityType<SpiderConstructEntity>,
    world: Level
) : AbstractConstructEntity(entityType, world), GeoEntity, ItemHolderComponent, InteractionComponent {
    override var heldStack by HELD_STACK
    var isAltModelEnabled by ALT_MODEL_ENABLED

    private var songSource: BlockPos? = null
    private val animationCache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)

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

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(ALT_MODEL_ENABLED, false)
        builder.define(HELD_STACK, ItemStack.EMPTY)
    }

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        val item = player.getItemInHand(hand).item

        if (player.isShiftKeyDown && (item == Items.IRON_BLOCK || item == Items.AMETHYST_BLOCK)) {
            if (!level().isClientSide) {
                val serverPlayer = player as ServerPlayer

                if (!serverPlayer.isSpectator) {
                    isAltModelEnabled = item == Items.IRON_BLOCK
                }
            } else {
                player.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value())
            }

            return InteractionResult.SUCCESS
        }

        return super.mobInteract(player, hand)
    }

    override fun tick() {
        super.tick()
        fakePlayer?.setItemInHand(InteractionHand.MAIN_HAND, heldStack)
    }

    override fun getInteractionPlayer(world: ServerLevel): ServerPlayer {
        return fakePlayer!!
    }

    override fun setItemSlot(slot: EquipmentSlot, stack: ItemStack) {
        if (slot == EquipmentSlot.MAINHAND) {
            heldStack = stack
        }
    }

    override fun getItemBySlot(slot: EquipmentSlot): ItemStack {
        return when (slot) {
            EquipmentSlot.MAINHAND -> heldStack
            else -> ItemStack.EMPTY
        }
    }

    override fun setRecordPlayingNearby(songPosition: BlockPos, playing: Boolean) {
        songSource = if (playing) songPosition else null
    }

    private fun canDance(): Boolean {
        val dist = songSource?.let { position().distanceToSqr(it.x + 0.5, it.y + 0.5, it.z + 0.5) }
        return dist != null && dist <= 36.0 && command == null
    }

    override fun getTypeName(): Component {
        val name = super.getTypeName()
        return redirectSpiderLang(name.string, this).let { Component.literal(it) }
    }

    override fun registerControllers(data: AnimatableManager.ControllerRegistrar) {
        data.add(animationController)
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache {
        return animationCache
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        super.addAdditionalSaveData(nbt)

        val registryAccess = this.level().registryAccess()
        nbt.put("held_stack", heldStack.save(registryAccess))
        nbt.putBoolean("alt_model", isAltModelEnabled)
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        super.readAdditionalSaveData(nbt)

        val registryAccess = this.level().registryAccess()
        heldStack = ItemStack.parseOptional(registryAccess, nbt.getCompound("held_stack"))
        isAltModelEnabled = nbt.getBoolean("alt_model")
    }

    companion object {
        fun createType(): EntityType<SpiderConstructEntity> {
            return EntityType.Builder.of(::SpiderConstructEntity, MobCategory.MISC)
                .sized(1.25f, 0.75f)
                .clientTrackingRange(8)
                .build("spider_construct")
        }

        fun createAttributes() = Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 10.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.FOLLOW_RANGE, 32.0)

        private val DANCE_ANIMATION = RawAnimation.begin().thenLoop("dance")

        val ALT_MODEL_ENABLED: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(
            SpiderConstructEntity::class.java,
            EntityDataSerializers.BOOLEAN
        )

        val HELD_STACK: EntityDataAccessor<ItemStack> = SynchedEntityData.defineId(
            SpiderConstructEntity::class.java,
            EntityDataSerializers.ITEM_STACK
        )
    }
}
