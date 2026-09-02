package coffee.cypher.hexbound.util

import net.minecraft.world.entity.Entity
import net.minecraft.network.syncher.EntityDataAccessor
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class DataTrackerDelegate<T: Entity, V : Any>(
    private val entity: T,
    private val data: EntityDataAccessor<V>
): ReadWriteProperty<T, V> {
    override fun getValue(thisRef: T, property: KProperty<*>): V {
        return entity.entityData.get(data)
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        entity.entityData.set(data, value)
    }
}

operator fun <T : Entity, V : Any> EntityDataAccessor<V>.provideDelegate(
    thisRef: T,
    prop: KProperty<*>
) : DataTrackerDelegate<T, V> {
    return DataTrackerDelegate(thisRef, this)
}
