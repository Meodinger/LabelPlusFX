package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.SCROLL_DELTA
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.accelerator.isAltDown
import ink.meodinger.lpfx.util.color.toHex
import ink.meodinger.lpfx.util.component.withContent
import ink.meodinger.lpfx.util.platform.MonoFont
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.INIT_IMAGE
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.omitHighText
import ink.meodinger.lpfx.util.string.omitWideText

import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import java.io.File
import java.io.IOException


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A scalable, draggable ScrollPane that can display image, text and labels
 *
 * Bind Status: Semi-bind (ColorHexList)
 */
class CLabelPane : ScrollPane() {

    companion object {

        // scale
        const val NOT_SET = -1.0

        // text display
        /**
         * Rect based shift
         */
        const val SHIFT_X = 20.0
        const val TEXT_INSET = 10.0
        const val TEXT_ALPHA = "A0"
        val TEXT_FONT = Font(MonoFont, 32.0)
    }

    // ----- Exception ----- //
    class LabelPaneException(message: String) : IOException(message) {
        companion object {
            fun pictureNotFound(picPath: String) = LabelPaneException("Picture $picPath not found")
        }
    }

    // ----- event ----- //

    class LabelEvent(
        eventType: EventType<LabelEvent>,
        val source: MouseEvent,
        val labelIndex: Int,
        val labelX: Double, val labelY: Double,
        val displayX: Double, val displayY: Double,
    ) : Event(eventType) {
        companion object {
            val LABEL_ANY = EventType<LabelEvent>(EventType.ROOT)
            val LABEL_MOVE = EventType(LABEL_ANY, "LABEL_MOVE")
            val LABEL_OTHER = EventType(LABEL_ANY, "LABEL_OTHER")
            val LABEL_PLACE = EventType(LABEL_ANY, "LABEL_PLACE")
            val LABEL_REMOVE = EventType(LABEL_ANY, "LABEL_REMOVE")
            val LABEL_POINTED = EventType(LABEL_ANY, "LABEL_POINTED")
            val LABEL_CLICKED = EventType(LABEL_ANY, "LABEL_CLICKED")
        }
    }

    // ----- layer system ----- //

    /**
     *           |   Layout   | Width
     * -----------------------------
     * pane      | -          | actual width
     * container | 0 - Fixed  | ?
     * image     | left-top   | image width
     * root      | with image | image width
     * layer     | with image | image width
     */

    /**
     * For all text display
     */
    private val textLayer = Canvas()

    /**
     * For group-based label display
     */
    private val labelLayers = ArrayList<AnchorPane>()

    /**
     * For image display
     */
    private val view = ImageView(INIT_IMAGE)

    /**
     * For display, scale, drag, label, event handle
     */
    private val root = StackPane()

    /**
     * For contain (provide space to drag root)
     */
    private val container = AnchorPane()

    // ----- runtime data ----- //

    private var shiftX = 0.0
    private var shiftY = 0.0
    private val labels = ArrayList<CLabel>()

    // ----- properties ----- //

    private val initScaleProperty: DoubleProperty = SimpleDoubleProperty(NOT_SET)
    private val minScaleProperty:  DoubleProperty = SimpleDoubleProperty(NOT_SET)
    private val maxScaleProperty:  DoubleProperty = SimpleDoubleProperty(NOT_SET)
    private val scaleProperty:     DoubleProperty = SimpleDoubleProperty(1.0)
    fun initScaleProperty():       DoubleProperty = initScaleProperty
    fun minScaleProperty():        DoubleProperty = minScaleProperty
    fun maxScaleProperty():        DoubleProperty = maxScaleProperty
    fun scaleProperty():           DoubleProperty = scaleProperty
    var initScale: Double
        get() = initScaleProperty.get()
        set(value) {
            if (value >= 0) {
                var temp = value
                if (minScale != NOT_SET) temp = temp.coerceAtLeast(minScale)
                if (maxScale != NOT_SET) temp = temp.coerceAtMost(maxScale)
                initScaleProperty.set(temp)
            } else {
                throw IllegalArgumentException(String.format(I18N["exception.scale.negative_scale.d"], value))
            }
        }
    var minScale: Double
        get() = minScaleProperty.get()
        set(value) {
            if (value < 0) return
            if (maxScale != NOT_SET && value > maxScale) return
            minScaleProperty.set(value)
        }
    var maxScale: Double
        get() = maxScaleProperty.get()
        set(value) {
            if (value < 0) return
            if (minScale != NOT_SET && value < minScale) return
            maxScaleProperty.set(value)
        }
    var scale: Double
        get() = scaleProperty.get()
        set(value) {
            if (value >= 0) {
                var temp = value
                if (minScale != NOT_SET) temp = temp.coerceAtLeast(minScale)
                if (maxScale != NOT_SET) temp = temp.coerceAtMost(maxScale)
                scaleProperty.set(temp)
            }
        }


