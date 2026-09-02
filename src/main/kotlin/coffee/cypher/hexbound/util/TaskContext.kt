package coffee.cypher.hexbound.util

interface TaskContext<T> {
    suspend fun <R> withContext(block: suspend T.() -> R): R
}
