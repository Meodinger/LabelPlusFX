package ink.meodinger.lpfx

import ink.meodinger.lpfx.component.CGroupBar
import ink.meodinger.lpfx.component.CLabelPane
import ink.meodinger.lpfx.component.CTreeView
import ink.meodinger.lpfx.component.common.CComboBox
import ink.meodinger.lpfx.component.common.CLigatureArea
import ink.meodinger.lpfx.component.common.CTextSlider
import ink.meodinger.lpfx.component.singleton.AMenuBar
import ink.meodinger.lpfx.util.component.*

import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority


/**
 * Author: Meodinger
 * Date: 2021/11/26
 * Have fun with my code!
 */

/**
 * Main View
 */
class View : BorderPane() {

    val bSwitchViewMode = Button(State.viewMode.toString())
    val bSwitchWorkMode = Button(State.workMode.toString())
    val lInfo           = Label()
    val pMain           = SplitPane()
    val pRight          = SplitPane()
    val cGroupBar       = CGroupBar()
    val cLabelPane      = CLabelPane()
    val cSlider         = CTextSlider()
    val cPicBox         = CComboBox<String>()
    val cGroupBox       = CComboBox<String>()
    val cTreeView       = CTreeView()
    val cTransArea      = CLigatureArea()

    init {
        top(AMenuBar)
        center(pMain) {
            add(BorderPane()) {
                top(cGroupBar)
                center(cLabelPane) {
                    initScale = SCALE_INIT
                    minScale = SCALE_MIN
                    maxScale = SCALE_MAX
                }
                bottom(HBox()) {
                    add(cSlider) {
                        initScale = SCALE_INIT
                        minScale = SCALE_MIN
                        maxScale = SCALE_MAX
                    }
                    add(HBox()) {
                        boxHGrow = Priority.ALWAYS
                    }
                    add(cPicBox) {
                        isWrapped = true
                    }
                }
            }
            add(pRight) {
                orientation = Orientation.VERTICAL
                add(BorderPane()) {
                    top(HBox()) {
                        add(bSwitchWorkMode) {
                            isMnemonicParsing = false
                        }
                        add(cGroupBox)
                        add(HBox()) {
                            alignment = Pos.CENTER
                            boxHGrow = Priority.ALWAYS
                        }
                        add(bSwitchViewMode) {
                            isMnemonicParsing = false
                        }
                    }
                    center(cTreeView)
                }
                add(BorderPane()) {
                    center(cTransArea) {
                        isWrapText = true
                    }
                    bottom(lInfo) {
                        padding = Insets(4.0, 8.0, 4.0, 8.0)
                    }
                }
            }
        }
    }

}