    private val onLabelPlaceProperty:   ObjectProperty<EventHandler<LabelEvent>> = SimpleObjectProperty(EventHandler<LabelEvent> {})
    private val onLabelRemoveProperty:  ObjectProperty<EventHandler<LabelEvent>> = SimpleObjectProperty(EventHandler<LabelEvent> {})
    private val onLabelPointedProperty: ObjectProperty<EventHandler<LabelEvent>> = SimpleObjectProperty(EventHandler<LabelEvent> {})
    private val onLabelClickedProperty: ObjectProperty<EventHandler<LabelEvent>> = SimpleObjectProperty(EventHandler<LabelEvent> {})
    private val onLabelMoveProperty:    ObjectProperty<EventHandler<LabelEvent>> = SimpleObjectProperty(EventHandler<LabelEvent> {})
    private val onLabelOtherProperty:   ObjectProperty<EventHandler<LabelEvent>> = SimpleObjectProperty(EventHandler<LabelEvent> {})
    fun onLabelPlaceProperty():         ObjectProperty<EventHandler<LabelEvent>> = onLabelPlaceProperty
    fun onLabelRemoveProperty():        ObjectProperty<EventHandler<LabelEvent>> = onLabelRemoveProperty
    fun onLabelPointedProperty():       ObjectProperty<EventHandler<LabelEvent>> = onLabelPointedProperty
    fun onLabelClickedProperty():       ObjectProperty<EventHandler<LabelEvent>> = onLabelClickedProperty
    fun onLabelMoveProperty():          ObjectProperty<EventHandler<LabelEvent>> = onLabelMoveProperty
    fun onLabelOtherProperty():         ObjectProperty<EventHandler<LabelEvent>> = onLabelOtherProperty
    val onLabelPlace:                                  EventHandler<LabelEvent> by onLabelPlaceProperty
    val onLabelRemove:                                 EventHandler<LabelEvent> by onLabelRemoveProperty
    val onLabelPointed:                                EventHandler<LabelEvent> by onLabelPointedProperty
    val onLabelClicked:                                EventHandler<LabelEvent> by onLabelClickedProperty
    val onLabelMove:                                   EventHandler<LabelEvent> by onLabelMoveProperty
    val onLabelOther:                                  EventHandler<LabelEvent> by onLabelOtherProperty
    fun setOnLabelPlace(handler: EventHandler<LabelEvent>)                       = onLabelPlaceProperty.set(handler)
    fun setOnLabelRemove(handler: EventHandler<LabelEvent>)                      = onLabelRemoveProperty.set(handler)
    fun setOnLabelPointed(handler: EventHandler<LabelEvent>)                     = onLabelPointedProperty.set(handler)
    fun setOnLabelClicked(handler: EventHandler<LabelEvent>)                     = onLabelClickedProperty.set(handler)
    fun setOnLabelMove(handler: EventHandler<LabelEvent>)                        = onLabelMoveProperty.set(handler)
    fun setOnLabelOther(handler: EventHandler<LabelEvent>)                       = onLabelOtherProperty.set(handler)

    private val colorHexListProperty: ListProperty<String> = SimpleListProperty(FXCollections.observableArrayList())
    fun colorHexListProperty(): ListProperty<String> = colorHexListProperty
    var colorHexList: ObservableList<String> by colorHexListProperty

    private val defaultCursorProperty: ObjectProperty<Cursor> = SimpleObjectProperty(Cursor.DEFAULT)
    fun defaultCursorProperty(): ObjectProperty<Cursor> = defaultCursorProperty
    var defaultCursor: Cursor by defaultCursorProperty

    private var image: Image by view.imageProperty()
    private val imageWidth: Double get() = image.width
    private val imageHeight: Double get() = image.height

