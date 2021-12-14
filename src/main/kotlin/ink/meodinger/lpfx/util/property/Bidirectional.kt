package ink.meodinger.lpfx.util.property

import javafx.beans.WeakListener
import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import java.lang.ref.WeakReference
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/12/13
 * Have fun with my code!
 */

/**
 * Base class for bidirectional bindings (listener based)
 *
 * All implementations should not have public constructors
 *
 * This class is a copy of com.sun.javafx.binding.BidirectionalBinding.
 * It functions as the origin class but doesn't have constructors to calc HashCode
 */
abstract class BidirectionalBinding<T> : ChangeListener<T>, WeakListener {

    companion object {
        private fun checkParameters(property1: Property<*>?, property2: Property<*>?) {
            Objects.requireNonNull(property1, "Both properties must be specified.")
            Objects.requireNonNull(property2, "Both properties must be specified.")
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
            private val propertyA: Property<Any?>,
            private val propertyB: Property<Any?>
        ): BidirectionalBinding<Any?>() {
            override val property1: Property<Any?> get() = propertyA
            override val property2: Property<Any?> get() = propertyB
            override fun changed(observable: ObservableValue<out Any?>?, oldValue: Any?, newValue: Any?) {
                throw RuntimeException("Should not reach here")
            }
        }
    }

    protected abstract val property1: Property<T>?
    protected abstract val property2: Property<T>?
    private val cachedHashCode by lazy { property1!!.hashCode() * property2!!.hashCode() }

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

open class TypedGenericBidirectionalBinding<T> protected constructor(
    property1: Property<T>,
    property2: Property<T>
) : BidirectionalBinding<T>() {

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
class RuledGenericBidirectionalBinding<T> private constructor(
    property1: Property<T>,
    private val rule1: (observable: Property<T>, oldV: T?, newV: T?, Property<T>) -> T,
    property2: Property<T>,
    private val rule2: (observable: Property<T>, oldV: T?, newV: T?, Property<T>) -> T
) : TypedGenericBidirectionalBinding<T>(property1, property2) {

    companion object {
        fun <T> bind(
            property1: Property<T>, rule1: (observable: Property<T>, oldV: T?, newV: T?, Property<T>) -> T,
            property2: Property<T>, rule2: (observable: Property<T>, oldV: T?, newV: T?, Property<T>) -> T
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
                if (p1 === sourceProperty) rule1(p1, oldValue, newValue, p2)
                else rule2(p2, oldValue, newValue, p1)
            super.changed(sourceProperty, oldValue, newRuledValue)
        }
    }

}

/**
 * Provide a convenient way to bind two ReadonlyProperty (or other) (by lambdas)
 *
 * Actually, this binding is to add a listener to both of the given observables.
 * Sometimes a property cannot be changed by its property but can via another way.
 * (Like the selectedIndexProperty of ComboBox) So this Binding is useful when
 * you don't want to manually add listener to the properties to be bound together
 * @param lambda1 Lambda to change o2 when o1 change
 * @param lambda2 Lambda to change o1 when o2 change
 */
class BidirectionalBindingByLambda<T>(
    observable1: ObservableValue<T>,
    private val lambda1: (observable: ObservableValue<T>, oldV: T?, newV: T?) -> Unit,
    observable2: ObservableValue<T>,
    private val lambda2: (observable: ObservableValue<T>, oldV: T?, newV: T?) -> Unit,
) : ChangeListener<T>, WeakListener {

    companion object {
        fun <T> bind(
            observable1: ObservableValue<T>, lambda1: (observable: ObservableValue<T>, oldV: T?, newV: T?) -> Unit,
            observable2: ObservableValue<T>, lambda2: (observable: ObservableValue<T>, oldV: T?, newV: T?) -> Unit
        ) {
            BidirectionalBindingByLambda(observable1, lambda1, observable2, lambda2).also {
                observable1.addListener(it)
                observable2.addListener(it)
            }
        }

        fun <T> unbind(observable1: ObservableValue<T>?, observable2: ObservableValue<T>?) {
            Objects.requireNonNull(observable1, "Both properties must be specified.")
            Objects.requireNonNull(observable2, "Both properties must be specified.")
            require(observable1 !== observable2) { "Cannot bind property to itself" }

            @Suppress("UNCHECKED_CAST")
            val binding = BidirectionalBindingByLambda(
                observable1 as ObservableValue<Any?>, { _, _, _ -> throw RuntimeException("Should not reach here") },
                observable2 as ObservableValue<Any?>, { _, _, _ -> throw RuntimeException("Should not reach here") },
            )
            observable1.removeListener(binding)
            observable2.removeListener(binding)
        }
    }

    private val observableRef1: WeakReference<ObservableValue<T>> = WeakReference(observable1)
    private val observableRef2: WeakReference<ObservableValue<T>> = WeakReference(observable2)
    private val observable1: ObservableValue<T>? get() = observableRef1.get()
    private val observable2: ObservableValue<T>? get() = observableRef2.get()
    private val cachedHashCode: Int by lazy { observable1.hashCode() * observable2.hashCode() }
    private var updating: Boolean = false

    init { cachedHashCode }

    override fun changed(sourceProperty: ObservableValue<out T>?, oldValue: T, newValue: T) {
        if (updating) return

        val o1 = observableRef1.get()
        val o2 = observableRef2.get()
        if (o1 == null || o2 == null) {
            o1?.removeListener(this)
            o2?.removeListener(this)
        } else {
            try {
                updating = true
                if (o1 === sourceProperty) {
                    lambda1(o1, oldValue, newValue)
                } else {
                    lambda2(o2, oldValue, newValue)
                }
            } catch (e: RuntimeException) {
                throw RuntimeException("Bidirectional binding lambda invoke failed", e)
            } finally {
                updating = false
            }
        }
    }

    override fun wasGarbageCollected(): Boolean = observable1 == null || observable2 == null
    override fun hashCode(): Int = cachedHashCode
    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val oA1 = observable1
        val oA2 = observable2
        if (oA1 == null || oA2 == null) return false

        if (other is BidirectionalBindingByLambda<*>) {
            val oB1 = other.observable1
            val oB2 = other.observable2
            if (oB1 == null || oB2 == null) return false

            if (oA1 === oB1 && oA2 === oB2) return true
            if (oA1 === oB2 && oA2 === oB1) return true
        }

        return false
    }

}
