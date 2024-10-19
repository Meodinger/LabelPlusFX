package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.Config.MonoFont
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.collection.autoRangeTo
import ink.meodinger.lpfx.util.color.toHexRGB
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.string.shortenLongText
import ink.meodinger.lpfx.util.string.shortenWideText

import javafx.beans.property.*
import javafx.collections.*
import javafx.event.*
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.control.ScrollPane
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.util.Duration
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
        // Text display constants
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
     * @param sourceEvent MouseEvent that produces the LabelEvent
     * @param labelIndex Index of the related label
     * @param displayX X coordinate based on the displaying image
     * @param displayY Y coordinate based on the displaying image
     * @param labelX X coordinate of the related TransLabel
     * @param labelY Y coordinate of the related TransLabel
     * @see LABEL_CREATE
     * @see LABEL_REMOVE
     * @see LABEL_HOVER
     * @see LABEL_CLICK
     * @see LABEL_MOVE
     * @see LABEL_OTHER
     */
    class LabelEvent(
        eventType: EventType<LabelEvent>,
        val sourceEvent: MouseEvent,
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
            val LABEL_ANY : EventType<LabelEvent> = EventType(EventType.ROOT, "LABEL_ANY")

            /**
             * Indicate should create a label. `labelIndex` will be NOT_FOUND
             * because the event target isn't a label. `labelX` and `labelY`
             * will be the coordinate of the label which will be created.
             * @see LabelEvent
             */
            val LABEL_CREATE : EventType<LabelEvent> = EventType(LABEL_ANY, "LABEL_CREATE")

            /**
             * Incicate shoule remove a label. `labelIndex` will be the target
             * label's index. `labelX` and `labelY` will be `Double.NaN` because
             * they are useless here and should not make distraction.
             * @see LabelEvent
             */
            val LABEL_REMOVE : EventType<LabelEvent> = EventType(LABEL_ANY, "LABEL_REMOVE")

            /**
             * Incicate we are hovering on a label. `labelIndex` will be the target
             * label's index. `labelX` and `labelY` will be `Double.NaN` because
             * they are useless here and should not make distraction.
             * @see LabelEvent
             */
            val LABEL_HOVER : EventType<LabelEvent> = EventType(LABEL_ANY, "LABEL_HOVER")

            /**
             * Incicate we have clicked a label. `labelIndex` will be the target
             * label's index. `labelX` and `labelY` will be `Double.NaN` because
             * they are useless here and should not make distraction.
             * @see LabelEvent
             */
            val LABEL_CLICK : EventType<LabelEvent> = EventType(LABEL_ANY, "LABEL_CLICK")

            /**
             * Incicate we moved a label. `labelIndex` will be the target
             * label's index. `labelX` and `labelY` will be the new coordinates
             * of the target label. Note that this event will only be dispacted
             * when we have finished moving a label.
             * @see LabelEvent
             */
            val LABEL_MOVE : EventType<LabelEvent> = EventType(LABEL_ANY, "LABEL_MOVE")

            /**
             * Incicate shoule remove a label. `labelIndex` will be NOT_FOUND
             * because the event target isn't a label. `labelX` and `labelY`
             * will be `Double.NaN` because the event target isn't a label.
             * @see LabelEvent
             */
            val LABEL_OTHER : EventType<LabelEvent> = EventType(LABEL_ANY, "LABEL_OTHER")
        }

        /**
         * @see Event.toString
         */
        override fun toString(): String {
            return "LabelEvent(type=$eventType, labelIndex=$labelIndex, displayX=$displayX, displayY=$displayY, labelX=$labelX, labelY=$labelY, button=${sourceEvent.button})"
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
    private val canvas = Canvas()

    /**
     * For labels display
     */
    private val anchorPane = AnchorPane().apply { isPickOnBounds = false }

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
    private val labelNodes: ObservableList<CLabel> get() = anchorPane.children as ObservableList<CLabel>

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

    private val useWheelToScaleProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun useWheelToScaleProperty(): BooleanProperty = useWheelToScaleProperty
    var useWheelToScale: Boolean by useWheelToScaleProperty

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

    private val imageProperty: ObjectProperty<Image> = imageView.imageProperty()
    fun imageProperty(): ObjectProperty<Image> = imageProperty
    var image: Image by imageProperty

    private val labelsProperty: ListProperty<TransLabel> = SimpleListProperty(FXCollections.emptyObservableList())
    fun labelsProperty(): ListProperty<TransLabel> = labelsProperty
    var labels: ObservableList<TransLabel> by labelsProperty

    private val labelRadiusProperty: DoubleProperty = SimpleDoubleProperty(24.0)
    fun labelRadiusProperty(): DoubleProperty = labelRadiusProperty
    var labelRadius: Double by labelRadiusProperty

    private val labelTextOpaqueProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun labelTextOpaqueProperty(): BooleanProperty = labelTextOpaqueProperty
    var isLabelTextOpaque: Boolean by labelTextOpaqueProperty

    private val labelColorOpacityProperty: DoubleProperty = SimpleDoubleProperty(0.5)
    fun labelColorOpacityProperty(): DoubleProperty = labelColorOpacityProperty
    var labelColorOpacity: Double by labelColorOpacityProperty

    private val newPictureScaleProperty: ObjectProperty<NewPictureScale> = SimpleObjectProperty(NewPictureScale.DEFAULT)
    fun newPictureScaleProperty(): ObjectProperty<NewPictureScale> = newPictureScaleProperty
    var newPictureScale: NewPictureScale by newPictureScaleProperty

    private val commonCursorProperty: ObjectProperty<Cursor> = SimpleObjectProperty(Cursor.DEFAULT)
    fun commonCursorProperty(): ObjectProperty<Cursor> = commonCursorProperty
    var commonCursor: Cursor by commonCursorProperty

    // endregion

    // region Properties:Selection

    private val selectedLabelsProperty: SetProperty<Int> = SimpleSetProperty(FXCollections.observableSet(HashSet()))
    /**
     * Press right-button of the mouse and drag to box-select some labels
     */
    fun selectedLabelsProperty(): ReadOnlySetProperty<Int> = selectedLabelsProperty
    /**
     * @see selectedLabelsProperty
     */
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
            val imageWidthBinding = imageProperty.transform(Image::getWidth).primitive()
            val imageHeightBinding = imageProperty.transform(Image::getHeight).primitive()
            add(imageView) {
                isPreserveRatio = true
                isPickOnBounds = true
            }
            add(anchorPane) {
                prefWidthProperty().bind(imageWidthBinding)
                prefHeightProperty().bind(imageHeightBinding)
            }
            add(canvas) {
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

        // Remove text when scale / scroll
        // NOTE: EventFilter is used to disable scroll when scale
        // NOTE: Horizon scroll use default impl: Shift + Scroll
        addEventFilter(ScrollEvent.SCROLL) {
            clearAllText()
        }

        // Scale
        scaleProperty.addListener(onNew<Number, Double> {
            root.scaleX = it
            root.scaleY = it
        })
        root.addEventFilter(ScrollEvent.SCROLL) {
            val isShortCutDown = it.isControlDown || it.isAltDown || it.isMetaDown
            if ((isShortCutDown && !useWheelToScale) || (!isShortCutDown && useWheelToScale)) {
                // In scale mode, with shift is one time faster
                val deltaScale = if (it.isShiftDown) {
                    if (it.deltaX > 0) 0.2 else -0.2
                } else {
                    if (it.deltaY > 0) 0.1 else -0.1
                }

                scale += deltaScale
                // x, y -> location not related to scale, based on left-top of the image
                // nLx = Lx + (imgW / 2 - x) * dS
                root.translateX += deltaScale * (image.width  / 2 - it.x)
                root.translateY += deltaScale * (image.height / 2 - it.y)

                it.consume()
            }
        }

        // Clear text
        root.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
            canvas.clearGraphicContext()
        }
        root.addEventHandler(MouseEvent.MOUSE_EXITED) {
            canvas.clearGraphicContext()
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

            canvas.clearGraphicContext()
            canvas.graphicsContext2D.lineWidth = 2.0 / scale
            canvas.graphicsContext2D.stroke = Color.BLACK
            canvas.graphicsContext2D.strokeRect(x, y, w, h)
        }
        root.addEventHandler(MouseEvent.MOUSE_RELEASED) {
            if (selecting) {
                canvas.clearGraphicContext()

                val shiftPercentX = shiftX / image.width
                val shiftPercentY = shiftY / image.height
                val eventPercentX = it.x / image.width
                val eventPercentY = it.y / image.height

                val rangeX = shiftPercentX.autoRangeTo(eventPercentX)
                val rangeY = shiftPercentY.autoRangeTo(eventPercentY)
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
                    if (it.wasAdded() && image !== INIT_IMAGE) {
                        it.addedSubList.forEach(this::createLabel)
                    }
                }
            }
        })
    }

    private fun createLabel(transLabel: TransLabel) {
        val label = CLabel().apply {
            indexProperty().bind(transLabel.indexProperty())
            colorProperty().bind(transLabel.colorProperty())
            radiusProperty().bind(labelRadiusProperty)
            textOpaqueProperty().bind(labelTextOpaqueProperty)
            colorOpacityProperty().bind(labelColorOpacityProperty)

            // Tooltip
            tooltip = Tooltip().apply {
                isWrapText = true
                font = font.s(24.0)
                textProperty().bind(transLabel.textProperty())

                // We show and hide it manually
                showDelay = Duration.INDEFINITE
                hideDelay = Duration.INDEFINITE
                showDuration = Duration.INDEFINITE
            }
        }

        // Hide text
        label.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
            canvas.clearGraphicContext()
            label.tooltip.hide()
        }
        label.addEventHandler(MouseEvent.MOUSE_EXITED) {
            canvas.clearGraphicContext()
            label.tooltip.hide()
        }

        // Draggable
        // ScenePos -> CursorPos; LayoutPos -> CtxPos
        // nLx = Lx + (nSx - Sx); nLy = Ly + (nSy - Sy)
        // nLx = (Lx - Sx) + nSx -> shiftN + sceneN
        label.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            // Mark immediately when this event will be consumed
            it.consume() // make sure root will not move together

            shiftX = label.anchorX - it.sceneX / scale
            shiftY = label.anchorY - it.sceneY / scale

            label.cursor = Cursor.MOVE
        }
        label.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
            // Mark immediately when this event will be consumed
            it.consume() // make sure root will not move together

            dragging = true

            val newAnchorX = shiftX + it.sceneX / scale
            val newAnchorY = shiftY + it.sceneY / scale

            //  0-----LR----    0 LR LR |
            //  |     LR        LR      |
            //  |LR LR|-----    LR      |
            //  |     |         --------|
            if (newAnchorX < 0 || newAnchorX > image.width - 2 * label.radius) return@addEventHandler
            if (newAnchorY < 0 || newAnchorY > image.height - 2 * label.radius) return@addEventHandler

            label.anchorX = newAnchorX
            label.anchorY = newAnchorY
        }
        label.addEventHandler(MouseEvent.MOUSE_RELEASED) {
            // Mark immediately when this event will be consumed
            it.consume() // make sure root will not move together

            label.cursor = Cursor.HAND

            if (dragging) fireEvent(LabelEvent(LabelEvent.LABEL_MOVE,
                it, transLabel.index,
                label.anchorX + it.x,
                label.anchorY + it.y,
                (label.anchorX + label.radius) / image.width,
                (label.anchorY + label.radius) / image.height,
            ))

            dragging = false
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
        }

        // Event handle
        // FIXME: Event doesn't dispatch if mouse is still but by scrolling the pointer moves on it
        // I know the Mouse_Moved event is dispatched when the mouse actually moves, but I want this happen, too.
        // see https://stackoverflow.com/questions/72169918/how-to-capture-the-event-when-node-moves-under-the-stand-still-mouse
        label.setOnMouseMoved {
            // Mark immediately when this event will be consumed
            it.consume() // disable further propagation

            fireEvent(LabelEvent(LabelEvent.LABEL_HOVER,
                it, transLabel.index,
                label.anchorX + it.x,
                label.anchorY + it.y,
                Double.NaN, Double.NaN
            ))
        }
        label.setOnMouseClicked {
            if (!it.isStillSincePress) return@setOnMouseClicked
            // Mark immediately when this event will be consumed
            it.consume() // disable further propagation

            when (it.button) {
                MouseButton.PRIMARY -> {
                    fireEvent(LabelEvent(LabelEvent.LABEL_CLICK,
                        it, transLabel.index,
                        label.anchorX + it.x,
                        label.anchorY + it.y,
                        Double.NaN, Double.NaN
                    ))
                }
                MouseButton.SECONDARY -> {
                    fireEvent(LabelEvent(LabelEvent.LABEL_REMOVE,
                        it, transLabel.index,
                        label.anchorX + it.x,
                        label.anchorY + it.y,
                        Double.NaN, Double.NaN
                    ))
                }
                else -> {
                    doNothing()
                }
            }
        }

        // Layout
        labelNodes.add(label)
        //Anchor-L-----  Anchor = imageWidth * x - LR
        //  |    R
        //  | LR X-----  x = (Anchor + LR) / imageWidth
        //  |    |
        label.anchorX = -label.radius + transLabel.x * image.width
        label.anchorY = -label.radius + transLabel.y * image.height
    }
    private fun removeLabel(transLabel: TransLabel) {
        val label = labelNodes.first { it.index == transLabel.index } ?: return

        // Unbind
        label.indexProperty().unbind()
        label.colorProperty().unbind()
        label.radiusProperty().unbind()
        label.textOpaqueProperty().unbind()
        label.colorOpacityProperty().unbind()

        // Remove view
        labelNodes.remove(label)
    }

    // Text rendering

    /**
     * Create some text at the given position
     * @param text Text to display
     * @param color Color of the text
     * @param x X coordinate where the text will be displayed, based on the image width
     * @param y Y coordinate where the text will be displayed, based on the image height
     */
    fun showText(text: String, color: Color, x: Double, y: Double) {
        val gc = canvas.graphicsContext2D

        // Clear
        gc.clearRect(0.0, 0.0, canvas.width, canvas.height)

        val s = shortenWideText(shortenLongText(text), (image.width - 2 * (SHIFT_X + TEXT_INSET)) / 2, TEXT_FONT)
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

    /**
     * Show TransLabel's Text at the give position
     * @param x X coordinate where the text will be displayed, based on the image width
     * @param y Y coordinate where the text will be displayed, based on the image height
     */
    fun showLabelText(labelIndex: Int, x: Double, y: Double) {
        val label = labelNodes.first { it.index == labelIndex }

        val screenBounds = root.localToScreen(root.boundsInLocal)
        label.tooltip.show(root,
            screenBounds.minX + x * scale + 8,
            screenBounds.minY + y * scale + 8,
        )
    }

    /**
     * Clear all displaying text
     */
    fun clearAllText() {
        canvas.clearGraphicContext()
        for (label in labelNodes) if (label.tooltip.isShowing) label.tooltip.hide()
    }

    // Layout position

    /**
     * Move the image where will make the target label
     * be displayed at the center of the LabelPane
     * @param labelIndex Index of the label which will be displaye at the center
     */
    fun moveToLabel(labelIndex: Int) {
        val label = labelNodes.firstOrNull { it.index == labelIndex } ?:return


        vvalue = 0.0
        hvalue = 0.0

        // Scaled (fake)
        // -> Image / 2 - (Image / 2 - Center) * Scale
        // -> Image / 2 * (1 - Scale) + Center * Scale
        val fakeX = image.width / 2 * (1 - scale) + label.anchorX * scale
        val fakeY = image.height / 2 * (1 - scale) + label.anchorY * scale

        // To center
        // -> Scroll / 2 = Layout + Fake + Radius
        // -> Layout = Scroll / 2 - Fake
        root.translateX = width  / 2 - fakeX - labelRadius
        root.translateY = height / 2 - fakeY - labelRadius
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
