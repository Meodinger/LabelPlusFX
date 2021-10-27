package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.options.CProperty

import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog


/**
 * Author: Meodinger
 * Date: 2021/9/20
 * Location: ink.meodinger.lpfx.component.singleton
 */

/**
 * A Dialog for properties change
 */
abstract class AbstractPropertiesDialog : Dialog<List<CProperty>>() {

    init {
        this.setResultConverter {
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

        val result = this.showAndWait()
        return if (result.isPresent) result.get() else emptyList()
    }
}