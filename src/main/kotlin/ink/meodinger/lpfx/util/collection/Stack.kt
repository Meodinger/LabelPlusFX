package ink.meodinger.lpfx.util.collection

/**
 * Author: Meodinger
 * Date: 2022/3/11
 * Have fun with my code!
 */

/**
 * Alternative Stack
 */
interface Stack<E> {

    /**
     * Indicates the size of the stack
     */
    val size: Int

    /**
     * Push an element into the stack
     */
    fun push(element: E)

    /**
     * Pop the element at the top of the stack
     */
    fun pop(): E

    /**
     * Peek the element at the top of the stack
     */
    fun peek(): E

    /**
     * Empty the stack
     */
    fun empty()

    /**
     * Returns `true` if the stack is empty (contains no elements), `false` otherwise.
     */
    fun isEmpty(): Boolean

}

/**
 * A Stack implemented with an array as internal storage.
 * Its read method `pop()` and `peek()` is faster than
 * `kotlin.collections.ArrayDeque`, but its write method
 * `push()` will be very slow if the need-to-grew size
 * is too large (like ArrayList).
 */
class ArrayStack<E>(initialCapacity: Int = 16) : Stack<E> {

    companion object {
        /**
         * A soft maximum array length imposed by stack growth computations.
         * @see jdk.internal.util.ArraysSupport.SOFT_MAX_ARRAY_LENGTH
         */
        private const val SOFT_MAX_ARRAY_LENGTH = Int.MAX_VALUE - 8
    }

    // We could not use Array<E?> here because we could not instance generic
    private var _array: Array<Any?> = arrayOfNulls(initialCapacity)
    private var _pointer: Int = 0

    @Suppress("UNCHECKED_CAST")
    private fun elementData(index: Int): E = _array[index] as E

    private fun grow() {
        val increment = (_pointer shr 1).coerceAtMost(SOFT_MAX_ARRAY_LENGTH - _pointer).coerceAtLeast(1)
        val newSize = _pointer + increment
        if (newSize < 0) throw OutOfMemoryError("Required array length $_pointer + $increment is too large")

        _array = arrayOfNulls<Any>(newSize).also {
            System.arraycopy(_array, 0, it, 0, _pointer)
        }
    }

    override val size: Int get() = _pointer

    override fun push(element: E) {
        if (_pointer == _array.size) grow()
        _array[_pointer++] = element
    }

    override fun pop(): E {
        if (_pointer == 0) throw IllegalStateException("Stack has no elements")
        return elementData(--_pointer)
    }

    override fun peek(): E {
        if (_pointer == 0) throw IllegalStateException("Stack has no elements")
        return elementData(_pointer - 1)
    }

    /**
     * Empty the ArrayStack. Note that this **will not**
     * earease the date that the backing Array holds.
     * It just sets the head pointer to 0.
     * @see Stack.empty
     */
    override fun empty() {
        _pointer = 0
    }

    override fun isEmpty(): Boolean {
        return _pointer == 0
    }

}

/**
 * A Stack implemented as a wrapper of ArrayDeque
 */
class DequeStack<E>: Stack<E> {

    private val _deque = ArrayDeque<E>()

    override val size: Int get() = _deque.size

    override fun peek(): E {
        return _deque.first()
    }
    override fun pop(): E {
        return _deque.removeFirst()
    }
    override fun push(element: E) {
        _deque.addFirst(element)
    }

    override fun empty() {
        _deque.clear()
    }

    override fun isEmpty(): Boolean {
        return _deque.isEmpty()
    }

}
