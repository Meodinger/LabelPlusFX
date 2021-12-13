package ink.meodinger.lpfx.util.property

import javafx.beans.property.Property

/**
 * Author: Meodinger
 * Date: 2021/12/13
 * Have fun with my code!
 */

val <T> Property<T>.isNotBound: Boolean get() = !isBound
