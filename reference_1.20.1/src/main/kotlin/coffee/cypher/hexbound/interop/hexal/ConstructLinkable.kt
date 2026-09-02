package coffee.cypher.hexbound.interop.hexal

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.asInt
import at.petrak.hexcasting.api.utils.asUUID
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import coffee.cypher.hexbound.init.Hexbound
import coffee.cypher.hexbound.util.mixinaccessor.construct
import dev.onyxstudios.cca.api.v3.component.ComponentV3
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtInt
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import ram.talia.hexal.api.linkable.ClientLinkableHolder
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.api.linkable.ServerLinkableHolder
import java.util.*

class ConstructLinkable(val construct: AbstractConstructEntity) : ILinkable,
    ILinkable.IRenderCentre,
    ClientTickingComponent,
    ServerTickingComponent,
    ComponentV3 {
    override val asActionResult = listOf(EntityIota(construct))

    override val linkableHolder = if (!construct.world.isClient)
        ServerLinkableHolder(this, construct.world as ServerWorld)
    else
        null

    override fun acceptMedia(other: ILinkable, sentMedia: Int) {
    }

    override fun canAcceptMedia(other: ILinkable, otherMediaLevel: Int): Int {
        return -1
    }

    override fun currentMediaLevel(): Int {
        return 0
    }

    override fun owner(): UUID {
        return construct.uuid
    }

    override val clientLinkableHolder = if (construct.world.isClient)
        ClientLinkableHolder(this, construct.world, construct.random)
    else
        null

    override fun getLinkableType(): LinkableRegistry.LinkableType<ConstructLinkable, *> {
        return ConstructLinkableType
    }

    override fun getPosition(): Vec3d {
        return construct.pos
    }

    override fun maxSqrLinkRange(): Double {
        return Action.MAX_DISTANCE * Action.MAX_DISTANCE
    }

    override fun shouldRemove(): Boolean {
        return construct.isRemoved && construct.removalReason?.shouldDestroy() == true
    }

    override fun colouriser(): FrozenColorizer {
        return FrozenColorizer.DEFAULT.get()
    }

    override fun renderCentre(other: ILinkable.IRenderCentre, recursioning: Boolean): Vec3d {
        return construct.boundingBox.center
    }

    override fun readFromNbt(tag: NbtCompound) {
        if (linkableHolder != null) {
            if (tag.contains("linkable_holder")) {
                linkableHolder.readFromNbt(tag.getCompound("linkable_holder"))
            }
        }
    }

    override fun writeToNbt(tag: NbtCompound) {
        if (linkableHolder != null) {
            tag.put("linkable_holder", linkableHolder.writeToNbt())
        }
    }

    override fun clientTick() {
        clientLinkableHolder?.renderLinks()
    }

    override fun serverTick() {
        checkLinks()
    }
}

object ConstructLinkableType :
    LinkableRegistry.LinkableType<ConstructLinkable, ConstructLinkable>(Hexbound.id("linkable_construct")) {
    override val canCast = true
    override val castingContextPriority = 0
    override val iotaPriority = 0

    override fun fromNbt(tag: NbtElement, level: ServerWorld): ConstructLinkable? {
        return (level.getEntity(tag.asUUID) as? AbstractConstructEntity)?.getLinkable()
    }

    override fun fromSync(tag: NbtElement, level: World): ConstructLinkable? {
        return (level.getEntityById(tag.asInt) as? AbstractConstructEntity)?.getLinkable()
    }

    override fun linkableFromCastingEnvironment(ctx: CastingEnvironment): ConstructLinkable? {
        return ctx.construct?.getLinkable()
    }

    override fun linkableFromIota(iota: Iota, level: ServerWorld): ConstructLinkable? {
        return ((iota as? EntityIota)?.entity as? AbstractConstructEntity)?.getLinkable()
    }

    override fun matchSync(centre: ILinkable.IRenderCentre, tag: NbtElement): Boolean {
        return (centre as? ConstructLinkable)?.construct?.id == tag.asInt
    }

    override fun toNbt(linkable: ILinkable): NbtElement {
        return NbtHelper.fromUuid((linkable as ConstructLinkable).construct.uuid)
    }

    override fun toSync(linkable: ILinkable): NbtElement {
        return NbtInt.of((linkable as ConstructLinkable).construct.id)
    }
}
