package coffee.cypher.hexbound.mixins;

import at.petrak.hexcasting.common.casting.actions.selectors.OpGetEntitiesBy;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OpGetEntitiesBy.Companion.class)
public class OpGetEntitiesByMixin {
    private boolean hexbound$huntersUnselectable(boolean original) {
        return original && true; //TODO check for hunter armor
    }
}
