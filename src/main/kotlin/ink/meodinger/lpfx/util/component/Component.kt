package ink.meodinger.lpfx.util.component

import javafx.event.ActionEvent
import javafx.geometry.VPos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment


/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Have fun with my code!
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

infix fun Button.does(onAction: Button.(ActionEvent) -> Unit): Button {
    this.setOnAction { onAction(this, it) }

    return this
}
