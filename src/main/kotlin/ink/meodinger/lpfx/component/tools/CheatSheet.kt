package ink.meodinger.lpfx.component.tools

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.util.component.add
import ink.meodinger.lpfx.util.component.closeOnEscape
import ink.meodinger.lpfx.util.component.gridHAlign

import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import java.awt.Desktop
import java.net.URL

/**
 * Author: Meodinger
 * Date: 2022/1/29
 * Have fun with my code!
 */
class CheatSheet : Stage() {

    init {
        icons.add(ICON)
        title = I18N["m.cheat"]
        width = 400.0
        height = 400.0
        isResizable = false
        scene = Scene(GridPane().apply {
            padding = Insets(16.0)
            vgap = 16.0
            hgap = 16.0
            alignment = Pos.CENTER

            add(Label(I18N["cheat.accelerator"]), 0, 0, 2, 1) {
                gridHAlign = HPos.CENTER
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
                gridHAlign = HPos.CENTER
            }
            add(Label(I18N["cheat.dnd.dsc"]), 0, 7)
            add(Label(I18N["cheat.dnd.res"]), 1, 7)
            add(Label(I18N["cheat.drag_label.dsc"]), 0, 8)
            add(Label(I18N["cheat.drag_label.res"]), 1, 8)
            add(Label(I18N["cheat.double_label.dsc"]), 0, 9)
            add(Label(I18N["cheat.double_label.res"]), 1, 9)
            add(Hyperlink(I18N["cheat.more_help"]), 0, 10, 2, 1) {
                gridHAlign = HPos.CENTER
                setOnAction {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(URL(INFO["application.help"]).toURI())
                    }
                }
            }
        })

        closeOnEscape()
    }

}
