package ink.meodinger.lpfx.util.property

import javafx.collections.*


/**
 * Author: Meodinger
 * Date: 2021/12/13
 * Have fun with my code!
 */


/**
 * Return a ObservableSet that reflect the keys and their changes of the ObservableMap
 * @return An unmodifiable ObservableSet
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

    return FXCollections.unmodifiableObservableSet(set)
}

/**
 * Return a ObservableList that reflect the sorted elements and their changes of the ObservableSet
 * @return An unmodifiable ObservableList
 */
fun <E> ObservableSet<E>.observableSorted(sorter: (Set<E>) -> List<E>): ObservableList<E> {
    val list = FXCollections.observableArrayList(sorter(this))

    addListener(SetChangeListener { list.setAll(sorter(it.set)) })

    return FXCollections.unmodifiableObservableList(list)
}
/**
 * Return a ObservableList that reflect the sorted elements and their changes of the ObservableList
 * @return An unmodifiable ObservableList
 */
fun <E> ObservableList<E>.observableSorted(sorter: (List<E>) -> List<E>): ObservableList<E> {
    val list = FXCollections.observableArrayList(sorter(this))

    addListener(ListChangeListener { list.setAll(sorter(it.list)) })

    return FXCollections.unmodifiableObservableList(list)
}
