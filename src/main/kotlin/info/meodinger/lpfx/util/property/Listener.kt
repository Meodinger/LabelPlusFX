package info.meodinger.lpfx.util.property

import javafx.beans.value.ChangeListener


/**
 * Author: Meodinger
 * Date: 2021/10/11
 * Location: info.meodinger.lpfx.util.property
 */

/**
 * For none-argument use
 */
fun <T> onChange(action: () -> Unit) = ChangeListener<T> { _, _, _ -> action() }