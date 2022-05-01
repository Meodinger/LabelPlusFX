@file:Suppress("KDocMissingDocumentation")

package ink.meodinger.lpfx.util.property

import javafx.beans.property.*
import javafx.beans.value.*
import javafx.collections.*
import kotlin.reflect.KProperty

/**
 * Author: Meodinger
 * Date: 2021/9/27
 * Have fun with my code!
 */

/**
 * Polyfill TornadoFX properties.kt
 */

operator fun <T> ObservableObjectValue<T>.getValue(thisRef: Any, property: KProperty<*>): T = get()
operator fun <T> ObjectProperty<T>.setValue(thisRef: Any, property: KProperty<*>, value: T) = set(value)

operator fun <K, V> ObservableMapValue<K, V>.getValue(thisRef: Any, property: KProperty<*>): ObservableMap<K, V> = get()
operator fun <K, V> MapProperty<K, V>.setValue(thisRef: Any, property: KProperty<*>, value: ObservableMap<K, V>) = set(value)

operator fun <E> ObservableListValue<E>.getValue(thisRef: Any, property: KProperty<*>): ObservableList<E> = get()
operator fun <E> ListProperty<E>.setValue(thisRef: Any, property: KProperty<*>, value: ObservableList<E>) = set(value)

operator fun <E> ObservableSetValue<E>.getValue(thisRef: Any, property: KProperty<*>): ObservableSet<E> = get()
operator fun <E> SetProperty<E>.setValue(thisRef: Any, property: KProperty<*>, value: ObservableSet<E>) = set(value)

operator fun ObservableStringValue.getValue(thisRef: Any, property: KProperty<*>): String = get()
operator fun StringProperty.setValue(thisRef: Any, property: KProperty<*>, value: String) = set(value)

operator fun ObservableBooleanValue.getValue(thisRef: Any, property: KProperty<*>): Boolean = get()
operator fun BooleanProperty.setValue(thisRef: Any, property: KProperty<*>, value: Boolean) = set(value)

operator fun ObservableIntegerValue.getValue(thisRef: Any, property: KProperty<*>): Int = get()
operator fun IntegerProperty.setValue(thisRef: Any, property: KProperty<*>, value: Int) = set(value)

operator fun ObservableLongValue.getValue(thisRef: Any, property: KProperty<*>): Long = get()
operator fun LongProperty.setValue(thisRef: Any, property: KProperty<*>, value: Long) = set(value)

operator fun ObservableFloatValue.getValue(thisRef: Any, property: KProperty<*>): Float = get()
operator fun FloatProperty.setValue(thisRef: Any, property: KProperty<*>, value: Float) = set(value)

operator fun ObservableDoubleValue.getValue(thisRef: Any, property: KProperty<*>): Double = get()
operator fun DoubleProperty.setValue(thisRef: Any, property: KProperty<*>, value: Double) = set(value)

// These delegations may cause their observable backing collection not observable.
// Checkout commit@c8a13e8 to reproduce the issue. (PicNamesBinding not update when pic removed/added)
// The reason is unclear now, but I'll find out some time.
//
// operator fun <K, V> ObservableMap<K, V>.getValue(thisRef: Any, property: KProperty<*>): Map<K, V> = toMap()
// operator fun <E> ObservableList<E>.getValue(thisRef: Any, property: KProperty<*>): List<E> = toList()
// operator fun <E> ObservableSet<E>.getValue(thisRef: Any, property: KProperty<*>): Set<E> = toSet()
