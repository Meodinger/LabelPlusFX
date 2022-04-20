package ink.meodinger.lpfx.util.property

import ink.meodinger.lpfx.util.doNothing
import javafx.beans.property.*
import javafx.collections.*


/**
 * Author: Meodinger
 * Date: 2021/12/13
 * Have fun with my code!
 */

/**
 * Is a property was bound (bidirectional not included)
 */
inline val <T> Property<T>.isNotBound: Boolean get() = !isBound

/**
 * Alias for setAll
 * @see ObservableList.setAll
 */
operator fun <E> ObservableList<E>.divAssign(value: List<E>) { setAll(value) }

fun <E> ObservableMap<E, *>.observableKeys(): ObservableSet<E> {
    val set = FXCollections.observableSet(HashSet(keys))

    addListener(MapChangeListener {
        if (it.wasAdded() != it.wasRemoved()) {
            if (it.wasRemoved()) {
                set.remove(it.key)
            } else {
                set.add(it.key)
            }
        }
    })

    return FXCollections.unmodifiableObservableSet(set)
}

fun <E> ObservableSet<E>.observableSorted(sorter: (Set<E>) -> List<E>): ObservableList<E> {
    val list = FXCollections.observableArrayList(sorter(this))

    addListener(SetChangeListener { list.setAll(sorter(it.set)) })

    return FXCollections.unmodifiableObservableList(list)
}
fun <E> ObservableList<E>.observableSorted(sorter: (List<E>) -> List<E>): ObservableList<E> {
    val list = FXCollections.observableArrayList(sorter(this))

    addListener(ListChangeListener { list.setAll(sorter(it.list)) })

    return FXCollections.unmodifiableObservableList(list)
}
