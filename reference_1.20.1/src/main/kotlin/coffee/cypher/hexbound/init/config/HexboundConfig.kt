package coffee.cypher.hexbound.init.config

import coffee.cypher.hexbound.init.Hexbound
import net.minecraft.util.Identifier

object HexboundConfig {
    val replaceSpiderConstruct: Boolean by configField()
    val spiderBatteryChargeRequired: Int by configField()
    val broadcasterParticleAmount: Int by configField()
    val constructPathfindingAttempts: Int by configField()

    private val constructActionDenyList: List<String> by configField()

    private val constructActionDenyCache = mutableMapOf<Identifier, Boolean>()
    private var constructActionRegex: List<Regex>? = null

    fun isActionForbiddenForConstruct(action: Identifier): Boolean {
        if (action in constructActionDenyCache) {
            return constructActionDenyCache.getValue(action)
        }

        if (constructActionRegex == null) {
            constructActionRegex = constructActionDenyList.map {
                it.split('*').joinToString(".*", transform = Regex::escape, prefix = "^", postfix = "$").toRegex()
            }
        }

        val regexList = constructActionRegex!!

        val result = regexList.any { it.matches(action.toString()) }

        constructActionDenyCache[action] = result

        return result
    }

    val CONFIG = buildConfig(Hexbound.id("config")) {
        section("client") {
            value("force_alternate_construct") {
                comment("Force Spider Construct to always use the alternative Robot model")
                bind(::replaceSpiderConstruct)

                defaultValue(false)
            }

            value("broadcaster_particle_amount") {
                comment("How many particles an activated Construct Broadcaster fires off")
                bind(::broadcasterParticleAmount)

                defaultValue(16)

                allowedRange(0..200)
            }
        }

        section("misc") {
            value("spider_battery_charge") {
                comment("Changes the required media charge for a Spider Construct Battery to fully charge (in amethyst dust units)")
                bind(::spiderBatteryChargeRequired)

                defaultValue(50)

                allowedRange(1..Int.MAX_VALUE)
            }

            value("construct_pathfinding_attempts") {
                comment("If a construct fails to get reasonably close to the target, it can try pathfinding to it again.")
                comment("Increasing this value will make constructs reaching their target more consistent, but can lead to trying to reach unreachable targets for too long, or cause lag.")
                bind(::constructPathfindingAttempts)

                defaultValue(3)

                allowedRange(1..10)
            }
        }

        section("execution") {
            listValue("construct_action_deny_list") {
                comment("Identifiers of pattern actions that should never be executed by Constructs.")
                comment("By default, includes most spells (should a Construct somehow acquire media to cast one) and sending orders to other Constructs or Hexal Wisps.")
                comment("Additionally, any entry containing a star will match any text (including empty strings) in that position.")
                bind(::constructActionDenyList)

                fallbackValue("")

                defaultValue(
                    "hexbound:instructions/*",
                    "hexbound:colorizer/*",
                    "hexcasting:explode*",
                    "hexcasting:add_motion",
                    "hexcasting:blink",
                    "hexcasting:break_block",
                    "hexcasting:place_block",
                    "hexcasting:craft/*",
                    "hexcasting:recharge",
                    "hexcasting:erase",
                    "hexcasting:*_water",
                    "hexcasting:ignite",
                    "hexcasting:extinguish",
                    "hexcasting:conjure_*",
                    "hexcasting:bonemeal",
                    "hexcasting:edify",
                    "hexcasting:colorize",
                    "hexcasting:sentinel/*",
                    "hexcasting:potion/*",
                    "hexal:everbook/*",
                    "hexal:wisp/summon/*",
                    "hexal:wisp/seon/*",
                    "hexal:wisp/consume",
                    "hexal:smelt",
                    "hexal:place_type",
                    "hexal:freeze",
                    "hexal:falling_block"
                )
            }
        }
    }

    fun init() {
        CONFIG.registerCallback {
            constructActionDenyCache.clear()
            constructActionRegex = null
        }
    }
}
