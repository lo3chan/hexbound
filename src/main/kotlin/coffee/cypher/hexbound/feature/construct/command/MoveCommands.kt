package coffee.cypher.hexbound.feature.construct.command

import coffee.cypher.hexbound.feature.construct.command.exception.BadTargetConstructCommandException
import coffee.cypher.hexbound.feature.construct.command.execution.ConstructCommandContext
import coffee.cypher.hexbound.init.HexboundData
import coffee.cypher.hexbound.util.formatVector
import coffee.cypher.kettle.scheduler.TaskContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

@Serializable
class MoveTo(
    @Contextual val targetPos: Vec3
) : ConstructCommand<MoveTo> {
    override fun getType() = HexboundData.ConstructCommandTypes.MOVE_TO

    override suspend fun TaskContext<out ConstructCommandContext>.execute() {
        withContext {
            maintain {
                if (construct.distanceToSqr(targetPos) > 32 * 32) {
                    throw BadTargetConstructCommandException(targetPos, "too_far")
                }
            }

            construct.navigation.stop()

            repeat(5) {
                val path = construct.navigation.createPath(targetPos.x, targetPos.y, targetPos.z, 1)
                           ?: throw BadTargetConstructCommandException(targetPos, "no_path_found")

                construct.navigation.moveTo(path, 1.0)

                waitUntil(checkEvery = 4) {
                    !construct.navigation.isInProgress || construct.distanceToSqr(targetPos) < 1.25 * 1.25
                }

                if (construct.navigation.isInProgress) {
                    var checksMade = 0
                    waitUntil(checkEvery = 2) {
                        !construct.navigation.isInProgress || checksMade++ >= 10
                    }
                }

                construct.navigation.stop()

                if (construct.distanceToSqr(targetPos) <= 1.75 * 1.75) {
                    return
                }
            }

            if (construct.distanceToSqr(targetPos) > 4) {
                throw BadTargetConstructCommandException(targetPos, "could_not_reach")
            }
        }
    }

    override fun display(world: ServerLevel): Component {
        return Component.translatable("hexbound.construct.command.move_to", formatVector(targetPos))
    }
}
