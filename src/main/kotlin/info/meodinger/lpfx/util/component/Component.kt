package info.meodinger.lpfx.util.component

import javafx.geometry.VPos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment


/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Location: info.meodinger.lpfx.util.component
 */

/**
 * Label constructor extension
 */
operator fun Label.invoke(isWrap: Boolean, textAlignment: TextAlignment): Label {
    this.isWrapText = isWrap
    this.textAlignment = textAlignment

    return this
}

operator fun Text.invoke(color: Color, textAlignment: TextAlignment, textOrigin: VPos): Text {
    this.fill = color
    this.textAlignment = textAlignment
    this.textOrigin = textOrigin

    return this
}

operator fun Button.invoke(onAction: Button.() -> Unit): Button {
    this.setOnAction { onAction.invoke(this) }

    return this
}