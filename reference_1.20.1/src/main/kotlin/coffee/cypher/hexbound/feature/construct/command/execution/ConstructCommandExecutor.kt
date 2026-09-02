package coffee.cypher.hexbound.feature.construct.command.execution

import coffee.cypher.hexbound.feature.construct.command.ConstructCommand
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import coffee.cypher.kettle.scheduler.*
import net.minecraft.server.world.ServerWorld

class ConstructCommandExecutor(
    val construct: AbstractConstructEntity,
    val world: ServerWorld,
    val onComplete: () -> Unit,
    val onError: (Throwable) -> Unit
) {
    private val scheduler = TickingScheduler<ConstructCommandContext>()
    private var currentTask: Task<ConstructCommandContext>? = null
    private val context = ConstructCommandContext(construct, world)

    fun startCommand(command: ConstructCommand<*>) {
        cancelCommand()

        currentTask = scheduler.task {
            run once {
                yieldsAfterMs = 5.0
            }

            start = true

            action {
                with(command) {
                    execute()
                }

                onComplete()
            }
        }
    }

    fun cancelCommand() {
        currentTask?.let { scheduler.removeTask(it) }
        context.requirements.clear()
        currentTask = null
    }

    fun tick() {
        try {
            context.requirements.forEach {
                it.invoke()
            }

            scheduler.tick({ context })
        } catch (e: Throwable) {
            val realError = if (e is TaskExecutionException) {
                e.cause ?: e
            } else {
                e
            }
            onError(realError)
        }

        if (currentTask?.state is Task.State.Stopped) {
            scheduler.removeTask(currentTask!!)
            currentTask = null
        }
    }
}
