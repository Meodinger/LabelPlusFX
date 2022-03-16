package ink.meodinger.lpfx.component.properties

import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog


/**
 * Author: Meodinger
 * Date: 2021/9/20
 * Have fun with my code!
 */

/**
 * A Dialog for properties change
 */
abstract class AbstractPropertiesDialog : Dialog<Map<String, Any>>() {

    /// TODO: Use value, not CProperty

    init {
        setResultConverter {
            when (it) {
                ButtonType.OK -> convertResult()
                else -> emptyMap()
            }
        }
    }

    protected abstract fun initProperties()

    protected abstract fun convertResult(): Map<String, Any>

    fun generateProperties(): Map<String, Any> {
        initProperties()

        return showAndWait().let { if (it.isPresent) it.get() else emptyMap() }
    }
}
