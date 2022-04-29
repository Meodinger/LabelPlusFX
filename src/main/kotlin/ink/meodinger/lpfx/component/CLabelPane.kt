package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.Config.MonoFont
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.collection.autoRangeTo
import ink.meodinger.lpfx.util.collection.div
import ink.meodinger.lpfx.util.color.toHexRGB
import ink.meodinger.lpfx.util.component.add
import ink.meodinger.lpfx.util.component.withContent
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.string.omitHighText
import ink.meodinger.lpfx.util.string.omitWideText

import javafx.beans.property.*
import javafx.collections.*
import javafx.event.*
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import kotlin.math.abs


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A scalable, draggable ScrollPane that can display image, text and labels
 */
class CLabelPane : ScrollPane() {

    companion object {
        /**
         * Rect based shift
         */
        private const val SHIFT_X = 20.0
        private const val TEXT_INSET = 10.0
        private const val TEXT_ALPHA = "A0"
        private val TEXT_FONT = Font(MonoFont, 32.0)
    }

    // region LabelEvent

    /**
     * When an action related to label occurs, a LabelEvent will be dispatched.
     * As LabelEvent is the result of MouseEven, it likes some kind of wrapper
     * of a MouseEvent, but provides more information about the label related.
     *
     * @param eventType Type of the LabelEvent
     * @param source MouseEvent that produces the LabelEvent
     * @param labelIndex Index of the related label
     * @param displayX X coordinate based on the displaying image
     * @param displayY Y coordinate based on the displaying image
     * @param labelX X coordinate of the related TransLabel
     * @param labelY Y coordinate of the related TransLabel
     */
    class LabelEvent(
        eventType: EventType<LabelEvent>,
        val source: MouseEvent,
        val labelIndex: Int,
        val displayX: Double,
        val displayY: Double,
        val labelX: Double,
        val labelY: Double,
    ) : Event(eventType) {
        companion object {

            /**
             * Root type of all LabelEventType
             */
            val LABEL_ANY   : EventType<LabelEvent> = EventType(EventType.ROOT, "LABEL_ANY")

            /**
             * Indicate should create a label. `labelIndex` will be NOT_FOUND
             * because the event target isn't a label. `labelX` and `labelY`
             * will be the coordinate of the label which will be created.
             */
            val LABEL_CREATE: EventType<LabelEvent> = EventType(LABEL_ANY, "LABEL_CREATE")

            /**
             * Incicate shoule remove a label. `labelIndex` will be the target
             * label's index. `labelX` and `labelY` will be `Double.NaN` because
             * they are useless here and should not make distraction.
             */
            val LABEL_REMOVE: EventType<LabelEvent> = EventType(LABEL_ANY, "LABEL_REMOVE")

            /**
             * Incicate we are hovering on a label. `labelIndex` will be the target
             * label's index. `labelX` and `labelY` will be `Double.NaN` because
             * they are useless here and should not make distraction.
             */
            val LABEL_HOVER : EventType<LabelEvent> = EventType(LABEL_ANY, "LABEL_HOVER")

            /**
             * Incicate we have clicked a label. `labelIndex` will be the target
             * label's index. `labelX` and `labelY` will be `Double.NaN` because
             * they are useless here and should not make distraction.
             */
            val LABEL_CLICK : EventType<LabelEvent> = EventType(LABEL_ANY, "LABEL_CLICK")

            /**
             * Incicate we moved a label. `labelIndex` will be the target
             * label's index. `labelX` and `labelY` will be the new coordinates
             * of the target label. Note that this event will only be dispacted
             * when we have finished moving a label.
             */
            val LABEL_MOVE  : EventType<LabelEvent> = EventType(LABEL_ANY, "LABEL_MOVE")

            /**
             * Incicate shoule remove a label. `labelIndex` will be NOT_FOUND
             * because the event target isn't a label. `labelX` and `labelY`
             * will be `Double.NaN` because the event target isn't a label.
             */
            val LABEL_OTHER : EventType<LabelEvent> = EventType(LABEL_ANY, "LABEL_OTHER")
        }
    }

    // endregion

    // region NewPicScale Enum

    /**
     * The scale strategy when display a new image
     */
    enum class NewPictureScale(private val description: String) {

