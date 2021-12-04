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
            setDividerPositions(0.618)
            add(BorderPane()) {
                top(cGroupBar)
                center(cLabelPane) {
                    initScale = 1.0
                    minScale = 0.2
                    maxScale = 2.0
                    scale = 1.0
                }
                bottom(HBox()) {
                    add(cSlider) {
                        initScale = 1.0
                        minScale = 0.2
                        maxScale = 2.0
                        scale = 1.0
                    }
                    add(HBox()) {
                        hGrow = Priority.ALWAYS
                    }
                    add(cPicBox) {
                        isWrapped = true
                    }
                }
            }
            add(pRight) {
                setDividerPositions(0.618)
                orientation = Orientation.VERTICAL
                add(BorderPane()) {
                    top(HBox()) {
                        add(bSwitchWorkMode) {
                            isMnemonicParsing = false
                        }
                        add(cGroupBox)
                        add(HBox()) {
                            alignment = Pos.CENTER
                            hGrow = Priority.ALWAYS
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