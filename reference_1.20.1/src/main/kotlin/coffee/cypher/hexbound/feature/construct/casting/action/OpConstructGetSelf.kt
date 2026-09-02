package coffee.cypher.hexbound.feature.construct.casting.action

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import coffee.cypher.hexbound.util.requireConstruct

object OpConstructGetSelf : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val construct = env.requireConstruct().construct

        return listOf(EntityIota(construct))
    }
}