        /**
         * The scale will be set to `initScale`
         */
        DEFAULT(I18N["label_pane.nps.default"]),

        /**
         * The scale will be set to `1.0`
         */
        FULL(I18N["label_pane.nps.full"]),

        /**
         * The scale will be set to a value that makes the
         * width of the image equals to the width of the pane.
         */
        FIT(I18N["label_pane.nps.fit"]),

        /**
         * The scale will not change
         */
        PREVIOUS(I18N["label_pane.nps.previous"]);

        override fun toString(): String = description
    }

    // endregion

    // region Layer System

    /*
     *           |   Layout   | Width
     * -----------------------------
     * text      | with image | image width
     * label     | with image | image width
     * image     | left-top   | image width
     * root      | with image | image width
     * pane      | -          | actual width
     */

    /**
     * For text display
     */
    private val textLayer = Canvas()

    /**
     * For labels display
     */
    private val labelLayer = AnchorPane().apply { isPickOnBounds = false }

    /**
     * For image display
     */
    private val imageView = ImageView(INIT_IMAGE)

    /**
     * For contain, display, scale, drag, label, event handle, internal disable
     */
    private val root = StackPane()

    // endregion

    // region Runtime Data

    private var shiftX = 0.0
    private var shiftY = 0.0
    private var dragging = false
    private var selecting = false

    @Suppress("UNCHECKED_CAST")
    private val labelNodes: ObservableList<CLabel> get() = labelLayer.children as ObservableList<CLabel>
    private val shouldCreate: Boolean get() = image !== INIT_IMAGE

    // endregion

    // region Properties:Scale

    private val initScaleProperty: DoubleProperty = SimpleDoubleProperty(Double.NaN)
    private val minScaleProperty:  DoubleProperty = SimpleDoubleProperty(Double.NaN)
    private val maxScaleProperty:  DoubleProperty = SimpleDoubleProperty(Double.NaN)
    private val scaleProperty:     DoubleProperty = SimpleDoubleProperty(1.0)
    fun initScaleProperty():       DoubleProperty = initScaleProperty
    fun minScaleProperty():        DoubleProperty = minScaleProperty
    fun maxScaleProperty():        DoubleProperty = maxScaleProperty
    fun scaleProperty():           DoubleProperty = scaleProperty
    var initScale: Double
        get() = initScaleProperty.get()
        set(value) {
            if (value < 0) return
            var temp = value
            if (!minScale.isNaN()) temp = temp.coerceAtLeast(minScale)
            if (!maxScale.isNaN()) temp = temp.coerceAtMost(maxScale)
            initScaleProperty.set(temp)
        }
    var minScale: Double
        get() = minScaleProperty.get()
        set(value) {
            if (value < 0 || (!maxScale.isNaN() && value > maxScale)) return
            minScaleProperty.set(value)
        }
    var maxScale: Double
        get() = maxScaleProperty.get()
        set(value) {
            if (value < 0 || (!minScale.isNaN() && value < minScale)) return
            maxScaleProperty.set(value)
        }
    var scale: Double
        get() = scaleProperty.get()
        set(value) {
            if (value < 0) return
            var temp = value
            if (!minScale.isNaN()) temp = temp.coerceAtLeast(minScale)
            if (!maxScale.isNaN()) temp = temp.coerceAtMost(maxScale)
            scaleProperty.set(temp)
        }

    // endregion

    // region Properties:Handler

