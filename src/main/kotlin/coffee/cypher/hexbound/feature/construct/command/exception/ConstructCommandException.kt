package coffee.cypher.hexbound.feature.construct.command.exception

import net.minecraft.network.chat.Component

open class ConstructCommandException(val errorText: Component) : Exception()
