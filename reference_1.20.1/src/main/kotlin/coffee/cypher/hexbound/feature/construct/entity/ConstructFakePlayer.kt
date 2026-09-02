package coffee.cypher.hexbound.feature.construct.entity

import coffee.cypher.hexbound.init.Hexbound
import coffee.cypher.hexbound.util.HexboundFakePlayer
import com.mojang.authlib.GameProfile
import dev.cafeteria.fakeplayerapi.server.FakePlayerBuilder
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import java.util.*

class ConstructFakePlayer(
    world: ServerWorld,
    val construct: AbstractConstructEntity
) : HexboundFakePlayer(CONSTRUCT_BUILDER, world.server, world, CONSTRUCT_PROFILE) {
    override fun getEyeY(): Double {
        return construct.eyeY
    }

    override fun getDisplayName(): Text {
        return construct.displayName
    }

    companion object {
        val CONSTRUCT_UUID: UUID = UUID.fromString("e4d9ffe8-8f9b-4fda-839f-c854f8771f0c")
        val CONSTRUCT_PROFILE = GameProfile(CONSTRUCT_UUID, "Construct")
        val CONSTRUCT_BUILDER = FakePlayerBuilder(Hexbound.id("construct_fake_player"))
    }
}
