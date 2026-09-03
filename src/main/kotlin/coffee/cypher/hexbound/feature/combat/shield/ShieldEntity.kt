package coffee.cypher.hexbound.feature.combat.shield

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.Util
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.BiFunction
import kotlin.math.abs

class ShieldEntity(
    type: EntityType<ShieldEntity>,
    level: Level,
    val owner: Player? = null,
    val maxAge: Int = 200,
    initialVisualType: VisualType = VisualType.REGULAR
) : Entity(type, level) {

    private val pigmentMemo = Util.memoize(FrozenPigment::fromNBT)
    private val basisMemo = Util.memoize(BiFunction(ShieldEntity::calculateBasis))

    private var lockedPosition: Triple<Vec3, Float, Float>? = null

    fun lockPosition() {
        lockedPosition = Triple(position(), xRot, yRot)
    }

    var pigment: FrozenPigment
        get() = pigmentMemo.apply(entityData.get(COLORIZER))
        set(value) {
            entityData.set(COLORIZER, value.serializeToNBT())
        }

    var visualType: VisualType
        get() = VisualType.values()[entityData.get(VISUAL_TYPE)]
        set(value) {
            entityData.set(VISUAL_TYPE, value.ordinal)
        }

    init {
        pigment = if (owner != null) {
            IXplatAbstractions.INSTANCE.getPigment(owner)
        } else {
            FrozenPigment.DEFAULT.get()
        }

        visualType = initialVisualType
    }

    override fun isPickable(): Boolean = true

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        builder.define(COLORIZER, FrozenPigment.DEFAULT.get().serializeToNBT())
        builder.define(VISUAL_TYPE, 0)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {}

    override fun addAdditionalSaveData(compound: CompoundTag) {}

    fun getBasis(): Triple<Vec3, Vec3, Vec3> {
        return basisMemo.apply(xRot, yRot)
    }

    override fun tick() {
        super.tick()

        if (owner?.isRemoved == true || owner?.isAlive == false) {
            discard()
        }

        if (!level().isClientSide && tickCount > maxAge) {
            discard()
        }

        if (tickCount == DEPLOY_TIME && level().isClientSide) {
            val (_, up, right) = getBasis()

            listOf(
                right.scale(-1.5).add(up.scale(1.625)),
                right.scale(-1.5).add(up),
                right.scale(1.5).add(up.scale(1.625)),
                right.scale(1.5).add(up)
            ).forEach {
                level().addParticle(
                    ConjureParticleOptions(pigment.colorProvider.getColor(level().gameTime.toFloat(), it)),
                    x + it.x, y + it.y, z + it.z,
                    0.0, 0.0, 0.0
                )
            }
        }

        lockedPosition?.let { (lockedPos, lockedPitch, lockedYaw) ->
            if (lockedPos.distanceTo(position()) > 0.1 || abs(xRot - lockedPitch) % 360 > 2 || abs(yRot - lockedYaw) % 360 > 2) {
                discard()
            }
        }
    }

    companion object {
        const val DEPLOY_TIME = 3

        val COLORIZER: EntityDataAccessor<CompoundTag> =
            SynchedEntityData.defineId(ShieldEntity::class.java, EntityDataSerializers.COMPOUND_TAG)

        val VISUAL_TYPE: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(ShieldEntity::class.java, EntityDataSerializers.INT)

        fun createType(): EntityType<ShieldEntity> {
            return EntityType.Builder.of({ type, world -> ShieldEntity(type, world, null, 200, VisualType.REGULAR) }, MobCategory.MISC)
                .fireImmune()
                .noSave()
                .noSummon()
                .sized(3f, 2.625f)
                .clientTrackingRange(10)
                .build("shield")
        }

        @JvmStatic
        fun canBypassShieldForDirection(direction: Vec3, shield: Entity): Boolean {
            if (shield !is ShieldEntity) {
                return false
            }

            return direction.dot(Vec3.directionFromRotation(shield.xRot, shield.yRot)) >= 0
        }

        private fun calculateBasis(pitch: Float, yaw: Float): Triple<Vec3, Vec3, Vec3> {
            val forward = Vec3.directionFromRotation(pitch, yaw)

            val up = Vec3(0.0, 1.0, 0.0)
                .xRot(pitch * Math.PI.toFloat() / 180f)
                .yRot(yaw * Math.PI.toFloat() / 180f)

            val right = forward.cross(up)

            return Triple(forward, up, right)
        }
    }

    enum class VisualType {
        REGULAR, GLITCHY
    }
}
