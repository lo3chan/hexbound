package coffee.cypher.hexbound.util

import com.mojang.authlib.GameProfile
import dev.cafeteria.fakeplayerapi.server.FakePlayerBuilder
import dev.cafeteria.fakeplayerapi.server.FakeServerPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld

abstract class HexboundFakePlayer(
    builder: FakePlayerBuilder,
    server: MinecraftServer,
    world: ServerWorld,
    profile: GameProfile
) : FakeServerPlayer(builder, server, world, profile) {
    fun resetToValidState() {
        unsetRemoved()
        health = maxHealth
    }
}
