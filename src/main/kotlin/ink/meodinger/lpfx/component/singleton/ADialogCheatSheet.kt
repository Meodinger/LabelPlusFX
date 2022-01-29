package ink.meodinger.lpfx.component.singleton

import ink.meodinger.lpfx.COMMON_GAP
import ink.meodinger.lpfx.PANE_HEIGHT
import ink.meodinger.lpfx.PANE_WIDTH
import ink.meodinger.lpfx.util.component.add
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.ICON
import ink.meodinger.lpfx.util.resource.get

import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.GridPane
import javafx.scene.text.TextAlignment
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2022/1/29
 * Have fun with my code!
 */
object ADialogCheatSheet : Stage() {

    init {
        title = I18N["m.cheat"]
        width = PANE_WIDTH / 3 * 2
        height = PANE_HEIGHT

        icons.add(ICON)
        isResizable = false

        scene = Scene(GridPane().apply {
            padding = Insets(COMMON_GAP)
            vgap = COMMON_GAP
            hgap = COMMON_GAP
            alignment = Pos.TOP_CENTER

            add(Label(I18N["cheat.accelerator"]), 0, 0, 2, 1) {
                GridPane.setHalignment(this, HPos.CENTER)
                textAlignment = TextAlignment.CENTER
            }
            add(Label("Ctrl/Meta + ↑/↓"), 0, 1)
            add(Label(I18N["cheat.switch_label"]), 1, 1)
            add(Label("Ctrl/Meta + ←/→"), 0, 2)
            add(Label(I18N["cheat.switch_picture"]), 1, 2)
            add(Label("Ctrl/Meta + Enter"), 0, 3)
            add(Label(I18N["cheat.switch_next"]), 1, 3)
            add(Label("Ctrl/Meta + Shift + Enter"), 0, 4)
            add(Label(I18N["cheat.switch_last"]), 1, 4)

            add(Separator(), 0, 5, 2, 1)

            add(Label(I18N["cheat.mouse"]), 0, 6, 2, 1) {
                GridPane.setHalignment(this, HPos.CENTER)
                textAlignment = TextAlignment.CENTER
            }
            add(Label(I18N["cheat.dnd.dsc"]), 0, 7)
            add(Label(I18N["cheat.dnd.res"]), 1, 7)
            add(Label(I18N["cheat.drag_label.dsc"]), 0, 8)
            add(Label(I18N["cheat.drag_label.res"]), 1, 8)
            add(Label(I18N["cheat.double_label.dsc"]), 0, 9)
            add(Label(I18N["cheat.double_label.res"]), 1, 9)
        })
    }

}