    private val onLabelCreateProperty: ObjectProperty<EventHandler<LabelEvent>> = SimpleObjectProperty(EventHandler<LabelEvent> {})
    private val onLabelRemoveProperty: ObjectProperty<EventHandler<LabelEvent>> = SimpleObjectProperty(EventHandler<LabelEvent> {})
    private val onLabelHoverProperty:  ObjectProperty<EventHandler<LabelEvent>> = SimpleObjectProperty(EventHandler<LabelEvent> {})
    private val onLabelClickProperty:  ObjectProperty<EventHandler<LabelEvent>> = SimpleObjectProperty(EventHandler<LabelEvent> {})
    private val onLabelMoveProperty:   ObjectProperty<EventHandler<LabelEvent>> = SimpleObjectProperty(EventHandler<LabelEvent> {})
    private val onLabelOtherProperty:  ObjectProperty<EventHandler<LabelEvent>> = SimpleObjectProperty(EventHandler<LabelEvent> {})
    fun onLabelCreateProperty():       ObjectProperty<EventHandler<LabelEvent>> = onLabelCreateProperty
    fun onLabelRemoveProperty():       ObjectProperty<EventHandler<LabelEvent>> = onLabelRemoveProperty
    fun onLabelHoverProperty():        ObjectProperty<EventHandler<LabelEvent>> = onLabelHoverProperty
    fun onLabelClickProperty():        ObjectProperty<EventHandler<LabelEvent>> = onLabelClickProperty
    fun onLabelMoveProperty():         ObjectProperty<EventHandler<LabelEvent>> = onLabelMoveProperty
    fun onLabelOtherProperty():        ObjectProperty<EventHandler<LabelEvent>> = onLabelOtherProperty
    val onLabelCreate:                                EventHandler<LabelEvent> by onLabelCreateProperty
    val onLabelRemove:                                EventHandler<LabelEvent> by onLabelRemoveProperty
    val onLabelHover:                                 EventHandler<LabelEvent> by onLabelHoverProperty
    val onLabelClick:                                 EventHandler<LabelEvent> by onLabelClickProperty
    val onLabelMove:                                  EventHandler<LabelEvent> by onLabelMoveProperty
    val onLabelOther:                                 EventHandler<LabelEvent> by onLabelOtherProperty
    fun setOnLabelCreate(handler: EventHandler<LabelEvent>) {
        removeEventHandler(LabelEvent.LABEL_CREATE, onLabelCreate)
        onLabelCreateProperty.set(handler)
        addEventHandler(LabelEvent.LABEL_CREATE, onLabelCreate)
    }
    fun setOnLabelRemove(handler: EventHandler<LabelEvent>) {
        removeEventHandler(LabelEvent.LABEL_REMOVE, onLabelRemove)
        onLabelRemoveProperty.set(handler)
        addEventHandler(LabelEvent.LABEL_REMOVE, onLabelRemove)
    }
    fun setOnLabelHover(handler: EventHandler<LabelEvent>) {
        removeEventHandler(LabelEvent.LABEL_HOVER, onLabelHover)
        onLabelHoverProperty.set(handler)
        addEventHandler(LabelEvent.LABEL_HOVER, onLabelHover)
    }
    fun setOnLabelClick(handler: EventHandler<LabelEvent>) {
        removeEventHandler(LabelEvent.LABEL_CLICK, onLabelClick)
        onLabelClickProperty.set(handler)
        addEventHandler(LabelEvent.LABEL_CLICK, onLabelClick)
    }
    fun setOnLabelMove(handler: EventHandler<LabelEvent>) {
        removeEventHandler(LabelEvent.LABEL_MOVE, onLabelMove)
        onLabelMoveProperty.set(handler)
        addEventHandler(LabelEvent.LABEL_MOVE, onLabelMove)
    }
    fun setOnLabelOther(handler: EventHandler<LabelEvent>) {
        removeEventHandler(LabelEvent.LABEL_OTHER, onLabelOther)
        onLabelOtherProperty.set(handler)
        addEventHandler(LabelEvent.LABEL_OTHER, onLabelOther)
    }

    // endregion

    // region Properties:Layout

    private val groupsProperty: ListProperty<TransGroup> = SimpleListProperty(FXCollections.observableArrayList())
    fun groupsProperty(): ListProperty<TransGroup> = groupsProperty
    var groups: ObservableList<TransGroup> by groupsProperty

    private val imageProperty: ObjectProperty<Image> = imageView.imageProperty()
    fun imageProperty(): ObjectProperty<Image> = imageProperty
    var image: Image by imageProperty

    private val labelsProperty: ListProperty<TransLabel> = SimpleListProperty(FXCollections.emptyObservableList())
    fun labelsProperty(): ListProperty<TransLabel> = labelsProperty
    var labels: ObservableList<TransLabel> by labelsProperty

    private val labelRadiusProperty: DoubleProperty = SimpleDoubleProperty(24.0)
    fun labelRadiusProperty(): DoubleProperty = labelRadiusProperty
    var labelRadius: Double by labelRadiusProperty

