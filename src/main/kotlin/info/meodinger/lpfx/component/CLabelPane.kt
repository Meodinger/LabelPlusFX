package info.meodinger.lpfx.component

import info.meodinger.lpfx.NOT_FOUND
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.type.TransLabel
import info.meodinger.lpfx.util.accelerator.isControlDown
import info.meodinger.lpfx.util.color.isColorHex
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.platform.MonoType
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.INIT_IMAGE
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.string.omitHighText
import info.meodinger.lpfx.util.string.omitWideText
import info.meodinger.lpfx.util.property.getValue
import info.meodinger.lpfx.util.property.setValue
import info.meodinger.lpfx.util.property.div
import info.meodinger.lpfx.util.property.plus

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
 * Location: info.meodinger.lpfx.component
 */

/**
 * A scalable, draggable ScrollPane that can display image, text and labels
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
        val TEXT_FONT = Font(MonoType, 32.0)
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

    val initScaleProperty = SimpleDoubleProperty(NOT_SET)
    val minScaleProperty = SimpleDoubleProperty(NOT_SET)
    val maxScaleProperty = SimpleDoubleProperty(NOT_SET)
    val scaleProperty = SimpleDoubleProperty(1.0)
    val colorHexListProperty = SimpleListProperty(FXCollections.observableArrayList<String>())
    val defaultCursorProperty = SimpleObjectProperty(Cursor.DEFAULT)
    val onLabelPlaceProperty = SimpleObjectProperty(EventHandler<LabelEvent> {})
    val onLabelRemoveProperty = SimpleObjectProperty(EventHandler<LabelEvent> {})
    val onLabelPointedProperty = SimpleObjectProperty(EventHandler<LabelEvent> {})
    val onLabelClickedProperty = SimpleObjectProperty(EventHandler<LabelEvent> {})
    val onLabelOtherProperty = SimpleObjectProperty(EventHandler<LabelEvent> {})
    val onLabelMoveProperty = SimpleObjectProperty(EventHandler<LabelEvent> {})

    var initScale: Double
        get() = initScaleProperty.value
        set(value) {
            if (value >= 0) {
                var temp = value
                if (minScale != NOT_SET) temp = temp.coerceAtLeast(minScale)
                if (maxScale != NOT_SET) temp = temp.coerceAtMost(maxScale)
                initScaleProperty.value = temp
            } else {
                throw IllegalArgumentException(I18N["exception.illegal_argument.negative_scale"])
            }
        }
    var minScale: Double
        get() = minScaleProperty.value
        set(value) {
            if (value < 0) return
            if (maxScale != NOT_SET && value > maxScale) return
            minScaleProperty.value = value
        }
    var maxScale: Double
        get() = maxScaleProperty.value
        set(value) {
            if (value < 0) return
            if (minScale != NOT_SET && value < minScale) return
            maxScaleProperty.value = value
        }
    var scale: Double
        get() = scaleProperty.value
        set(value) {
            if (value >= 0) {
                var temp = value
                if (minScale != NOT_SET) temp = temp.coerceAtLeast(minScale)
                if (maxScale != NOT_SET) temp = temp.coerceAtMost(maxScale)
                scaleProperty.value = temp
            }
        }
    var colorHexList: ObservableList<String> by colorHexListProperty
    var defaultCursor: Cursor by defaultCursorProperty
    var onLabelPlace: EventHandler<LabelEvent> by onLabelPlaceProperty
    var onLabelRemove: EventHandler<LabelEvent> by onLabelRemoveProperty
    var onLabelPointed: EventHandler<LabelEvent> by onLabelPointedProperty
    var onLabelClicked: EventHandler<LabelEvent> by onLabelClickedProperty
    var onLabelOther: EventHandler<LabelEvent> by onLabelOtherProperty
    var onLabelMove: EventHandler<LabelEvent> by onLabelMoveProperty
    var image: Image by view.imageProperty()

    private val imageWidth: Double get() = image.width
    private val imageHeight: Double get() = image.height

    init {
        textLayer.isMouseTransparent = true
        textLayer.graphicsContext2D.font = TEXT_FONT
        textLayer.graphicsContext2D.textBaseline = VPos.TOP
        view.isPreserveRatio = true
        view.isPickOnBounds = true
        root.alignment = Pos.CENTER

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

        // Scale
        root.addEventHandler(ScrollEvent.SCROLL) {
            this.removeText()
            if (isControlDown(it) || it.isAltDown) {
                scale += it.deltaY / 400
            }
        }
        scaleProperty.addListener { _, _, newValue ->
            val newScale = newValue as Double
            root.scaleX = newScale
            root.scaleY = newScale
            container.setPrefSize(
                container.boundsInParent.maxX.coerceAtLeast(viewportBounds.width),
                container.boundsInParent.maxY.coerceAtLeast(viewportBounds.height)
            )
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
            this.removeText()
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
                onLabelPlace.handle(LabelEvent(LabelEvent.LABEL_PLACE,
                    it, labels.size + 1,
                    it.x / imageWidth, it.y / imageHeight,
                    it.x, it.y
                ))
            }
        }

        root.children.add(view)
        root.children.add(textLayer)
        container.children.add(root)

        content = container
    }

    fun reset() {
        isVisible = false

        vvalue = 0.0
        hvalue = 0.0
        root.layoutX = 0.0
        root.layoutY = 0.0

        scale = initScale
        image = INIT_IMAGE

        setupLayers(0)

        moveToCenter()
        isVisible = true
    }
    fun clear() {
        isVisible = false

        vvalue = 0.0
        hvalue = 0.0
        root.layoutX = 0.0
        root.layoutY = 0.0

        image = INIT_IMAGE
        scale = initScale

        setupLayers(0)
    }
    fun render(picPath: String, layerCount: Int, transLabels: List<TransLabel>) {
        isVisible = false

        vvalue = 0.0
        hvalue = 0.0
        root.layoutX = 0.0
        root.layoutY = 0.0

        setupImage(picPath)
        setupLayers(layerCount)
        setupLabels(transLabels)

        isVisible = true
    }

    fun updateColor(id: Int, hex: String) {
        if (isColorHex(hex)) colorHexList[id] = hex
    }

    private fun getLabel(labelIndex: Int): CLabel {
        for (label in labels) if (label.index == labelIndex) return label
        throw IllegalArgumentException(String.format(I18N["exception.illegal_argument.label_not_found.format.i"], labelIndex))
    }
    private fun getLabelGroup(label: CLabel): Int {
        for (i in labelLayers.indices) if (labelLayers[i].children.contains(label)) return i
        throw IllegalArgumentException(String.format(I18N["exception.illegal_argument.label_not_found.format.i"], label.index))
    }

    @Throws(IOException::class)
    private fun setupImage(path: String) {
        image = Image(File(path).toURI().toURL().toString())
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
        labels.clear()
        for (transLabel in transLabels) {
            createLabel(transLabel)
        }
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

        val label = CLabel(
            transLabel.index,
            radius,
            Color.web(colorHexList[transLabel.groupId] + alpha)
        )

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
            this.removeText()
        }

        // Event handle
        label.setOnMouseMoved {
            onLabelPointed.handle(LabelEvent(LabelEvent.LABEL_POINTED,
                it, transLabel.index,
                transLabel.x, transLabel.y,
                label.layoutX + it.x, label.layoutY + it.y
            ))
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

        // Add label in list
        labels.add(label)

        // Bind property
        label.indexProperty.bind(transLabel.indexProperty)
        label.colorProperty.bind(Bindings.createObjectBinding(
            { Color.web(colorHexList[transLabel.groupId] + alpha) },
            colorHexListProperty, transLabel.groupIdProperty
        ))
        transLabel.xProperty.bind((label.layoutXProperty() + radius) / view.image.widthProperty())
        transLabel.yProperty.bind((label.layoutYProperty() + radius) / view.image.heightProperty())
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
        // Remove label in list
        labels.remove(label)
        // Remove label comp
        labelLayers[groupId].children.remove(label)
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