package coffee.cypher.hexbound.feature.construct.command.exception

import coffee.cypher.hexbound.util.formatVector
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

class BadTargetConstructCommandException(text: Component) : ConstructCommandException(text) {
    constructor(stub: String, vararg args: Any) : this(
        Component.translatable("hexbound.construct.exception.bad_target.$stub", *args)
    )

    constructor(pos: Vec3i, stub: String, vararg args: Any) : this(stub, formatVector(pos), *args)

    constructor(pos: Vec3, stub: String, vararg args: Any) : this(stub, formatVector(pos), *args)

    constructor(target: Entity, stub: String, vararg args: Any) : this(stub, target.displayName ?: Component.literal("Unknown"), *args)
}
