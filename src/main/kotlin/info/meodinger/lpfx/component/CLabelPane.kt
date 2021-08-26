package info.meodinger.lpfx.component

import info.meodinger.lpfx.NOT_FOUND
import info.meodinger.lpfx.type.TransLabel
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.dialog.showException
import info.meodinger.lpfx.util.accelerator.isControlDown
import info.meodinger.lpfx.util.platform.MonoType
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.INIT_IMAGE
import info.meodinger.lpfx.util.resource.get

import javafx.beans.binding.StringBinding
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
import kotlin.jvm.Throws

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 *
 *           |   Layout   | Width
 * -----------------------------
 * pane      | -          | actual width
 * container | 0 - Fixed  | ?
 * image     | left-top   | image width
 * root      | with image | image width
 * layer     | with image | image width
 */
class CLabelPane : ScrollPane() {

    companion object {

        // scale
        const val NOT_SET = -1.0

        // label display
        const val LABEL_RADIUS = 24.0
        const val LABEL_ALPHA = "80"

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
            val LABEL_OTHER = EventType(LABEL_ANY, "LABEL_OTHER")
            val LABEL_PLACE = EventType(LABEL_ANY, "LABEL_PLACE")
            val LABEL_REMOVE = EventType(LABEL_ANY, "LABEL_REMOVE")
            val LABEL_POINTED = EventType(LABEL_ANY, "LABEL_POINTED")
            val LABEL_CLICKED = EventType(LABEL_ANY, "LABEL_CLICKED")
        }
    }

    // ----- layer system ----- //

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
    private val view = ImageView()

    /**
     * For display, scale, drag, label, event handle
     */
    private val root = StackPane()

    /**
     * For contain, scale event handle (use mouse scroll)
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
    val colorListProperty = SimpleListProperty(FXCollections.emptyObservableList<String>())
    val selectedLabelIndexProperty = SimpleIntegerProperty(NOT_FOUND)
    val defaultCursorProperty = SimpleObjectProperty(Cursor.DEFAULT)
    val onLabelPlaceProperty = SimpleObjectProperty(EventHandler<LabelEvent> {})
    val onLabelRemoveProperty = SimpleObjectProperty(EventHandler<LabelEvent> {})
    val onLabelPointedProperty = SimpleObjectProperty(EventHandler<LabelEvent> {})
    val onLabelClickedProperty = SimpleObjectProperty(EventHandler<LabelEvent> {})
    val onLabelOtherProperty = SimpleObjectProperty(EventHandler<LabelEvent> {})

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
    var colorList: ObservableList<String>
        get() = colorListProperty.value
        set(value) {
            colorListProperty.value = value
        }
    var selectedLabelIndex: Int
        get() = selectedLabelIndexProperty.value
        set(value) {
            selectedLabelIndexProperty.value = value
        }
    var defaultCursor: Cursor
        get() = defaultCursorProperty.value
        set(value) {
            defaultCursorProperty.value = value
        }
    var onLabelPlace: EventHandler<LabelEvent>
        get() = onLabelPlaceProperty.value
        set(value) {
            onLabelPlaceProperty.value = value
        }
    var onLabelRemove: EventHandler<LabelEvent>
        get() = onLabelRemoveProperty.value
        set(value) {
            onLabelRemoveProperty.value = value
        }
    var onLabelPointed: EventHandler<LabelEvent>
        get() = onLabelPointedProperty.value
        set(value) {
            onLabelPointedProperty.value = value
        }
    var onLabelClicked: EventHandler<LabelEvent>
        get() = onLabelClickedProperty.value
        set(value) {
            onLabelClickedProperty.value = value
        }
    var onLabelOther: EventHandler<LabelEvent>
        get() = onLabelOtherProperty.value
        set(value) {
            onLabelOtherProperty.value = value
        }
    var image: Image
        get() = view.image
        set(value) {
            view.image = value
        }
    val imageWidth: Double
        get() = image.width
    val imageHeight: Double
        get() = image.height


    init {
        textLayer.isMouseTransparent = true
        textLayer.graphicsContext2D.font = TEXT_FONT
        textLayer.graphicsContext2D.textBaseline = VPos.TOP
        view.image = INIT_IMAGE
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
                root.boundsInParent.maxX.coerceAtLeast(viewportBounds.width),
                root.boundsInParent.maxY.coerceAtLeast(viewportBounds.height)
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
            onLabelOther.handle(LabelEvent(LabelEvent.LABEL_OTHER, it, NOT_FOUND, it.x / imageWidth, it.y / imageHeight, it.x, it.y))
        }
        root.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY) {
                if (!it.isStillSincePress) return@addEventHandler
                onLabelPlace.handle(LabelEvent(LabelEvent.LABEL_PLACE, it, NOT_FOUND, it.x / imageWidth, it.y / imageHeight, it.x, it.y))
            }
        }

        root.children.add(view)
        root.children.add(textLayer)
        container.children.add(root)

        content = container
    }

    fun reset() {
        isVisible = false

        scale = initScale
        image = INIT_IMAGE
        selectedLabelIndex = NOT_FOUND

        root.layoutX = 0.0
        root.layoutY = 0.0
        setupLayers(0)

        moveToCenter()
        isVisible = true
    }

    private fun getLabel(transLabel: TransLabel): CLabel {
        for (label in labels) if (label.index == transLabel.index) return label
        throw IllegalArgumentException(String.format(I18N["exception.illegal_argument.label_not_found.format.i"], transLabel.index))
    }

    @Throws(IOException::class)
    private fun setupImage(path: String) {
        val file = File(path)
        if (file.exists()) {
            image = Image(file.toURI().toURL().toString())
            scale = width / imageWidth
        } else {
            image = INIT_IMAGE
            scale = initScale
            throw IOException(String.format(I18N["exception.io.picture_not_found.format.s"], path))
        }
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
    fun createLabel(transLabel: TransLabel) {
        val label = CLabel(
            transLabel.index,
            LABEL_RADIUS,
            colorList[transLabel.groupId] + LABEL_ALPHA
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
            if (newLayoutX < 0 || newLayoutX > imageWidth - 2 * LABEL_RADIUS) return@addEventHandler
            if (newLayoutY < 0 || newLayoutY > imageHeight - 2 * LABEL_RADIUS) return@addEventHandler

            AnchorPane.setLeftAnchor(label, newLayoutX)
            AnchorPane.setTopAnchor(label, newLayoutY)
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

        // Selected index
        label.addEventHandler(MouseEvent.MOUSE_CLICKED) {
             selectedLabelIndex = transLabel.index
        }

        // Text display
        label.addEventHandler(MouseEvent.MOUSE_MOVED) {
            removeText()
            createText(transLabel.text, Color.BLACK, it.x + label.layoutX, it.y + label.layoutY)
            it.consume()
        }

        // Event handle
        label.setOnMouseMoved {
            onLabelPointed.handle(LabelEvent(LabelEvent.LABEL_POINTED, it, transLabel.index, transLabel.x, transLabel.y, label.layoutX + it.x, label.layoutY + it.y))
        }
        label.setOnMouseClicked {
            if (!it.isStillSincePress) return@setOnMouseClicked
            if (it.button == MouseButton.PRIMARY) {
                onLabelClicked.handle(LabelEvent(LabelEvent.LABEL_CLICKED, it, transLabel.index, transLabel.x, transLabel.y, label.layoutX + it.x, label.layoutY + it.y))
            } else if (it.button == MouseButton.SECONDARY) {
                onLabelRemove.handle(LabelEvent(LabelEvent.LABEL_REMOVE, it, transLabel.index, transLabel.x, transLabel.y, label.layoutX + it.x, label.layoutY + it.y))
            }
        }

        //Anchor-L-----  Anchor = imageWidth * x - LR
        //  |    R
        //  | LR X-----  x = (Anchor + LR) / imageWidth
        //  |    |

        // Layout
        AnchorPane.setLeftAnchor(label, imageWidth * transLabel.x - LABEL_RADIUS)
        AnchorPane.setTopAnchor(label, imageHeight * transLabel.y - LABEL_RADIUS)
        labelLayers[transLabel.groupId].children.add(label)

        // Add label in list
        labels.add(label)

        // Bind property
        label.colorProperty.bind(object : StringBinding() {

            init {
                bind(colorListProperty)
                bind(transLabel.groupIdProperty)
            }

            override fun computeValue(): String {
                val colorBinding = colorListProperty.valueAt(transLabel.groupIdProperty)
                if (colorBinding.isNotNull.value) {
                    return colorBinding.value + LABEL_ALPHA
                }
                return "000000$LABEL_ALPHA"
            }

        })
        label.indexProperty.bind(transLabel.indexProperty)
        transLabel.xProperty.bind(label.layoutXProperty().add(LABEL_RADIUS).divide(view.image.widthProperty()))
        transLabel.yProperty.bind(label.layoutYProperty().add(LABEL_RADIUS).divide(view.image.heightProperty()))
    }
    fun createText(text: String, color: Color, x: Double, y: Double) {
        val gc = textLayer.graphicsContext2D
        val t = Text(text).also { it.font = TEXT_FONT }

        val textW = t.boundsInLocal.width
        val textH = t.boundsInLocal.height
        val shapeW = textW + 2 * TEXT_INSET
        val shapeH = textH + 2 * TEXT_INSET

        //
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
        gc.fillText(text, textX, textY)
    }

    fun removeLabelLayer(groupId: Int) {
        val layer = labelLayers[groupId]
        // Remove labels
        labels.removeAll(layer.children)
        layer.children.clear()
        // Remove layer
        labelLayers.remove(layer)
        root.children.remove(layer) // Bottom is view
    }
    fun removeLabel(transLabel: TransLabel) {
        val label = getLabel(transLabel)
        // Remove label in list
        labels.remove(label)
        // Remove label comp
        labelLayers[transLabel.groupId].children.remove(label)
        // Edit data
        if (selectedLabelIndex == label.index) selectedLabelIndex = NOT_FOUND
    }
    fun removeText() {
        textLayer.graphicsContext2D.clearRect(0.0, 0.0, textLayer.width, textLayer.height)
    }

    fun moveToLabel(transLabel: TransLabel) {
        val label = getLabel(transLabel)

        vvalue = 0.0
        hvalue = 0.0

        val centerX = AnchorPane.getLeftAnchor(label)
        val centerY = AnchorPane.getTopAnchor(label)

        /**
         * Scaled (fake)
         *  -> Image / 2 - (Image / 2 - Center) * Scale
         *  -> Image / 2 * (1 - Scale) + Center * Scale
         */
        val fakeX = imageWidth / 2 * (1 - scale) + centerX * scale
        val fakeY = imageHeight / 2 * (1 - scale) + centerY * scale

        /**
         * To center
         *  -> Scroll / 2 = Layout + Fake
         *  -> Layout = Scroll / 2 - Fake
         */
        root.layoutX = width / 2 - fakeX
        root.layoutY = height / 2 - fakeY
    }
    fun moveToZero() {
        root.layoutX = - (1 - scale) * imageWidth / 2
        root.layoutY = - (1 - scale) * imageHeight / 2
    }
    fun moveToCenter() {
        root.layoutX = (width - imageWidth) / 2
        root.layoutY = (height - imageHeight) / 2
    }

    fun update(picPath: String, layerCount: Int, transLabels: List<TransLabel>) {
        this.isDisable = true
        try {
            setupImage(picPath)
        } catch (e: IOException) {
            setupLayers(0)
            showException(e)
            return
        }
        setupLayers(layerCount)
        setupLabels(transLabels)
        this.isDisable = false
    }
}