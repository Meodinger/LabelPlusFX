package ink.meodinger.lpfx

import ink.meodinger.lpfx.component.*
import ink.meodinger.lpfx.component.common.CComboBox
import ink.meodinger.lpfx.component.common.CLigatureArea
import ink.meodinger.lpfx.component.common.CTextSlider
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.string.emptyString

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.control.SplitPane
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.util.StringConverter


/**
 * Author: Meodinger
 * Date: 2021/11/26
 * Have fun with my code!
 */

/**
 * Main View
 */
class View(state: State) : BorderPane() {

    private val statsBar = HBox()
    private val cTreeViewMenu = CTreeMenu(state)

    val menuBar         = CMenuBar(state)
    val bSwitchViewMode = Button()
    val bSwitchWorkMode = Button()
    val lLocation       = Label()
    val lBackup         = Label()
    val lAccEditTime    = Label()
    val pMain           = SplitPane()
    val pRight          = SplitPane()
    val cGroupBar       = CGroupBar()
    val cLabelPane      = CLabelPane()
    val cSlider         = CTextSlider()
    val cPicBox         = CComboBox<String>()
    val cGroupBox       = CComboBox<TransGroup>()
    val cTreeView       = CTreeView()
    val cTransArea      = CLigatureArea()

    private val showStatsBarProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun showStatsBarProperty(): BooleanProperty = showStatsBarProperty
    var showStatsBar: Boolean by showStatsBarProperty

    init {
        state.view = this

        top(menuBar)
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
                        hgrow = Priority.ALWAYS
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
                        add(cGroupBox) {
                            innerBox.converter = object : StringConverter<TransGroup>() {
                                override fun toString(group: TransGroup?): String = group?.name ?: emptyString()
                                override fun fromString(string: String?): TransGroup? = cGroupBox.items.firstOrNull { it.name == string }
                            }
                        }
                        add(HBox()) {
                            alignment = Pos.CENTER
                            hgrow = Priority.ALWAYS
                        }
                        add(bSwitchViewMode) {
                            isMnemonicParsing = false
                        }
                    }
                    center(cTreeView) {
                        contextMenu = cTreeViewMenu.apply { update(emptyList()) }
                        addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) {
                            cTreeViewMenu.update(selectionModel.selectedItems.toList())
                        }
                    }
                }
                add(cTransArea) {
                    isWrapText = true
                }
            }
        }

        statsBar.apply {
            val generalPadding = Insets(4.0, 8.0, 4.0, 8.0)
            add(HBox()) {
                hgrow = Priority.ALWAYS
            }
            add(Separator()) {
                orientation = Orientation.VERTICAL
            }
            add(lBackup) {
                padding = generalPadding
                prefWidth = 150.0
            }
            add(Separator()) {
                orientation = Orientation.VERTICAL
            }
            add(lLocation) {
                padding = generalPadding
                prefWidth = 90.0
            }
            add(Separator()) {
                orientation = Orientation.VERTICAL
            }
            add(lAccEditTime) {
                padding = generalPadding
                prefWidth = 180.0
            }
        }

        showStatsBarProperty().addListener(onNew {
            if (it) this@View.bottom(statsBar) else this@View.children.remove(statsBar)
        })
    }

}
