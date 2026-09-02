package coffee.cypher.hexbound.feature.construct.command

import coffee.cypher.hexbound.feature.construct.command.exception.BadTargetConstructCommandException
import coffee.cypher.hexbound.feature.construct.command.execution.ConstructCommandContext
import coffee.cypher.hexbound.init.HexboundData
import coffee.cypher.hexbound.init.config.HexboundConfig
import coffee.cypher.hexbound.util.formatVector
import coffee.cypher.kettle.scheduler.TaskContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

@Serializable
class MoveTo(
    @Contextual val targetPos: Vec3d
) : ConstructCommand<MoveTo> {
    override fun getType() = HexboundData.ConstructCommandTypes.MOVE_TO

    override suspend fun TaskContext<out ConstructCommandContext>.execute() {
        withContext {
            maintain {
                if (construct.squaredDistanceTo(targetPos) > 32 * 32) {
                    throw BadTargetConstructCommandException(targetPos, "too_far")
                }
            }

            construct.navigation.stop()

            repeat(HexboundConfig.constructPathfindingAttempts) {
                val path = construct.navigation.findPathTo(targetPos.x, targetPos.y, targetPos.z, 1)
                           ?: throw BadTargetConstructCommandException(targetPos, "no_path_found")

                construct.navigation.startMovingAlong(path, 1.0)

                waitUntil(checkEvery = 4) {
                    !construct.isNavigating || construct.squaredDistanceTo(targetPos) < 1.25 * 1.25
                }

                if (construct.isNavigating) {
                    var checksMade = 0
                    waitUntil(checkEvery = 2) {
                        !construct.isNavigating || checksMade++ >= 10
                    }
                }

                construct.navigation.stop()

                if (construct.squaredDistanceTo(targetPos) <= 1.75 * 1.75) {
                    return
                }
            }

            if (construct.squaredDistanceTo(targetPos) > 4) {
                throw BadTargetConstructCommandException(targetPos, "could_not_reach")
            }
        }
    }

    override fun display(world: ServerWorld): Text {
        return Text.translatable("hexbound.construct.command.move_to", formatVector(targetPos))
    }
}
