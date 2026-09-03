package coffee.cypher.hexbound.feature.construct.command.exception

import coffee.cypher.hexbound.feature.construct.entity.component.ConstructComponentKey
import net.minecraft.network.chat.Component

class MissingComponentConstructCommandException(key: ConstructComponentKey<*>) : ConstructCommandException(
    Component.translatable("hexbound.construct.error.component_missing.${key.key}")
)
