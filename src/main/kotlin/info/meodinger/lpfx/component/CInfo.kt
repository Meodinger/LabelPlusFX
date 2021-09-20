package info.meodinger.lpfx.component

import javafx.geometry.Insets
import javafx.scene.control.Label

/**
 * Author: Meodinger
 * Date: 2021/9/20
 * Location: info.meodinger.lpfx.component
 */

/**
 * A Label to show quick info
 */
class CInfo : Label() {

    init {
        this.padding = Insets(4.0, 8.0, 4.0, 8.0)
    }

    fun showInfo(info: String) {
        this.text = info
    }

}