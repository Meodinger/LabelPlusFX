package ink.meodinger.lpfx.util.property

import javafx.beans.WeakListener
import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import java.lang.ref.WeakReference


/**
 * Author: Meodinger
 * Date: 2021/12/13
 * Have fun with my code!
 */

/**
 * Base class for bidirectional bindings (listener based)
 * This class is a copy of com.sun.javafx.binding.BidirectionalBinding
 */
abstract class BidirectionalBinding<T>(
    property1: Property<T>,
    property2: Property<T>
) : ChangeListener<T>, WeakListener {

    companion object {
        private fun checkParameters(property1: Property<*>?, property2: Property<*>?) {
            requireNotNull(property1) { "Both properties must be specified." }
            requireNotNull(property2) { "Both properties must be specified." }
            require(property1 !== property2) { "Cannot bind property to itself" }
        }

        fun <T> unbind(property1: Property<T>?, property2: Property<T>?) {
            checkParameters(property1, property2)

            @Suppress("UNCHECKED_CAST")
            val binding = UntypedGenericBidirectionalBinding(
                property1 as Property<Any?>,
                property2 as Property<Any?>
            )
            property1.removeListener(binding)
            property2.removeListener(binding)
        }

        private class UntypedGenericBidirectionalBinding(
            propertyA: Property<Any?>,
            propertyB: Property<Any?>
        ): BidirectionalBinding<Any?>(propertyA, propertyB) {
            override val property1: Property<Any?> = propertyA
            override val property2: Property<Any?> = propertyB
            override fun changed(observable: ObservableValue<out Any?>?, oldValue: Any?, newValue: Any?) {
                throw RuntimeException("Should not reach here")
            }
        }
    }

    protected abstract val property1: Property<T>?
    protected abstract val property2: Property<T>?
    private val cachedHashCode by lazy { property1.hashCode() * property2.hashCode() }

    init { cachedHashCode }

    override fun wasGarbageCollected(): Boolean = property1 == null || property2 == null
    override fun hashCode(): Int = cachedHashCode
    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val propertyA1 = property1
        val propertyA2 = property2
        if (propertyA1 == null || propertyA2 == null) return false

        if (other is BidirectionalBinding<*>) {
            val propertyB1 = other.property1
            val propertyB2 = other.property2
            if (propertyB1 == null || propertyB2 == null) return false

            if (propertyA1 === propertyB1 && propertyA2 === propertyB2) return true
            if (propertyA1 === propertyB2 && propertyA2 === propertyB1) return true
        }

        return false
    }

}

/**
 * Base class for generic bidirectional bindings (listener based)
 * This class is a copy of com.sun.javafx.binding.BidirectionalBinding.TypedGenericBidirectionalBinding
 */
open class TypedGenericBidirectionalBinding<T> protected constructor(
    property1: Property<T>,
    property2: Property<T>
) : BidirectionalBinding<T>(property1, property2) {

    companion object {
        fun <T> bind(property1: Property<T>, property2: Property<T>) {
            TypedGenericBidirectionalBinding(property1, property2).also {
                property1.addListener(it)
                property2.addListener(it)
            }
        }
    }

    private val propertyRef1: WeakReference<Property<T>> = WeakReference(property1)
    private val propertyRef2: WeakReference<Property<T>> = WeakReference(property2)
    override val property1: Property<T>? get() = propertyRef1.get()
    override val property2: Property<T>? get() = propertyRef2.get()
    protected var updating = false

    override fun changed(sourceProperty: ObservableValue<out T>, oldValue: T, newValue: T) {
        if (updating) return

        val p1 = property1
        val p2 = property2
        if (p1 == null || p2 == null) {
            p1?.removeListener(this)
            p2?.removeListener(this)
        } else {
            try {
                updating = true
                if (p1 === sourceProperty) {
                    p2.setValue(newValue)
                } else {
                    p1.setValue(newValue)
                }
            } catch (e: RuntimeException) {
                try {
                    if (p1 === sourceProperty) {
                        p1.setValue(oldValue)
                    } else {
                        p2.setValue(oldValue)
                    }
                } catch (e2: Exception) {
                    e2.addSuppressed(e)
                    unbind(p1, p2)
                    throw RuntimeException("Bidirectional binding failed together with an attempt " +
                        "to restore the source property to the previous value. " +
                        "Removing the bidirectional binding from properties $p1 and $p2",
                        e2
                    )
                }
                throw RuntimeException("Bidirectional binding failed, setting to the previous value", e)
            } finally {
                updating = false
            }
        }
    }
}

/**
 * Provide a convenient way to bind two properties with processed new value
 *
 * Actually, this binding is to add a listener to both of the given observables.
 * Sometimes we don't want the raw new value to be applied to the other property,
 * that means we need to process it before pass to the other property.
 * @param rule1 Lambda to process o2's change
 * @param rule2 Lambda to process o2's change
 */
class RuledGenericBidirectionalBinding<T> private constructor(
    property1: Property<T>,
    private val rule1: (observable: Property<T>, oldV: T?, newV: T?, another: Property<T>) -> T,
    property2: Property<T>,
    private val rule2: (observable: Property<T>, oldV: T?, newV: T?, another: Property<T>) -> T
) : TypedGenericBidirectionalBinding<T>(property1, property2) {

    companion object {
        fun <T> bind(
            property1: Property<T>, rule1: (observable: Property<T>, oldV: T?, newV: T?, another: Property<T>) -> T,
            property2: Property<T>, rule2: (observable: Property<T>, oldV: T?, newV: T?, another: Property<T>) -> T
        ) {
            RuledGenericBidirectionalBinding(property1, rule1, property2, rule2).also {
                property1.addListener(it)
                property2.addListener(it)
            }
        }
    }

    override fun changed(sourceProperty: ObservableValue<out T>, oldValue: T, newValue: T) {
        if (updating) return

        val p1 = property1
        val p2 = property2
        if (p1 == null || p2 == null) {
            p1?.removeListener(this)
            p2?.removeListener(this)
        } else {
            val newRuledValue: T =
                if (p1 === sourceProperty)
                    rule1(p1, oldValue, newValue, p2)
                else
                    rule2(p2, oldValue, newValue, p1)

            // Use super to set value
            super.changed(sourceProperty, oldValue, newRuledValue)
        }
    }

}
