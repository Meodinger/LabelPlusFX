package info.meodinger.lpfx.component

import info.meodinger.lpfx.type.TransLabel
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.dialog.showException
import info.meodinger.lpfx.util.keyboard.isControlDown
import info.meodinger.lpfx.util.platform.MonoType
import info.meodinger.lpfx.util.resource.INIT_IMAGE

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

        // layer
        const val NOT_FOUND = -1
        const val NOT_SET = -1.0

        // general
        const val LINE_HEIGHT_RATIO = 0.5

        // label
        const val LABEL_RADIUS = 20.0
        const val LABEL_ALPHA = "80"

        // display
        const val DISPLAY_SHIFT = 20.0
        const val DISPLAY_INSET = 10.0
        const val DISPLAY_ALPHA = "A0"
        const val DISPLAY_FONT_SIZE = 28.0
        val DISPLAY_FONT = Font(MonoType, DISPLAY_FONT_SIZE)
    }

    // ----- event ----- //

    class LabelEvent(
        eventType: EventType<LabelEvent>,
        val source: MouseEvent,
        val labelIndex: Int,
    ) : Event(eventType) {
        companion object {
            val LABEL_ANY = EventType<LabelEvent>(EventType.ROOT)
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
    private val labels = ArrayList<CLabel?>()

    // ----- properties ----- //

    val initScaleProperty = SimpleDoubleProperty(NOT_SET)
    val minScaleProperty = SimpleDoubleProperty(NOT_SET)
    val maxScaleProperty = SimpleDoubleProperty(NOT_SET)
    val scaleProperty = SimpleDoubleProperty(1.0)
    val defaultCursorProperty = SimpleObjectProperty(Cursor.DEFAULT)
    val onLabelPointedProperty = SimpleObjectProperty(EventHandler<LabelEvent> { println(it) })
    val onLabelClickedProperty = SimpleObjectProperty(EventHandler<LabelEvent> { println(it) })
    val selectedLabelIndexProperty = SimpleIntegerProperty(NOT_FOUND)
    val colorListProperty = SimpleListProperty<String>()

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
    var selectedLabelIndex: Int
        get() = selectedLabelIndexProperty.value
        set(value) {
            selectedLabelIndexProperty.value = value
        }
    var colorList: MutableList<String>
        get() = colorListProperty.value
        set(value) {
            colorListProperty.value = FXCollections.observableList(value)
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
        textLayer.graphicsContext2D.font = DISPLAY_FONT
        view.image = INIT_IMAGE
        view.isPreserveRatio = true
        view.isPickOnBounds = true
        root.alignment = Pos.CENTER

        // Draggable
        // ScenePos -> CursorPos; LayoutPos -> CtxPos
        // nLx = Lx + (nSx - Sx); nLy = Ly + (nSy - Sy)
        // nLx = (Lx - Sx) + nSx -> shiftN + sceneN
        root.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            shiftX = root.layoutX -  it.sceneX
            shiftY = root.layoutY -it.sceneY
            root.cursor = Cursor.MOVE
        }
        root.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
            root.layoutX = shiftX + it.sceneX
            root.layoutY = shiftY + it.sceneY
        }
        root.addEventHandler(MouseEvent.MOUSE_RELEASED) {
            root.cursor = defaultCursor
        }

        // Scale
        root.addEventHandler(ScrollEvent.SCROLL) {
            this.clearText()
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
            this.clearText()
        }

        root.children.add(view)
        root.children.add(textLayer)
        content = container.also { it.children.add(root) }
    }

    fun reset() {
        scale = initScale
        image = INIT_IMAGE
        selectedLabelIndex = NOT_FOUND

        root.layoutX = 0.0
        root.layoutY = 0.0
        setupLayers(0)
    }

    fun setupImage(path: String) {
        try {
            image = Image(File(path).toURI().toURL().toString())
        } catch (e : IOException) {
            showException(e)
        }
    }
    fun setupLayers(count: Int) {
        labelLayers.forEach { root.children.remove(it) }
        labelLayers.clear()
        clearText()

        for (i in 0 until count) AnchorPane().also {
            it.isPickOnBounds = false
            root.children.add(it)
            labelLayers.add(it)
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

    fun placeLabel(transLabel: TransLabel) {
        val label = CLabel(
            transLabel.index,
            LABEL_RADIUS,
            colorList[transLabel.groupId]
        )

        // Bind property
        label.indexProperty.bind(transLabel.indexProperty)
        label.colorProperty.bind(colorListProperty.valueAt(transLabel.groupIdProperty))

        // Register label
        while (labels.size <= label.index) {
            labels.add(null)
        }
        labels[label.index] = label

        // Draggable
        // ScenePos -> CursorPos; LayoutPos -> CtxPos
        // nLx = Lx + (nSx - Sx); nLy = Ly + (nSy - Sy)
        // nLx = (Lx - Sx) + nSx -> shiftN + sceneN
        label.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            shiftX = label.layoutX -  it.sceneX
            shiftY = label.layoutY -it.sceneY
            label.cursor = Cursor.MOVE
        }
        label.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
            clearText()

            label.layoutX = shiftX + it.sceneX
            label.layoutY = shiftY + it.sceneY

            transLabel.x = it.x / imageWidth
            transLabel.y = it.y / imageHeight
        }
        label.addEventHandler(MouseEvent.MOUSE_RELEASED) {
            label.cursor = defaultCursor
        }

        // Cursor
        label.addEventHandler(MouseEvent.MOUSE_ENTERED) {
            label.cursor = Cursor.HAND
        }
        label.addEventHandler(MouseEvent.MOUSE_EXITED) {
            label.cursor = defaultCursor
            this.clearText()
        }

        // Selected index
        label.addEventHandler(MouseEvent.MOUSE_CLICKED) {
             selectedLabelIndex = label.index
        }

        // Event handle
        label.setOnMouseMoved {
            onLabelPointed.handle(LabelEvent(LabelEvent.LABEL_POINTED, it, label.index))
        }
        label.setOnMouseClicked {
            onLabelClicked.handle(LabelEvent(LabelEvent.LABEL_CLICKED, it, label.index))
        }

        // Layout
        AnchorPane.setLeftAnchor(label, imageWidth * transLabel.x)
        AnchorPane.setTopAnchor(label, imageHeight * transLabel.y)
        labelLayers[transLabel.groupId].children.add(label)
    }
    fun placeText(text: String, color: Color, x: Double, y: Double) {
        val gc = textLayer.graphicsContext2D
        val lineCount = text.length - text.replace("\n".toRegex(), "").length + 1
        val t = Text(text).also { it.font = DISPLAY_FONT }

        val textW = t.boundsInLocal.width
        val textH = t.boundsInLocal.height
        var textX = x + DISPLAY_SHIFT
        var textY = y + textH / lineCount

        val shapeW = textW + 2 * DISPLAY_INSET
        val shapeH = textH + 2 * DISPLAY_INSET
        var shapeX = x + (DISPLAY_SHIFT - DISPLAY_INSET)
        var shapeY = y

        if (shapeX + shapeW > imageWidth) {
            shapeX = x - shapeW
            textX = x - textW - DISPLAY_INSET
        }
        if (shapeY + shapeH > imageHeight) {
            shapeY = y - shapeH
            textY = y - textH - DISPLAY_INSET
        }

        gc.fill = Color.web(Color.WHEAT.toHex() + DISPLAY_ALPHA)
        gc.fillRect(shapeX, shapeY, shapeW, shapeH)
        gc.stroke = Color.DARKGRAY
        gc.strokeRect(shapeX, shapeY, shapeW, shapeH)
        gc.fill = color
        gc.fillText(text, textX, textY)
    }
    fun clearLabel(transLabel: TransLabel) {
        labelLayers[transLabel.groupId].children.remove(labels[transLabel.index])
        labels[transLabel.index] = null
    }
    fun clearText() {
        textLayer.graphicsContext2D.clearRect(0.0, 0.0, textLayer.width, textLayer.height)
    }

    fun addLabelLayer() {
        val pane = AnchorPane()
        pane.isPickOnBounds = false
        labelLayers.add(pane)
        root.children.add(pane)
        textLayer.toFront()
    }
    fun removeLabelLayer(groupId: Int) {
        val pane = labelLayers[groupId]
        labelLayers.remove(pane)
        root.children.remove(pane)
    }
    fun updateTextLayer() {}
    fun moveToLabel(index: Int) {}
}