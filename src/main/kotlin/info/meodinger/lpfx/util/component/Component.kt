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
operator fun Label.invoke(
    isWrap: Boolean = false,
    textAlignment: TextAlignment = TextAlignment.LEFT
): Label {
    this.isWrapText = isWrap
    this.textAlignment = textAlignment

    return this
}

operator fun Text.invoke(
    color: Color = Color.BLACK,
    textAlignment: TextAlignment = TextAlignment.LEFT,
    textOrigin: VPos = VPos.BASELINE
): Text {
    this.fill = color
    this.textAlignment = textAlignment
    this.textOrigin = textOrigin

    return this
}

operator fun Button.invoke(onAction: Button.() -> Unit): Button {
    this.setOnAction { onAction.invoke(this) }

    return this
}