    private val labelColorOpacityProperty: DoubleProperty = SimpleDoubleProperty(0.5)
    fun labelColorOpacityProperty(): DoubleProperty = labelColorOpacityProperty
    var labelColorOpacity: Double by labelColorOpacityProperty

    private val labelTextOpaqueProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun labelTextOpaqueProperty(): BooleanProperty = labelTextOpaqueProperty
    var isLabelTextOpaque: Boolean by labelTextOpaqueProperty

    private val newPictureScaleProperty: ObjectProperty<NewPictureScale> = SimpleObjectProperty(NewPictureScale.DEFAULT)
    fun newPictureScaleProperty(): ObjectProperty<NewPictureScale> = newPictureScaleProperty
    var newPictureScale: NewPictureScale by newPictureScaleProperty

    private val commonCursorProperty: ObjectProperty<Cursor> = SimpleObjectProperty(Cursor.DEFAULT)
    fun commonCursorProperty(): ObjectProperty<Cursor> = commonCursorProperty
    var commonCursor: Cursor by commonCursorProperty

    // endregion

    // region Properties:Selection

    private val selectedLabelsProperty: SetProperty<Int> = SimpleSetProperty(FXCollections.observableSet(HashSet()))
    fun selectedLabelsProperty(): ReadOnlySetProperty<Int> = selectedLabelsProperty
    val selectedLabels: Set<Int> by selectedLabelsProperty
    private var selectedIndices: ObservableSet<Int> by selectedLabelsProperty

    // endregion

    init {
        // Note that the scroll bar is some kind of useless
        // They cannot locate the picture properly because
        // I used so many translateX/Y to layout the root
        withContent(root) {
            alignment = Pos.CENTER

            // Layer system
            val imageWidthBinding = imageProperty.transform(Image::getWidth)
            val imageHeightBinding = imageProperty.transform(Image::getHeight)
            add(imageView) {
                isPreserveRatio = true
                isPickOnBounds = true
            }
            add(labelLayer) {
                prefWidthProperty().bind(imageWidthBinding)
                prefHeightProperty().bind(imageHeightBinding)
            }
            add(textLayer) {
                isMouseTransparent = true
                graphicsContext2D.font = TEXT_FONT
                graphicsContext2D.textBaseline = VPos.TOP

                widthProperty().bind(imageWidthBinding)
                heightProperty().bind(imageHeightBinding)
            }
        }

        // Disable mnemonic parsing event from LabelPane
        addEventFilter(KeyEvent.ANY) {
            if (it.code == KeyCode.ALT) it.consume()
        }

        // Remove text when scroll event fired
        // NOTE: Horizon scroll use default impl: Shift + Scroll
        addEventFilter(ScrollEvent.SCROLL) {
            clearText()
        }

        // Scale
        scaleProperty.addListener(onNew<Number, Double> {
            root.scaleX = it
            root.scaleY = it
        })
        root.addEventFilter(ScrollEvent.SCROLL) {
            if (it.isControlDown || it.isAltDown || it.isMetaDown) {
                val deltaScale = if (it.deltaY > 0) 0.1 else -0.1

                scale += deltaScale

                // x, y -> location not related to scale, based on left-top of the image
                // nLx = Lx + (imgW / 2 - x) * dS
                root.translateX += deltaScale * (image.width  / 2 - it.x)
                root.translateY += deltaScale * (image.height / 2 - it.y)

                it.consume()
            }
        }

        // Draggable
        // Note: Will somebody help me impl this using pannable & viewport?
        // ScenePos -> CursorPos; LayoutPos -> ContextPos
        // nLx = Lx + (nSx - Sx)
        // nLx = (Lx - Sx) + nSx -> shiftN + sceneN
        root.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            if (!it.isPrimaryButtonDown) return@addEventHandler

            shiftX = root.translateX - it.sceneX
            shiftY = root.translateY - it.sceneY
            root.cursor = Cursor.MOVE
        }
        root.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
            if (!it.isPrimaryButtonDown) return@addEventHandler

            dragging = true

