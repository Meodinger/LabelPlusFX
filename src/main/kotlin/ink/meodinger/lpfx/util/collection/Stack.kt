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

class ArrayStack<E> : Stack<E> {

    companion object {
        private const val DEFAULT_CAPACITY = 16
    }

    private var _array: Array<Any?> = arrayOfNulls(DEFAULT_CAPACITY)
    private var _pointer: Int = 0

    @Suppress("UNCHECKED_CAST")
    private fun elementData(index: Int): E = _array[index] as E

    override val size: Int get() = _pointer

    override fun push(element: E) {
        if (_pointer == _array.size) {
            val increment = _pointer shr 1
            val newSize = _pointer + increment
            if (newSize < 0) throw OutOfMemoryError("Required array length $_pointer + $increment is too large")

            _array = _array.copyOf(newSize)
        }
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

    override fun empty() {
        _pointer = 0
    }

    override fun isEmpty(): Boolean {
        return _pointer == 0
    }

}
