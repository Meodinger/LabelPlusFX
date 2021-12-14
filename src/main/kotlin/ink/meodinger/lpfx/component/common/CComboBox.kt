package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.isNotBound
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.beans.property.*
import javafx.collections.ObservableList
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.SelectionModel
import javafx.scene.layout.HBox
import javafx.scene.text.TextAlignment


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A ComboBox with back/next Button (in HBox)
 */
class CComboBox<T> : HBox() {

    private val comboBox = ComboBox<T>()
    private val back = Button("<")
    private val next = Button(">")

    val selectionModel: SelectionModel<T> get() = comboBox.selectionModel
    val size: Int get() = comboBox.items.size

    private val itemsProperty: ObjectProperty<ObservableList<T>> = comboBox.itemsProperty()
    fun itemsProperty(): ObjectProperty<ObservableList<T>> = itemsProperty
    var items: ObservableList<T> by itemsProperty

    private val valueProperty: ObjectProperty<T> = comboBox.valueProperty()
    fun valueProperty(): ObjectProperty<T> = valueProperty
    var value: T by valueProperty

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(comboBox.selectionModel.selectedIndex)
    fun indexProperty(): IntegerProperty = indexProperty
    var index: Int by indexProperty

    private val isWrappedProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun wrappedProperty(): BooleanProperty = isWrappedProperty
    var isWrapped: Boolean by isWrappedProperty

    init {
        indexProperty.addListener(onNew<Number, Int> { comboBox.selectionModel.select(it) })
        comboBox.selectionModel.selectedIndexProperty().addListener(onNew<Number, Int> { indexProperty.set(it) })

        back.setOnMouseClicked { back() }
        next.setOnMouseClicked { next() }

        comboBox.prefWidth = 150.0
        back.textAlignment = TextAlignment.CENTER
        next.textAlignment = TextAlignment.CENTER

        children.addAll(comboBox, back, next)
    }

    fun reset() {
        if (itemsProperty.isNotBound) comboBox.items.clear()
        if (valueProperty.isNotBound) comboBox.selectionModel.clearSelection()
    }

    fun back() {
        val size = comboBox.items.size
        var newIndex = index - 1

        if (isWrapped) if (newIndex < 0) newIndex += size
        if (newIndex >= 0) comboBox.value = comboBox.items[newIndex]
    }
    fun next() {
        val size = comboBox.items.size
        var newIndex = index + 1

        if (isWrapped) if (newIndex > size - 1) newIndex -= size
        if (newIndex <= size - 1) comboBox.value = comboBox.items[newIndex]
    }

    fun select(index: Int) {
        if (comboBox.items.size == 0 && index == 0) return

        if (index in 0 until comboBox.items.size) comboBox.selectionModel.select(index)
        else throw IllegalArgumentException(String.format(I18N["exception.combo_box.item_index_invalid.i"], index))
    }
    fun select(item: T) {
        if (comboBox.items.contains(item)) comboBox.selectionModel.select(item)
        else throw IllegalArgumentException(String.format(I18N["exception.combo_box.no_such_item.s"], item.toString()))
    }

}
