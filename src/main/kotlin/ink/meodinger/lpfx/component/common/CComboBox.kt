package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.util.property.*

import javafx.beans.property.*
import javafx.beans.value.WeakChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.SingleSelectionModel
import javafx.scene.layout.HBox


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A ComboBox with back/next Button (wrapped in a HBox).
 * Provided some common wrapped properties and accessibility
 * to the inner ComboBox by field `innerBox`.
 * Note that we do not export property value because
 * **it can be anything as long as it is a valid value of type T.**
 * You can use bindings like `itemsProperty.valueAt(indexProperty)`
 */
class CComboBox<T> : HBox() {

    private val back = Button("<")
    private val next = Button(">")
    val innerBox: ComboBox<T> = ComboBox()

    private val itemsProperty: ListProperty<T> = SimpleListProperty(FXCollections.emptyObservableList())
    fun itemsProperty(): ListProperty<T> = itemsProperty
    var items: ObservableList<T> by itemsProperty

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(NOT_FOUND)
    fun indexProperty(): IntegerProperty = indexProperty
    var index: Int by indexProperty

    private val selectionModelProperty: ObjectProperty<SingleSelectionModel<T>> = SimpleObjectProperty(null)
    fun selectionModelProperty(): ReadOnlyObjectProperty<SingleSelectionModel<T>> = selectionModelProperty
    val selectionModel: SingleSelectionModel<T> by selectionModelProperty

    private val wrappedProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun wrappedProperty(): BooleanProperty = wrappedProperty
    var isWrapped: Boolean by wrappedProperty

    init {
        itemsProperty.bindBidirectional(innerBox.itemsProperty())
        selectionModelProperty.bindBidirectional(innerBox.selectionModelProperty())

        // Bind bidirectionally by listeners
        val listenerSelection = onNew<Number, Int>(indexProperty::set)
        indexProperty.addListener(onNew<Number, Int>(selectionModel::select))
        innerBox.selectionModel.selectedIndexProperty().addListener(listenerSelection)
        innerBox.selectionModelProperty().addListener(WeakChangeListener { _, o, n ->
            o?.selectedIndexProperty()?.removeListener(listenerSelection)
            n?.selectedIndexProperty()?.addListener(listenerSelection)

            index = selectionModel.selectedIndex
        })

        back.setOnMouseClicked { back() }
        next.setOnMouseClicked { next() }

        // HGrow not work for resizeable node, use bind instead
        innerBox.prefWidthProperty().bind(prefWidthProperty() - back.widthProperty() - next.widthProperty())
        back.alignment = Pos.CENTER
        next.alignment = Pos.CENTER

        // Set a proper default width
        prefWidth = 160.0

        children.addAll(innerBox, back, next)
    }

    fun back() {
        val size = items.size
        var newIndex = index - 1

        if (isWrapped) if (newIndex < 0) newIndex += size
        if (newIndex >= 0) selectionModel.select(newIndex)
    }
    fun next() {
        val size = items.size
        var newIndex = index + 1

        if (isWrapped) if (newIndex >= size) newIndex -= size
        if (newIndex < size) selectionModel.select(newIndex)
    }

    fun select(index: Int) {
        selectionModel.select(index)
    }
    fun select(item: T) {
        if (items.contains(item)) selectionModel.select(item)
    }

}
