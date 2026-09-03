package coffee.cypher.hexbound.feature.construct.entity

import com.mojang.authlib.GameProfile
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.neoforged.neoforge.common.util.FakePlayer
import java.util.*

class ConstructFakePlayer(
    level: ServerLevel,
    val construct: AbstractConstructEntity
) : FakePlayer(level, CONSTRUCT_PROFILE) {
    override fun getEyeY(): Double {
        return construct.eyeY
    }

    override fun getDisplayName(): Component {
        return construct.displayName
    }

    fun resetToValidState() {}

    companion object {
        val CONSTRUCT_UUID: UUID = UUID.fromString("e4d9ffe8-8f9b-4fda-839f-c854f8771f0c")
        val CONSTRUCT_PROFILE = GameProfile(CONSTRUCT_UUID, "[Construct]")
    }
}
