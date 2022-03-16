package ink.meodinger.lpfx.util.property

import javafx.beans.property.Property
import javafx.collections.ObservableList

/**
 * Author: Meodinger
 * Date: 2021/12/13
 * Have fun with my code!
 */

/**
 * Is a property was bound (bidirectional not included)
 */
inline val <T> Property<T>.isNotBound: Boolean get() = !isBound

/**
 * Alias for setAll
 * @see ObservableList.setAll
 */
operator fun <E> ObservableList<E>.divAssign(value: List<E>) {
    setAll(value)
}