    init {
        textLayer.isMouseTransparent = true
        textLayer.graphicsContext2D.font = TEXT_FONT
        textLayer.graphicsContext2D.textBaseline = VPos.TOP
        view.isPreserveRatio = true
        view.isPickOnBounds = true
        root.alignment = Pos.CENTER

        addEventFilter(ScrollEvent.SCROLL) {
            // Remove text when scroll event fired
            removeText()

            // Horizon scroll
            if (it.isControlDown) {
                hvalue -= it.deltaY / (10 * SCROLL_DELTA)
                it.consume()
            }
        }

        // Scale
        scaleProperty.addListener(onNew<Number, Double> {
            root.scaleX = it
            root.scaleY = it
        })
        root.addEventFilter(ScrollEvent.SCROLL) {
            if (isAltDown(it)) {
                scale += it.deltaY / (10 * SCROLL_DELTA)
                it.consume()
            }
        }

        // Draggable
        // ScenePos -> CursorPos; LayoutPos -> CtxPos
        // nLx = Lx + (nSx - Sx); nLy = Ly + (nSy - Sy)
        // nLx = (Lx - Sx) + nSx -> shiftN + sceneN
        root.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            if (!it.isConsumed) {
                shiftX = root.layoutX - it.sceneX
                shiftY = root.layoutY - it.sceneY
                root.cursor = Cursor.MOVE
            }
        }
        root.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
            if (!it.isConsumed) {
                root.layoutX = shiftX + it.sceneX
                root.layoutY = shiftY + it.sceneY
            }
        }
        root.addEventHandler(MouseEvent.MOUSE_RELEASED) {
            root.cursor = defaultCursor
        }

        // Cursor
        root.addEventHandler(MouseEvent.MOUSE_ENTERED) {
            root.cursor = defaultCursor
        }
        root.addEventHandler(MouseEvent.MOUSE_MOVED) {
            root.cursor = defaultCursor
        }
        root.addEventHandler(MouseEvent.MOUSE_EXITED) {
            root.cursor = defaultCursor
            removeText()
        }

        // Handle
        root.addEventHandler(MouseEvent.MOUSE_MOVED) {
            onLabelOther.handle(LabelEvent(LabelEvent.LABEL_OTHER,
                it, NOT_FOUND,
                it.x / imageWidth, it.y / imageHeight,
                it.x, it.y
            ))
        }
        root.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY) {
                if (!it.isStillSincePress) return@addEventHandler

                // Make sure all labels will not be placed partial outside
                val pickRadius = Settings[Settings.LabelRadius].asDouble().coerceAtLeast(CLabel.MIN_PICK_RADIUS)
                if (it.x <= pickRadius || it.x + pickRadius >= imageWidth) return@addEventHandler
                if (it.y <= pickRadius || it.y + pickRadius >= imageHeight) return@addEventHandler

                onLabelPlace.handle(LabelEvent(LabelEvent.LABEL_PLACE,
                    it, labels.size + 1,
                    it.x / imageWidth, it.y / imageHeight,
                    it.x, it.y
                ))
            }
        }

        root.children.add(view)
        root.children.add(textLayer)

        content = container withContent root
    }

    fun reset() {
        container.isDisable = true

        vvalue = 0.0
        hvalue = 0.0
        root.layoutX = 0.0
        root.layoutY = 0.0

        scale = initScale

        setupImage(INIT_IMAGE)
        setupLayers(0)
        setupLabels(emptyList())

        moveToCenter()

        container.isDisable = false
    }

    /**
     * Render CLabels
     *
     * @throws LabelPaneException when picture not found
     * @throws IOException when Image load failed
     */
    @Throws(LabelPaneException::class, IOException::class)
    fun render(picFile: File, layerCount: Int, transLabels: List<TransLabel>) {
        container.isDisable = true

        vvalue = 0.0
        hvalue = 0.0
        root.layoutX = 0.0
        root.layoutY = 0.0

        if (picFile.exists()) {
            setupImage(picFile)
            setupLayers(layerCount)
            setupLabels(transLabels)

            container.isDisable = false
        } else {
            setupImage(INIT_IMAGE)
            setupLayers(0)
            setupLabels(emptyList())

            scale = initScale
            moveToCenter()

            throw LabelPaneException.pictureNotFound(picFile.path)
        }
    }

    private fun getLabel(labelIndex: Int): CLabel {
        for (label in labels) if (label.index == labelIndex) return label
        throw IllegalArgumentException(String.format(I18N["exception.label_pane.label_not_found.i"], labelIndex))
    }
    private fun getLabelGroup(label: CLabel): Int {
        for (i in labelLayers.indices) if (labelLayers[i].children.contains(label)) return i
        throw IllegalArgumentException(String.format(I18N["exception.label_pane.label_not_found.i"], label.index))
    }

    @Throws(IOException::class)
    private fun setupImage(file: File) {
        setupImage(Image(file.toURI().toURL().toString()))
    }
    private fun setupImage(image: Image) {
        this.image = image
    }
    private fun setupLayers(count: Int) {
        labelLayers.forEach { root.children.remove(it) }
        labelLayers.clear()

        removeText()
        textLayer.width = imageWidth
        textLayer.height = imageHeight

        for (i in 0 until count) createLabelLayer()
    }
    private fun setupLabels(transLabels: List<TransLabel>) {
        for (label in labels) {
            label.indexProperty().unbind()
            label.colorProperty().unbind()
        }
        labels.clear()

        for (transLabel in transLabels) createLabel(transLabel)
    }

    fun createLabelLayer() {
        val pane = AnchorPane().also { it.isPickOnBounds = false }
        // Layout
        root.children.add(pane)
        // Add layer in list
        labelLayers.add(pane)
        // Move text layer to front
        textLayer.toFront()
    }
    /**
     * Properties (x, y) of argument TransLabel will bind to created CLabel;
     *
     * Properties (groupId, index) of created CLabel will bind to TransLabel
     */
    fun createLabel(transLabel: TransLabel) {
        val radius = Settings[Settings.LabelRadius].asDouble()
        val alpha = Settings[Settings.LabelAlpha].asString()

        val label = CLabel(radius = radius).also {
            it.indexProperty().bind(transLabel.indexProperty)
            it.colorProperty().bind(Bindings.createObjectBinding(
                { Color.web(colorHexList[transLabel.groupId] + alpha) },
                colorHexListProperty, transLabel.groupIdProperty
            ))
        }

        // Draggable
        // ScenePos -> CursorPos; LayoutPos -> CtxPos
        // nLx = Lx + (nSx - Sx); nLy = Ly + (nSy - Sy)
        // nLx = (Lx - Sx) + nSx -> shiftN + sceneN
        label.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            it.consume()

            shiftX = label.layoutX -  it.sceneX / scale
            shiftY = label.layoutY -it.sceneY / scale
            label.cursor = Cursor.MOVE
        }
        label.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
            it.consume()
            removeText()

            val newLayoutX = shiftX + it.sceneX / scale
            val newLayoutY = shiftY + it.sceneY / scale

            //  0--L-----    0 LR LR |
            //  |  R         LR      |
            //  |LR|-----    LR      |
            //  |  |         --------|
            if (newLayoutX < 0 || newLayoutX > imageWidth - 2 * radius) return@addEventHandler
            if (newLayoutY < 0 || newLayoutY > imageHeight - 2 * radius) return@addEventHandler

            label.layoutX = newLayoutX
            label.layoutY = newLayoutY

            onLabelMove.handle(LabelEvent(LabelEvent.LABEL_MOVE,
                it, transLabel.index,
                transLabel.x, transLabel.y,
                label.layoutX + it.x, label.layoutY + it.y
            ))
        }
        label.addEventHandler(MouseEvent.MOUSE_RELEASED) {
            label.cursor = Cursor.HAND
        }

        // Cursor
        label.addEventHandler(MouseEvent.MOUSE_ENTERED) {
            label.cursor = Cursor.HAND
        }
        label.addEventHandler(MouseEvent.MOUSE_MOVED) {
            label.cursor = Cursor.HAND
        }
        label.addEventHandler(MouseEvent.MOUSE_EXITED) {
            label.cursor = defaultCursor
            removeText()
        }

        // Event handle
        label.setOnMouseMoved {
            onLabelPointed.handle(LabelEvent(LabelEvent.LABEL_POINTED,
                it, transLabel.index,
                transLabel.x, transLabel.y,
                label.layoutX + it.x, label.layoutY + it.y
            ))
            it.consume()
        }
        label.setOnMouseClicked {
            if (!it.isStillSincePress) return@setOnMouseClicked
            if (it.button == MouseButton.PRIMARY) {
                onLabelClicked.handle(LabelEvent(LabelEvent.LABEL_CLICKED,
                    it, transLabel.index,
                    transLabel.x, transLabel.y,
                    label.layoutX + it.x, label.layoutY + it.y
                ))
            } else if (it.button == MouseButton.SECONDARY) {
                onLabelRemove.handle(LabelEvent(LabelEvent.LABEL_REMOVE,
                    it, transLabel.index,
                    transLabel.x, transLabel.y,
                    label.layoutX + it.x, label.layoutY + it.y
                ))
            }
        }

        //Anchor-L-----  Anchor = imageWidth * x - LR
        //  |    R
        //  | LR X-----  x = (Anchor + LR) / imageWidth
        //  |    |

        // Layout
        label.layoutX = imageWidth * transLabel.x - radius
        label.layoutY = imageHeight * transLabel.y - radius
        labelLayers[transLabel.groupId].children.add(label)

        // Bind
        transLabel.xProperty.bind((label.layoutXProperty() + radius) / view.image.widthProperty())
        transLabel.yProperty.bind((label.layoutYProperty() + radius) / view.image.heightProperty())

        // Add label in list
        labels.add(label)
    }
    fun createText(text: String, color: Color, x: Double, y: Double) {
        val gc = textLayer.graphicsContext2D
        val s = omitWideText(omitHighText(text), (imageWidth - 2 * (SHIFT_X + TEXT_INSET)) / 2, TEXT_FONT)
        val t = Text(s).also { it.font = TEXT_FONT }

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

        if (shapeX + shapeW > imageWidth) {
            textX = x - textW - SHIFT_X - TEXT_INSET
            shapeX = x - shapeW - SHIFT_X
        }
        if (shapeY + shapeH > imageHeight) {
            textY = y - textH - TEXT_INSET
            shapeY = y - shapeH
        }

        gc.fill = Color.web(Color.WHEAT.toHex() + TEXT_ALPHA)
        gc.fillRect(shapeX, shapeY, shapeW, shapeH)
        gc.stroke = Color.DARKGRAY
        gc.strokeRect(shapeX, shapeY, shapeW, shapeH)
        gc.fill = color
        gc.fillText(t.text, textX, textY)
    }

    fun removeLabelLayer(groupId: Int) {
        // When this method called, means there
        // are no labels in this group. So no
        // need to edit labels

        val layer = labelLayers[groupId]

        // Remove layer in list
        labelLayers.remove(layer)
        // Remove layer comp
        root.children.remove(layer)
    }
    fun removeLabel(labelIndex: Int) {
        val label = getLabel(labelIndex)
        val groupId = getLabelGroup(label)

        // Unbind
        label.indexProperty().unbind()
        label.colorProperty().unbind()

        // Remove view
        labelLayers[groupId].children.remove(label)
        // Remove data
        labels.remove(label)
    }
    fun removeText() {
        textLayer.graphicsContext2D.clearRect(0.0, 0.0, textLayer.width, textLayer.height)
    }

    fun moveToLabel(labelIndex: Int) {
        vvalue = 0.0
        hvalue = 0.0

        val label = getLabel(labelIndex)

        //
        // Scaled (fake)
        // -> Image / 2 - (Image / 2 - Center) * Scale
        // -> Image / 2 * (1 - Scale) + Center * Scale
        //
        val fakeX = imageWidth / 2 * (1 - scale) + label.layoutX * scale
        val fakeY = imageHeight / 2 * (1 - scale) + label.layoutY * scale

        //
        // To center
        // -> Scroll / 2 = Layout + Fake
        // -> Layout = Scroll / 2 - Fake
        //
        root.layoutX = width / 2 - fakeX
        root.layoutY = height / 2 - fakeY
    }
    fun moveToZero() {
        vvalue = 0.0
        hvalue = 0.0

        root.layoutX = - (1 - scale) * imageWidth / 2
        root.layoutY = - (1 - scale) * imageHeight / 2
    }
    fun moveToCenter() {
        vvalue = 0.0
        hvalue = 0.0

        root.layoutX = (width - imageWidth) / 2
        root.layoutY = (height - imageHeight) / 2
    }

    fun fitToPane() {
        scale = width / imageWidth
    }
}