            root.translateX = shiftX + it.sceneX
            root.translateY = shiftY + it.sceneY
        }
        root.addEventHandler(MouseEvent.MOUSE_RELEASED) {
            dragging = false
        }

        // Box selection
        // ScenePos -> CursorPos; LayoutPos -> ContextPos
        // W = abs(nLx - Lx), H = abs(nLy - Ly)
        root.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            if (!it.isSecondaryButtonDown) return@addEventHandler

            shiftX = it.x
            shiftY = it.y
            root.cursor = Cursor.CROSSHAIR
        }
        root.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
            if (!it.isSecondaryButtonDown) return@addEventHandler

            selecting = true

            val x = shiftX.coerceAtMost(it.x)
            val y = shiftY.coerceAtMost(it.y)
            val w = abs(it.x - shiftX)
            val h = abs(it.y - shiftY)

            clearText()
            val gc = textLayer.graphicsContext2D
            gc.lineWidth = 2.0 / scale
            gc.stroke = Color.BLACK
            gc.strokeRect(x, y, w, h)
        }
        root.addEventHandler(MouseEvent.MOUSE_RELEASED) {
            if (selecting) {
                clearText()

                val rangeX = shiftX.autoRangeTo(it.x) / image.width
                val rangeY = shiftY.autoRangeTo(it.y) / image.height
                val indices = HashSet<Int>()
                for (label in labels) {
                    if (rangeX.contains(label.x) && rangeY.contains(label.y)) {
                        indices.add(label.index)
                    }
                }

                if (it.isShiftDown && it.isAltDown) {
                    // Shift + Alt -> Preserve mode
                    // Select labels in both box-selection and selected
                    selectedIndices = FXCollections.observableSet(selectedIndices.filter(indices::contains).toSet())
                } else if (it.isShiftDown) {
                    // Shift -> Add mode
                    selectedIndices.addAll(indices)
                } else if (it.isAltDown) {
                    // Alt -> Subtract mode
                    selectedIndices.removeAll(indices)
                } else {
                    // Else -> Select mode
                    selectedIndices = FXCollections.observableSet(indices)
                }
            }

            selecting = false
        }

        // Restore cursor
        root.addEventHandler(MouseEvent.MOUSE_RELEASED) {
            root.cursor = commonCursor
        }

        // Cursor
        root.addEventHandler(MouseEvent.MOUSE_ENTERED) {
            root.cursor = commonCursor
        }
        root.addEventHandler(MouseEvent.MOUSE_MOVED) {
            root.cursor = commonCursor
        }
        root.addEventHandler(MouseEvent.MOUSE_EXITED) {
            root.cursor = commonCursor
            clearText()
        }

        // Handle
        root.addEventHandler(MouseEvent.MOUSE_MOVED) {
            fireEvent(LabelEvent(LabelEvent.LABEL_OTHER, it, NOT_FOUND, it.x, it.y, Double.NaN, Double.NaN))
        }
        root.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY) {
                if (!it.isStillSincePress) return@addEventHandler

                // Make sure all labels will not be placed partial outside
                val pickRadius = labelRadius.coerceAtLeast(CLabel.MIN_PICK_RADIUS)
                if (it.x <= pickRadius || it.x + pickRadius >= image.width) return@addEventHandler
                if (it.y <= pickRadius || it.y + pickRadius >= image.height) return@addEventHandler

                fireEvent(LabelEvent(LabelEvent.LABEL_CREATE,
                    it, NOT_FOUND, it.x, it.y,
                    it.x / image.width,
                    it.y / image.height,
                ))
            }
        }

        imageProperty.addListener(onNew {
            if (it === INIT_IMAGE) {
                root.isDisable = true
                scale = initScale
                moveToCenter()
            } else {
                root.isDisable = false
                when (newPictureScale) {
                    NewPictureScale.DEFAULT  -> scale = initScale
                    NewPictureScale.FULL     -> scale = 1.0 // 100%
                    NewPictureScale.FIT      -> fitToPane() // Fit
                    NewPictureScale.PREVIOUS -> doNothing() // Last
                }
                moveToZero()
            }
        })
        labelsProperty.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasPermutated()) {
                    // will not happen
                    throw IllegalStateException("Permuted: $it")
                } else if (it.wasUpdated()) {
                    // will not happen
                    throw IllegalStateException("Updated: $it")
                } else {
                    if (it.wasRemoved()) {
                        it.removed.forEach(this::removeLabel)
                        it.removed.map(TransLabel::index).forEach(selectedLabelsProperty::remove)
                    }
                    if (it.wasAdded() && shouldCreate) {
                        it.addedSubList.forEach(this::createLabel)
                    }
                }
            }
        })
    }

    private fun createLabel(transLabel: TransLabel) {
        val label = CLabel().apply {
            indexProperty().bind(transLabel.indexProperty())
            radiusProperty().bind(labelRadiusProperty)
            colorProperty().bind(groupsProperty.valueAt(transLabel.groupIdProperty()).transform { Color.web(it.colorHex) })
            colorOpacityProperty().bind(labelColorOpacityProperty)
            textOpaqueProperty().bind(labelTextOpaqueProperty)
        }

        // Draggable
        // ScenePos -> CursorPos; LayoutPos -> CtxPos
        // nLx = Lx + (nSx - Sx); nLy = Ly + (nSy - Sy)
        // nLx = (Lx - Sx) + nSx -> shiftN + sceneN
        label.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            shiftX = label.layoutX - it.sceneX / scale
            shiftY = label.layoutY - it.sceneY / scale

            label.cursor = Cursor.MOVE

            it.consume() // make sure root will not move together
        }
        label.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
            dragging = true

            clearText()

            val newLayoutX = shiftX + it.sceneX / scale
            val newLayoutY = shiftY + it.sceneY / scale

            //  0-----LR----    0 LR LR |
            //  |     LR        LR      |
            //  |LR LR|-----    LR      |
            //  |     |         --------|
            if (newLayoutX < 0 || newLayoutX > image.width - 2 * label.radius) return@addEventHandler
            if (newLayoutY < 0 || newLayoutY > image.height - 2 * label.radius) return@addEventHandler

            label.layoutX = newLayoutX
            label.layoutY = newLayoutY

            it.consume() // make sure root will not move together
        }
        label.addEventHandler(MouseEvent.MOUSE_RELEASED) {
            label.cursor = Cursor.HAND

            if (dragging) fireEvent(LabelEvent(LabelEvent.LABEL_MOVE,
                it, transLabel.index,
                label.layoutX + it.x,
                label.layoutY + it.y,
                label.layoutX / image.width,
                label.layoutY / image.height,
            ))

            dragging = false

            it.consume() // prevent further propagation
        }

        // Cursor
        label.addEventHandler(MouseEvent.MOUSE_ENTERED) {
            label.cursor = Cursor.HAND
        }
        label.addEventHandler(MouseEvent.MOUSE_MOVED) {
            label.cursor = Cursor.HAND
        }
        label.addEventHandler(MouseEvent.MOUSE_EXITED) {
            label.cursor = commonCursor
            clearText()
        }

        // Event handle
        label.setOnMouseMoved {
            fireEvent(LabelEvent(LabelEvent.LABEL_HOVER,
                it, transLabel.index,
                label.layoutX + it.x,
                label.layoutY + it.y,
                Double.NaN, Double.NaN
            ))
        }
        label.setOnMouseClicked {
            if (!it.isStillSincePress) return@setOnMouseClicked
            if (it.button == MouseButton.PRIMARY) {
                fireEvent(LabelEvent(LabelEvent.LABEL_CLICK,
                    it, transLabel.index,
                    label.layoutX + it.x,
                    label.layoutY + it.y,
                    Double.NaN, Double.NaN
                ))
            } else if (it.button == MouseButton.SECONDARY) {

                fireEvent(LabelEvent(LabelEvent.LABEL_REMOVE,
                    it, transLabel.index,
                    label.layoutX + it.x,
                    label.layoutY + it.y,
                    Double.NaN, Double.NaN
                ))
            }
        }

        //Anchor-L-----  Anchor = imageWidth * x - LR
        //  |    R
        //  | LR X-----  x = (Anchor + LR) / imageWidth
        //  |    |

        // Layout
        label.layoutX = -label.radius + transLabel.x * image.width
        label.layoutY = -label.radius + transLabel.y * image.height

        // Layout
        labelNodes.add(label)
    }
    private fun removeLabel(transLabel: TransLabel) {
        val label = labelNodes.first { it.index == transLabel.index } ?: return

        // Unbind
        label.radiusProperty().unbind()
        label.indexProperty().unbind()
        label.colorProperty().unbind()
        label.colorOpacityProperty().unbind()
        label.textOpaqueProperty().unbind()

        // Remove view
        labelNodes.remove(label)
    }

    // Text rendering

    /**
     * Clear all the texts that are displaying
     */
    fun clearText() {
        textLayer.graphicsContext2D.clearRect(0.0, 0.0, textLayer.width, textLayer.height)
    }
    /**
     * Create some text at the given coordinate
     * @param text Text to display
     * @param color Color of the text
     * @param x X coordinate where the text will be displayed, based on the image width
     * @param y Y coordinate where the text will be displayed, based on the image width
     */
    fun createText(text: String, color: Color, x: Double, y: Double) {
        val gc = textLayer.graphicsContext2D
        val s = omitWideText(omitHighText(text), (image.width - 2 * (SHIFT_X + TEXT_INSET)) / 2, TEXT_FONT)
        val t = Text(s).apply { font = TEXT_FONT }

        val textW = t.boundsInLocal.width
        val textH = t.boundsInLocal.height
        val shapeW = textW + 2 * TEXT_INSET
        val shapeH = textH + 2 * TEXT_INSET

        //   0 -> x  ------
        //   ↓       |    |
        //   y       ------
        var textX = x + SHIFT_X + TEXT_INSET
        var textY = y + TEXT_INSET
        var shapeX = x + SHIFT_X
        var shapeY = y

        if (shapeX + shapeW > image.width) {
            textX = x - textW - SHIFT_X - TEXT_INSET
            shapeX = x - shapeW - SHIFT_X
        }
        if (shapeY + shapeH > image.height) {
            textY = y - textH - TEXT_INSET
            shapeY = y - shapeH
        }

        gc.fill = Color.web(Color.WHEAT.toHexRGB() + TEXT_ALPHA)
        gc.fillRect(shapeX, shapeY, shapeW, shapeH)
        gc.lineWidth = 2.0
        gc.stroke = Color.DARKGRAY
        gc.strokeRect(shapeX, shapeY, shapeW, shapeH)
        gc.fill = color
        gc.fillText(t.text, textX, textY)
    }

    // Layout position

    /**
     * Move the image where will make the target label
     * be displayed at the center of the LabelPane
     * @param labelIndex Index of the label which will be displaye at the center
     */
    fun moveToLabel(labelIndex: Int) {
        if (!shouldCreate) return

        val label = labelNodes.first { it.index == labelIndex }

        vvalue = 0.0
        hvalue = 0.0

        // Scaled (fake)
        // -> Image / 2 - (Image / 2 - Center) * Scale
        // -> Image / 2 * (1 - Scale) + Center * Scale
        val fakeX = image.width / 2 * (1 - scale) + label.layoutX * scale
        val fakeY = image.height / 2 * (1 - scale) + label.layoutY * scale

        // To center
        // -> Scroll / 2 = Layout + Fake
        // -> Layout = Scroll / 2 - Fake
        root.translateX = width / 2 - fakeX
        root.translateY = height / 2 - fakeY
    }
    /**
     * Move the image to the top-left of the LabelPane
     */
    fun moveToZero() {
        vvalue = 0.0
        hvalue = 0.0

        root.translateX = - (1 - scale) * image.width / 2
        root.translateY = - (1 - scale) * image.height / 2
    }
    /**
     * Move the image to the center of the LabelPane
     */
    fun moveToCenter() {
        vvalue = 0.0
        hvalue = 0.0

        root.translateX = (width - image.width) / 2
        root.translateY = (height - image.height) / 2
    }

    // Scale

    /**
     * The scale will be set to a value that makes the
     * width of the image equals to the width of the pane.
     */
    fun fitToPane() {
        scale = width / image.width
    }

    // Dangerous Zone

    /**
     * This function will let imageProperty re-get its value.
     * It's useful when the binding was manually invalidated.
     * @return current image
     */
    fun requestShowImage(): Image = imageProperty.get()
    /**
     * WARNING: this function will force LabelPane to create labels regardless of
     *          whether the label actually should and could be layout.
     */
    fun requestCreateLabels() = labels.forEach(this::createLabel)
    /**
     * WARNING: this function will force LabelPane to remove labels regardless of
     *          whether the label actually was layout or should be removed.
     */
    fun requestRemoveLabels() = labels.forEach(this::removeLabel)

}
