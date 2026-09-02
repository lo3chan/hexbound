package coffee.cypher.hexbound.feature.construct.command.exception

import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.translatable

class UnknownConstructCommandException(val original: Throwable) : ConstructCommandException(
    buildText {
        translatable("hexbound.construct.exception.unknown_error", original.toString())
    }
)
