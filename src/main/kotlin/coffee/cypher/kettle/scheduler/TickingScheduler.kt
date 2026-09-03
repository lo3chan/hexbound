package coffee.cypher.kettle.scheduler

import kotlin.coroutines.*

class TaskExecutionException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
    constructor(cause: Throwable?) : this(cause?.message, cause)
}

class Task<C>(
    val builder: TaskBuilder<C>,
    val executor: (TaskContext<C>) -> Unit
) {
    sealed class State {
        object Running : State()
        object Stopped : State()
    }

    var state: State = State.Running
    var continuation: Continuation<Unit>? = null
}

class TaskBuilder<C> {
    var yieldsAfterMs: Double = 5.0
    var start: Boolean = true
    var actionBlock: (suspend TaskContext<C>.() -> Unit)? = null

    infix fun run(block: TaskBuilder<C>.() -> Unit): TaskBuilder<C> {
        this.block()
        return this
    }

    infix fun once(block: () -> Unit) {
        block()
    }

    fun action(block: suspend TaskContext<C>.() -> Unit) {
        actionBlock = block
    }
}

class TickingScheduler<C> {
    private val tasks = mutableListOf<Task<C>>()

    fun task(config: TaskBuilder<C>.() -> Unit): Task<C> {
        val builder = TaskBuilder<C>().apply(config)
        val task = Task(builder) { ctx ->
            builder.actionBlock?.let { block ->
                val continuation = object : Continuation<Unit> {
                    override val context: CoroutineContext = EmptyCoroutineContext
                    override fun resumeWith(result: Result<Unit>) {
                        result.exceptionOrNull()?.let { throw TaskExecutionException(it) }
                    }
                }
                block.startCoroutine(ctx, continuation)
            }
        }
        tasks.add(task)
        return task
    }

    fun removeTask(task: Task<C>) {
        tasks.remove(task)
        task.state = Task.State.Stopped
    }

    fun tick(contextSupplier: () -> C) {
        val ctx = TaskContext(contextSupplier())
        val iter = tasks.iterator()
        while (iter.hasNext()) {
            val task = iter.next()
            try {
                if (task.continuation == null) {
                    task.executor(ctx)
                    task.continuation = TaskSchedulerState.activeContinuation
                    TaskSchedulerState.activeContinuation = null
                } else {
                    val cont = task.continuation
                    task.continuation = null
                    cont?.resume(Unit)
                    task.continuation = TaskSchedulerState.activeContinuation
                    TaskSchedulerState.activeContinuation = null
                }
            } catch (t: Throwable) {
                task.state = Task.State.Stopped
                iter.remove()
                throw t
            }
        }
    }
}
