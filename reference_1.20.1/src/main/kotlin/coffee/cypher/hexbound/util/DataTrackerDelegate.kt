package coffee.cypher.hexbound.util

import net.minecraft.entity.Entity
import net.minecraft.entity.data.TrackedData
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class DataTrackerDelegate<T: Entity, V : Any>(
    private val entity: T,
    private val data: TrackedData<V>
): ReadWriteProperty<T, V> {
    override fun getValue(thisRef: T, property: KProperty<*>): V {
        return entity.dataTracker.get(data)
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        entity.dataTracker.set(data, value)
    }
}

operator fun <T : Entity, V : Any> TrackedData<V>.provideDelegate(
    thisRef: T,
    prop: KProperty<*>
) : DataTrackerDelegate<T, V> {
    return DataTrackerDelegate(thisRef, this)
}
