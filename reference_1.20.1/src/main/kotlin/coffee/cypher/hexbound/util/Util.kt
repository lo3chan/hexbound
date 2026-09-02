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
import coffee.cypher.hexbound.mixins.accessor.MutableTextAccessor
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.AllayEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.component.TranslatableComponent
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.quiltmc.qkl.library.text.Color
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.color
import org.quiltmc.qkl.library.text.translatable
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

fun List<Iota>.getAllay(index: Int, argc: Int = 0): AllayEntity =
    getEntityOfType("entity.allay", index, argc)

fun CastingEnvironment.requireCaster(): ServerPlayerEntity = caster ?: throw MishapNoSpellCircle() //TODO better mishap

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

fun redirectSpiderLang(original: Text, entity: SpiderConstructEntity? = null): Text {
    if (entity?.isAltModelEnabled != true && !HexboundConfig.replaceSpiderConstruct) {
        return original
    }

    val component = original.asComponent()

    val newComponent = if (component is TranslatableComponent)
        TranslatableComponent(redirectSpiderLang(component.key), null, emptyArray())
    else
        component

    return MutableTextAccessor.create(
        newComponent,
        original.siblings.map(::redirectSpiderLang),
        original.style
    )
}

val DECIMAL_FORMAT = DecimalFormat("#0.#")

fun formatVector(vec: Vec3d): Text {
    return buildText {
        color(Color.RED) {
            translatable(
                "hexbound.vector_format",
                DECIMAL_FORMAT.format(vec.x),
                DECIMAL_FORMAT.format(vec.y),
                DECIMAL_FORMAT.format(vec.z)
            )
        }
    }
}

fun formatVector(vec: Vec3i): Text {
    return buildText {
        return buildText {
            color(Color.RED) {
                translatable(
                    "hexbound.vector_format",
                    vec.x.toString(),
                    vec.y.toString(),
                    vec.z.toString(),
                )
            }
        }
    }
}

fun localizeSide(direction: Direction): Text {
    return Text.translatable("hexbound.direction.${direction.name.lowercase()}")
}
