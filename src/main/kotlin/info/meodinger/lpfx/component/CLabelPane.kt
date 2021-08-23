package info.meodinger.lpfx.component

import info.meodinger.lpfx.type.TransLabel
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.dialog.showException
import info.meodinger.lpfx.util.accelerator.isControlDown
import info.meodinger.lpfx.util.platform.MonoType
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.INIT_IMAGE
import info.meodinger.lpfx.util.resource.get

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.geometry.Pos
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

        // selected
        const val NOT_FOUND = -1

        // label display
        const val LABEL_RADIUS = 20.0
        const val LABEL_ALPHA = "80"

        // text display
        const val TEXT_SHIFT = 20.0
        const val TEXT_INSET = 10.0
        const val TEXT_ALPHA = "A0"
        val TEXT_FONT = Font(MonoType, 28.0)
    }

    // ----- event ----- //

    class LabelEvent(
        eventType: EventType<LabelEvent>,
        val source: MouseEvent,
        val labelIndex: Int,
        val labelX: Double, val labelY: Double,
        val rootX: Double, val rootY: Double,
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
    val defaultCursorProperty = SimpleObjectProperty(Cursor.DEFAULT)
    val colorListProperty = SimpleListProperty<String>(FXCollections.emptyObservableList())
    val selectedLabelIndexProperty = SimpleIntegerProperty(NOT_FOUND)
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
                if (minScale != NOT_SET) temp = Math.max(temp, minScale)
                if (maxScale != NOT_SET) temp = Math.min(temp, maxScale)
                initScaleProperty.value = temp
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
                if (minScale != NOT_SET) temp = Math.max(temp, minScale)
                if (maxScale != NOT_SET) temp = Math.min(temp, maxScale)
                scaleProperty.value = temp
            }
        }
    var defaultCursor: Cursor
        get() = defaultCursorProperty.value
        set(value) {
            defaultCursorProperty.value = value
        }
    var colorList: MutableList<String>
        get() = colorListProperty.value
        set(value) {
            // setAll not supported
            colorListProperty.value = FXCollections.observableList(value)
        }
    var selectedLabelIndex: Int
        get() = selectedLabelIndexProperty.value
        set(value) {
            selectedLabelIndexProperty.value = value
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
            println("${root.layoutX}-${root.layoutY}")
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
                Math.max(root.boundsInParent.maxX, viewportBounds.width),
                Math.max(root.boundsInParent.maxY, viewportBounds.height)
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
                onLabelPlace.handle(LabelEvent(LabelEvent.LABEL_PLACE, it, NOT_FOUND, it.x / imageWidth, it.y / imageHeight, it.x, it.y))
            }
        }

        root.children.add(view)
        root.children.add(textLayer)
        container.children.add(root)

        content = container
    }

    fun reset() {
        scale = initScale
        image = INIT_IMAGE
        selectedLabelIndex = NOT_FOUND

        root.layoutX = 0.0
        root.layoutY = 0.0
        setupLayers(0)
    }

    private fun getLabel(transLabel: TransLabel): CLabel {
        for (label in labels) if (label.index == transLabel.index) return label
        throw IllegalStateException(String.format(I18N["exception.illegal_state.label_not_found.format"], transLabel.index))
    }

    fun setupImage(path: String) {
        try {
            image = Image(File(path).toURI().toURL().toString())
            scale = width / imageWidth
        } catch (e : IOException) {
            showException(e)
        }
    }
    fun setupLayers(count: Int) {
        labelLayers.forEach { root.children.remove(it) }
        labelLayers.clear()
        removeText()

        for (i in 0 until count) {
            val pane = AnchorPane().also { it.isPickOnBounds = false }
            root.children.add(pane)
            labelLayers.add(pane)
        }

        textLayer.width = imageWidth
        textLayer.height = imageHeight
        textLayer.toFront()
    }
    fun setupLabels(transLabels: List<TransLabel>) {
        labels.clear()
        for (transLabel in transLabels) {
            placeLabel(transLabel)
        }
    }

    fun placeLabelLayer() {
        val pane = AnchorPane().also { it.isPickOnBounds = false }
        // Layout
        root.children.add(pane)
        // Add layer in list
        labelLayers.add(pane)
        // Move text layer to front
        textLayer.toFront()
    }
    fun placeLabel(transLabel: TransLabel) {
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

            AnchorPane.setLeftAnchor(label, shiftX + it.sceneX / scale)
            AnchorPane.setTopAnchor(label, shiftY + it.sceneY / scale)
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
            placeText(transLabel.text, Color.BLACK, it.x + label.layoutX, it.y + label.layoutY)
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

        // Layout
        AnchorPane.setLeftAnchor(label, imageWidth * transLabel.x)
        AnchorPane.setTopAnchor(label, imageHeight * transLabel.y)
        labelLayers[transLabel.groupId].children.add(label)

        // Add label in list
        labels.add(label)

        // Bind property
        label.indexProperty.bind(transLabel.indexProperty)
        label.colorProperty.bind(colorListProperty.valueAt(transLabel.groupIdProperty).asString("%s$LABEL_ALPHA"))
        transLabel.xProperty.bind(label.layoutXProperty().divide(view.image.widthProperty()))
        transLabel.yProperty.bind(label.layoutYProperty().divide(view.image.heightProperty()))
    }
    fun placeText(text: String, color: Color, x: Double, y: Double) {
        val gc = textLayer.graphicsContext2D
        val lineCount = text.length - text.replace("\n".toRegex(), "").length + 1
        val t = Text(text).also { it.font = TEXT_FONT }

        val textW = t.boundsInLocal.width
        val textH = t.boundsInLocal.height
        var textX = x + TEXT_SHIFT
        var textY = y + textH / lineCount

        val shapeW = textW + 2 * TEXT_INSET
        val shapeH = textH + 2 * TEXT_INSET
        var shapeX = x + (TEXT_SHIFT - TEXT_INSET)
        var shapeY = y

        if (shapeX + shapeW > imageWidth) {
            shapeX = x - shapeW
            textX = x - textW - TEXT_INSET
        }
        if (shapeY + shapeH > imageHeight) {
            shapeY = y - shapeH
            textY = y - textH - TEXT_INSET
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
        setupImage(picPath)
        setupLayers(layerCount)
        setupLabels(transLabels)
    }
}