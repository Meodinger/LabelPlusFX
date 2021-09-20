package info.meodinger.lpfx.component

import info.meodinger.lpfx.type.TransGroup

import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment


/**
 * Author: Meodinger
 * Date: 2021/9/20
 * Location: info.meodinger.lpfx.component
 */

/**
 * A Region that displays a TransGroup
 */
class CGroup(transGroup: TransGroup) : Region() {

    companion object {
        private const val CORNER_RADII = 4.0
        private const val BORDER_WIDTH = 1.0
        private const val PADDING = 2.0
    }

    val groupName: String = transGroup.name

    private val backgroundSelected: Background = Background(BackgroundFill(
        Color.LIGHTGRAY,
        CornerRadii(CORNER_RADII),
        Insets(0.0)
    ))
    private val borderDefault: Border = Border(BorderStroke(
        Color.TRANSPARENT,
        BorderStrokeStyle.NONE,
        CornerRadii(CORNER_RADII),
        BorderWidths(BORDER_WIDTH)
    ))
    private val borderHovered: Border = Border(BorderStroke(
        Color.web(transGroup.color),
        BorderStrokeStyle.SOLID,
        CornerRadii(CORNER_RADII),
        BorderWidths(BORDER_WIDTH)
    ))
    private val text: Text = Text(transGroup.name).also {
        it.fill = Color.web(transGroup.color)
        it.textAlignment = TextAlignment.CENTER
        it.textOrigin = VPos.CENTER
    }

    init {
        this.padding = Insets(PADDING)
        this.border = borderDefault
        this.hoverProperty().addListener { _, _, isHover ->
            if (isHover) this.border = borderHovered
            else this.border = borderDefault
        }

        this.children.add(text)

        val textW = text.boundsInLocal.width
        val textH = text.boundsInLocal.height
        val borderInsets = this.border.insets
        val borderOutsets = this.border.outsets
        val borderStrokeInsets = this.border.strokes[0].insets
        val borderWidths = this.border.strokes[0].widths
        val insets = this.insets
        val padding = this.padding
        val prefW = textW +
                borderOutsets.left + borderInsets.left + borderStrokeInsets.left + borderWidths.left + padding.left + insets.left +
                borderOutsets.right + borderInsets.right + borderStrokeInsets.right + borderWidths.right + padding.right + insets.right
        val prefH = textH +
                borderOutsets.top + borderInsets.top + borderStrokeInsets.top + borderWidths.top + padding.top + insets.top +
                borderOutsets.bottom + borderInsets.bottom + borderStrokeInsets.bottom + borderWidths.bottom + padding.bottom + insets.bottom
        this.setPrefSize(prefW, prefH)

        text.layoutX = (-textW + prefWidth) / 2
        text.layoutY = prefHeight / 2
    }

    fun select() {
        this.background = backgroundSelected
    }

    fun unselect() {
        this.background = null
    }
}