package ink.meodinger.lpfx.util.property

import javafx.beans.property.Property

/**
 * Author: Meodinger
 * Date: 2021/12/13
 * Have fun with my code!
 */

/**
 * Is a property was bound (bidirectional not included)
 */
inline val <T> Property<T>.isNotBound: Boolean get() = !isBound
