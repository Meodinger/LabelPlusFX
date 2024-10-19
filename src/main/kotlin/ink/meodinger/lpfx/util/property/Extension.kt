package ink.meodinger.lpfx.util.property

import javafx.beans.binding.Bindings
import javafx.beans.binding.IntegerBinding
import javafx.collections.*


/**
 * Author: Meodinger
 * Date: 2021/12/13
 * Have fun with my code!
 */


/**
 * Return a ObservableSet that reflect the keys and their changes of the ObservableMap.
 * @return An ObservableSet that **MUTABLE** but you should better **NOT** change it.
 */
fun <K> ObservableMap<K, *>.observableKeySet(): ObservableSet<K> {
    val set = FXCollections.observableSet(HashSet(keys))

    addListener(MapChangeListener {
        // Note that if change was added and removed, it means
        // the key was unchanged and a value was just replaced.
        // That shouldn't affect the key set, so we do nothing.
        if (it.wasAdded() != it.wasRemoved()) {
            if (it.wasAdded()) {
                set.add(it.key)
            } else {
                set.remove(it.key)
            }
        }
    })

    // It's a compromise with the WeakListener attached to UnmodifiableSet which often offline.
    return set
}

/**
 * Return a ObservableList that reflect the sorted elements and their changes of the ObservableSet
 * @return An ObservableList that **MUTABLE** but you should better **NOT** change it.
 */
fun <E> ObservableSet<E>.observableSorted(sorter: (Set<E>) -> List<E>): ObservableList<E> {
    val list = FXCollections.observableArrayList(sorter(this))

    addListener(SetChangeListener {
        // Sort the set whenever it changes
        list.setAll(sorter(it.set))
    })

    // It's a compromise with the WeakListener attached to UnmodifiableList which often offline.
    return list
}
/**
 * Return a ObservableList that reflect the sorted elements and their changes of the ObservableList
 * @return An ObservableList that **MUTABLE** but you should better **NOT** change it.
 */
fun <E> ObservableList<E>.observableSorted(sorter: (List<E>) -> List<E>): ObservableList<E> {
    val list = FXCollections.observableArrayList(sorter(this))

    addListener(ListChangeListener {
        // Sort the list whenever it changes
        list.setAll(sorter(it.list))
    })

    // It's a compromise with the WeakListener attached to UnmodifiableList which often offline.
    return list
}

/**
 * Return a IntegerBinding that represents the index in the list of the given element
 */
fun <E> ObservableList<E>.observableIndexOf(element: E): IntegerBinding {
    return Bindings.createIntegerBinding({ indexOf(element) }, this)
}
