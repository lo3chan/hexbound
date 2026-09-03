package coffee.cypher.hexbound.feature.construct.command.execution

import coffee.cypher.hexbound.feature.construct.command.exception.MissingComponentConstructCommandException
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import coffee.cypher.hexbound.feature.construct.entity.component.ConstructComponentKey
import net.minecraft.server.level.ServerLevel

class ConstructCommandContext(val construct: AbstractConstructEntity, val world: ServerLevel) {
    internal val requirements = mutableListOf<() -> Unit>()

    fun <T : Any> requireComponent(key: ConstructComponentKey<T>): T {
        return construct.getComponent(key) ?: throw MissingComponentConstructCommandException(key)
    }

    fun maintain(condition: () -> Unit) {
        condition()

        requirements += condition
    }
}
