package coffee.cypher.hexbound.util

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import coffee.cypher.hexbound.feature.construct.casting.env.ConstructCastEnv
import coffee.cypher.hexbound.feature.construct.entity.AbstractConstructEntity
import coffee.cypher.hexbound.feature.construct.entity.SpiderConstructEntity
import coffee.cypher.hexbound.feature.construct.mishap.MishapNoConstruct
import coffee.cypher.hexbound.init.config.HexboundConfig
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.animal.allay.Allay
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.core.Vec3i
import java.text.DecimalFormat

val HexPattern.nonBlankSignature: String
    get() = anglesSignature().ifBlank { "empty" }

inline fun <reified T : Entity> List<Iota>.getEntityOfType(errorStub: String, index: Int, argc: Int = 0): T {
    val x = this.getOrElse(index) { throw MishapNotEnoughArgs(index + 1, this.size) }
    if (x is EntityIota) {
        val entity = x.entity
        if (entity is T) {
            return entity
        }
    }

    throw MishapInvalidIota.of(x, if (argc == 0) index else argc - (index + 1), errorStub)
}

fun List<Iota>.getConstruct(index: Int, argc: Int = 0): AbstractConstructEntity =
    getEntityOfType("entity.construct.generic", index, argc)

fun List<Iota>.getSpiderConstruct(index: Int, argc: Int = 0): SpiderConstructEntity =
    getEntityOfType("entity.construct.spider", index, argc)

fun List<Iota>.getAllay(index: Int, argc: Int = 0): Allay =
    getEntityOfType("entity.allay", index, argc)

fun CastingEnvironment.requireCaster(): ServerPlayer = caster as? ServerPlayer ?: throw MishapNoSpellCircle() //TODO better mishap

fun CastingEnvironment.requireConstruct(): ConstructCastEnv = this as? ConstructCastEnv ?: throw MishapNoConstruct()

/*
 * For now only used for display name on Robot version constructs
 */
fun redirectSpiderLang(original: String, entity: SpiderConstructEntity? = null): String {
    return if (entity?.isAltModelEnabled == true || HexboundConfig.replaceSpiderConstruct)
        original.replace("construct.spider", "construct.robot")
            .replace("spider_construct", "robot_construct")
    else
        original
}

fun redirectSpiderLang(original: Component, entity: SpiderConstructEntity? = null): Component {
    if (entity?.isAltModelEnabled != true && !HexboundConfig.replaceSpiderConstruct) {
        return original
    }

    return Component.translatable(redirectSpiderLang(original.string))
}

val DECIMAL_FORMAT = DecimalFormat("#0.#")

fun formatVector(vec: Vec3): Component {
    return Component.translatable(
        "hexbound.vector_format",
        DECIMAL_FORMAT.format(vec.x),
        DECIMAL_FORMAT.format(vec.y),
        DECIMAL_FORMAT.format(vec.z)
    )
}

fun formatVector(vec: Vec3i): Component {
    return Component.translatable(
        "hexbound.vector_format",
        vec.x.toString(),
        vec.y.toString(),
        vec.z.toString(),
    )
}

fun localizeSide(direction: Direction): Component {
    return Component.translatable("hexbound.direction.${direction.name.lowercase()}")
}
