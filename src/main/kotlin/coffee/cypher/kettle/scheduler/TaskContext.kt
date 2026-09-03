package coffee.cypher.kettle.scheduler

import kotlin.coroutines.*

class TaskContext<C>(val context: C) {
    var yieldsAfterMs: Double = 5.0
    var start: Boolean = true

    inline fun <R> withContext(block: C.() -> R): R = context.block()

    suspend fun waitUntil(checkEvery: Int = 1, condition: () -> Boolean) {
        while (!condition()) {
            repeat(checkEvery) { yield() }
        }
    }

    suspend fun sleep(ticks: Int) {
        repeat(ticks) { yield() }
    }

    suspend fun yield() = suspendCoroutine<Unit> { cont ->
        TaskSchedulerState.activeContinuation = cont
    }
}

object TaskSchedulerState {
    var activeContinuation: Continuation<Unit>? = null
}
