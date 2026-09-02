package coffee.cypher.hexbound.feature.construct.command.exception

import coffee.cypher.hexbound.util.formatVector
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.translatable

class BadTargetConstructCommandException(text: Text) : ConstructCommandException(text) {
    constructor(stub: String, vararg args: Any) : this(
        buildText {
            translatable("hexbound.construct.exception.bad_target.$stub", *args)
        }
    )

    constructor(pos: Vec3i, stub: String, vararg args: Any) : this(stub, formatVector(pos), *args)

    constructor(pos: Vec3d, stub: String, vararg args: Any) : this(stub, formatVector(pos), *args)

    constructor(target: Entity, stub: String, vararg args: Any) : this(stub, target.name, *args)
}
