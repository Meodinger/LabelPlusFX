package ink.meodinger.lpfx

import ink.meodinger.lpfx.component.*
import ink.meodinger.lpfx.component.common.*
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.transform
import ink.meodinger.lpfx.util.string.emptyString

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.util.Callback


/**
 * Author: Meodinger
 * Date: 2021/11/26
 * Have fun with my code!
 */

/**
 * Main View
 */
class View(state: State) : BorderPane() {

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
                        prefWidth = 200.0
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
                            prefWidth = 160.0
                            innerBox.buttonCell = object : ListCell<TransGroup>() {
                                override fun updateItem(item: TransGroup?, empty: Boolean) {
                                    super.updateItem(item, empty)
                                    text = if (empty) null else item?.name
                                }
                            }
                            innerBox.cellFactory = Callback {
                                object : ListCell<TransGroup>() {
                                    override fun updateItem(item: TransGroup?, empty: Boolean) {
                                        super.updateItem(item, empty)

                                        if (empty || item == null) {
                                            text = null
                                            graphic = null
                                        } else {
                                            text = item.name
                                            graphic = Circle(GRAPHICS_CIRCLE_RADIUS, item.colorHex.let(Color::web))
                                        }
                                    }
                                }
                            }
                        }
                        add(HBox()) {
                            hgrow = Priority.ALWAYS
                        }
                        add(bSwitchViewMode) {
                            isMnemonicParsing = false
                        }
                    }
                    center(cTreeView) {
                        contextMenu = CTreeMenu(state, this)
                    }
                }
                add(TitledPane()) {
                    textProperty().bind(state.currentLabelIndexProperty().transform {
                        if (it == NOT_FOUND) emptyString() else it.toString()
                    })
                    withContent(cTransArea) {
                        isWrapText = true
                    }
                }
            }
        }

        val statsBar = HBox().apply {
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

        showStatsBarProperty.addListener(onNew {
            if (it) this@View.bottom(statsBar) else this@View.children.remove(statsBar)
        })
    }

}
