package ink.meodinger.lpfx.util.component

import javafx.event.ActionEvent
import javafx.geometry.VPos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
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
    textAlign: TextAlignment = TextAlignment.LEFT
): Label {
    return apply {
        isWrapText = isWrap
        textAlignment = textAlign
    }
}

operator fun Text.invoke(
    color: Color = Color.BLACK,
    textAlign: TextAlignment = TextAlignment.LEFT,
    textOri: VPos = VPos.BASELINE
): Text {
    return apply {
        fill = color
        textAlignment = textAlign
        textOrigin = textOri
    }
}

infix fun Button.does(onAction: Button.(ActionEvent) -> Unit): Button {
    return apply { setOnAction { onAction(this, it) } }
}

infix fun MenuItem.does(onAction: MenuItem.(ActionEvent) -> Unit): MenuItem {
    return apply { setOnAction { onAction(this, it) } }
}
