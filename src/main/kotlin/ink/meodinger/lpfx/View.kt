package ink.meodinger.lpfx

import ink.meodinger.lpfx.component.*
import ink.meodinger.lpfx.component.common.*
import ink.meodinger.lpfx.options.Preference
import ink.meodinger.lpfx.options.RecentFiles
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.string.emptyString

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

    companion object {
        // Scale
        private const val SCALE_MIN : Double = 0.1
        private const val SCALE_MAX : Double = 4.0
        private const val SCALE_INIT: Double = 0.8
    }

    // region Components

    /**
     * Work mode switch button
     */
    val bSwitchWorkMode: Button = Button()

    /**
     * View mode switch button
     */
    val bSwitchViewMode: Button = Button()

    /**
     * StatsBar label: location
     */
    val lLocation: Label = Label()

    /**
     * StatsBar label: backup information
     */
    val lBackup: Label = Label()

    /**
     * StatsBar label: accumulate editing time
     */
    val lAccEditTime: Label = Label()

    /**
     * Picture ComboBox, change pictures
     */
    val cPicBox: CComboBox<String> = CComboBox()

    /**
     * Group ComboBox, change groups
     */
    val cGroupBox: CComboBox<TransGroup> = CComboBox()

    /**
     * GroupBar, display TransGroups above the LabelPane
     */
    val cGroupBar: CGroupBar = CGroupBar()

    /**
     * LabelPane, display Image & Labels
     */
    val cLabelPane: CLabelPane = CLabelPane()

    /**
     * TreeView, display labels by label-index or by groupId
     */
    val cTreeView: CTreeView = CTreeView()

    /**
     * TransArea, edit label's text
     */
    val cTransArea: CLigatureArea = CLigatureArea()

    // endregion

    init {
        state.view = this

        top(CMenuBar(state)) {
            recentFilesProperty().bind(RecentFiles.recentFilesProperty())
        }
        center(SplitPane()) {
            add(BorderPane()) {
                top(cGroupBar)
                center(cLabelPane) {
                    initScale = SCALE_INIT
                    minScale = SCALE_MIN
                    maxScale = SCALE_MAX
                }
                bottom(HBox()) {
                    add(CTextSlider()) {
                        disableProperty().bind(cLabelPane.disableProperty())
                        initScaleProperty().bind(cLabelPane.initScaleProperty())
                        scaleProperty().bindBidirectional(cLabelPane.scaleProperty())
                        minScaleProperty().bindBidirectional(cLabelPane.minScaleProperty())
                        maxScaleProperty().bindBidirectional(cLabelPane.maxScaleProperty())
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
            add(SplitPane()) {
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
                                            graphic = Circle(8.0, item.colorHex.let(Color::web))
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

                dividers[0].positionProperty().bindBidirectional(Preference.rightDividerPositionProperty())
            }

            dividers[0].positionProperty().bindBidirectional(Preference.mainDividerPositionProperty())
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
                text = I18N["stats.not_backed"]
            }
            add(Separator()) {
                orientation = Orientation.VERTICAL
            }
            add(lLocation) {
                padding = generalPadding
                prefWidth = 90.0
                text = "-- : --"
            }
            add(Separator()) {
                orientation = Orientation.VERTICAL
            }
            add(lAccEditTime) {
                padding = generalPadding
                prefWidth = 180.0
                text = String.format(I18N["stats.accumulator.s"], "--:--:--")
            }
        }

        Preference.showStatsBarProperty().addListener(onNew {
            if (it) bottom(statsBar) else children.remove(statsBar)
        })
    }

}
