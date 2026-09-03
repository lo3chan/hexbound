package coffee.cypher.hexbound.feature.construct.command.exception

import net.minecraft.network.chat.Component

class UnknownConstructCommandException(val original: Throwable) : ConstructCommandException(
    Component.translatable("hexbound.construct.exception.unknown_error", original.toString())
)
