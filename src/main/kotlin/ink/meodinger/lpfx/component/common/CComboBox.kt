package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.property.getValue
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

    private val valueProperty: ObjectProperty<T> = comboBox.valueProperty()
    fun valueProperty(): ObjectProperty<T> = valueProperty
    val value: T by valueProperty

    private val indexProperty: ReadOnlyIntegerProperty = comboBox.selectionModel.selectedIndexProperty()
    fun indexProperty(): ReadOnlyIntegerProperty = indexProperty
    val index: Int by indexProperty

    private val isWrappedProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun wrappedProperty(): BooleanProperty = isWrappedProperty
    var isWrapped: Boolean by isWrappedProperty

    private val itemsProperty: ObjectProperty<ObservableList<T>> = comboBox.itemsProperty()
    fun itemsProperty(): ObjectProperty<ObservableList<T>> = itemsProperty
    val items: ObservableList<T> by itemsProperty

    val selectionModel: SelectionModel<T> get() = comboBox.selectionModel

    init {
        back.setOnMouseClicked { back() }
        next.setOnMouseClicked { next() }

        comboBox.prefWidth = 150.0
        back.textAlignment = TextAlignment.CENTER
        next.textAlignment = TextAlignment.CENTER

        children.addAll(comboBox, back, next)
    }

    fun reset() {
        comboBox.items.clear()
        comboBox.selectionModel.clearSelection()
    }

    fun setList(list: List<T>) {
        items.setAll(list)

        if (list.isNotEmpty()) comboBox.selectionModel.select(0)
    }

    fun createItem(item: T) {
        items.add(item)
    }
    fun removeItem(item: T) {
        items.remove(item)
    }

    fun back() {
        val size = comboBox.items.size
        var newIndex = index - 1

        if (isWrapped) {
            if (newIndex < 0) newIndex += size
        }
        if (newIndex >= 0) {
            comboBox.value = comboBox.items[newIndex]
        }
    }
    fun next() {
        val size = comboBox.items.size
        var newIndex = index + 1

        if (isWrapped) {
            if (newIndex > size - 1) newIndex -= size
        }
        if (newIndex <= size - 1) {
            comboBox.value = comboBox.items[newIndex]
        }
    }

    fun select(index: Int) {
        comboBox.selectionModel.clearSelection()
        if (comboBox.items.size == 0 && index == 0) return
        if (index in 0 until comboBox.items.size) comboBox.selectionModel.select(index)
        else throw IllegalArgumentException(String.format(I18N["exception.combo_box.item_index_invalid.i"], index))
    }
    fun select(item: T) {
        comboBox.selectionModel.clearSelection()
        if (comboBox.items.contains(item)) comboBox.selectionModel.select(item)
        else throw IllegalArgumentException(String.format(I18N["exception.combo_box.no_such_item.s"], item.toString()))
    }

}