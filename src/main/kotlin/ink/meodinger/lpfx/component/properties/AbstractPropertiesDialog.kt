package ink.meodinger.lpfx.component.properties

import ink.meodinger.lpfx.options.CProperty

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
abstract class AbstractPropertiesDialog : Dialog<List<CProperty>>() {

    /// TODO: Use value, not CProperty

    init {
        setResultConverter {
            when (it) {
                ButtonType.OK -> convertResult()
                else -> emptyList()
            }
        }
    }

    protected abstract fun initProperties()

    protected abstract fun convertResult(): List<CProperty>

    fun generateProperties(): List<CProperty> {
        initProperties()

        return showAndWait().let { if (it.isPresent) it.get() else emptyList() }
    }
}