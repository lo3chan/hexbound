package coffee.cypher.hexbound.util

import com.mojang.authlib.GameProfile
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.neoforged.neoforge.common.util.FakePlayerFactory

object HexboundFakePlayer {
    fun create(world: ServerLevel, profile: GameProfile): ServerPlayer {
        val player = FakePlayerFactory.get(world, profile)
        player.health = player.maxHealth
        return player
    }
}
