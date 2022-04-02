package ink.meodinger.lpfx.util.property

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue


/**
 * Author: Meodinger
 * Date: 2021/10/11
 * Have fun with my code!
 */

/**
 * For none-argument use
 */
fun <T> onChange(action: () -> Unit) = ChangeListener<T> { _, _, _ -> action() }

/**
 * For new-argument use
 */
@Suppress("UNCHECKED_CAST")
fun <T, U : T> onNew(action: (U) -> Unit) = ChangeListener<T> { _, _, new -> action(new as U) }

/**
 * Listen once
 */
fun <T> ObservableValue<T>.once(listener: ChangeListener<T>) {
    addListener(object : ChangeListener<T> {
        override fun changed(observable: ObservableValue<out T>?, oldValue: T, newValue: T) {
            listener.changed(observable, oldValue, newValue)
            removeListener(this)
        }
    })
}
