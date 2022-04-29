package ink.meodinger.lpfx.util

import javafx.application.Application
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.regex.Pattern
import kotlin.reflect.KProperty


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Version
 */
data class Version(val a: Int, val b: Int, val c: Int): Comparable<Version> {

    companion object {
        val V0 = Version(0, 0, 0)

        private val pattern = Pattern.compile("v[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{1,2}", Pattern.CASE_INSENSITIVE)
        private fun check(i : Int): Int {
            if (i in 0..99) return i
            throw IllegalArgumentException("Version number must in 0..99, got $i")
        }

        /**
         * Return a Version by String like "v0.1.99", case-insensitive
         * @param version Start with 'v', with three number parts that split by '.' and value between 0 and 99
         * @return Version, V0 if format invalid
         */
        fun of(version: String): Version {
            if (!pattern.matcher(version).matches()) return V0
            val l = version.split(".")

            return Version(l[0].substring(1).toInt(), l[1].toInt(), l[2].toInt())
        }
    }

    init {
        check(a)
        check(b)
        check(c)
    }

    override fun toString(): String = "v$a.$b.$c"

    override operator fun compareTo(other: Version): Int {
        return (this.a - other.a) * 10000 + (this.b - other.b) * 100 + (this.c - other.c)
    }

}

/**
 * Hooked Application
 */
abstract class HookedApplication : Application() {

    private var shuttingDown: Boolean = false
    private val shutdownHooks = LinkedHashMap<String, () -> Unit>()

    /**
     * Add a shutdown hook
     * Should use the resolve function as callback
     */
    fun addShutdownHook(key: String, onShutdown: () -> Unit) {
        if (shuttingDown) return
        shutdownHooks[key] = onShutdown
    }

    /**
     * Remove a shutdown hook
     */
    fun removeShutdownHook(key: String) {
        if (shuttingDown) return
        shutdownHooks.remove(key)
    }

    /**
     * Clear all shutdown hooks
     */
    fun clearShutdownHooks() {
        if (shuttingDown) return
        shutdownHooks.clear()
    }

    /**
     * This method should be called in stop().
     *
     * You MUST run this before stop or hooks will not be executed.
     * And you should actually shut the app down in the callback function.
     */
    protected fun runHooks(callback: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        if (shuttingDown) return

        shuttingDown = true

        if (shutdownHooks.isEmpty()) {
            callback()
        } else {
            shutdownHooks.values.forEach {
                try {
                    it()
                } catch (e: Throwable) {
                    onError(e)
                }
            }
            callback()
        }
    }

    abstract fun exit()

}

/**
 * All streams want to be auto-closed should run `autoClose()`
 */
inline fun using(crossinline block: ResourceManager.() -> Unit): Catcher {
    val manager = ResourceManager()
    try {
        // Use manager's auto close
        manager.use(block)
    } catch (t: Throwable) {
        // Exception thrown in try block
        manager.throwable = t
    }

    return Catcher(manager.throwable)
}
class ResourceManager : AutoCloseable {

    private val resourceQueue = ConcurrentLinkedDeque<AutoCloseable>()
    var throwable: Throwable? = null

    fun <T: AutoCloseable> T.autoClose(): T {
        // The last opened Steam is the first closed
        resourceQueue.addFirst(this)
        return this
    }

    override fun close() {
        for (closeable in resourceQueue) {
            try {
                closeable.close()
            } catch (t: Throwable) {
                // Exception thrown when close (also in try block)
                if (this.throwable == null) {
                    this.throwable = t
                } else {
                    this.throwable!!.addSuppressed(t)
                }
            }
        }
    }
}
class Catcher(var throwable: Throwable? = null) {

    var thrown: Throwable? = null

    inline infix fun <reified T : Throwable> catch(block: (T) -> Unit): Catcher {
        if (this.throwable is T) {
            try {
                block(this.throwable as T)
            } catch (thrown: Throwable) {
                // Exception thrown in catch block
                this.thrown = thrown
            } finally {
                // It's been caught, so set it to null
                this.throwable = null
            }
        }
        return this
    }

    inline infix fun finally(block: () -> Unit) {
        try {
            block()
        } catch (thrown: Throwable) {
            if (this.throwable == null) {
                // we've caught the exception, or none was thrown
                if (this.thrown == null) {
                    // No exception was thrown in the catch blocks
                    throw thrown
                } else {
                    // An exception was thrown in the catch block
                    throw this.thrown!!.apply { addSuppressed(thrown) }
                }
            } else {
                // We never caught the exception
                // So this.thrown is also null
                throw this.throwable!!.apply { addSuppressed(thrown) }
            }
        }

        // At this point the `finally` block did not throw an exception
        // We need to see if there are still any exceptions left to throw
        this.throwable?.let {
            thrown?.apply { it.addSuppressed(this) }
            throw it
        } ?: this.thrown?.let {
            throw it
        }
    }
}

/**
 * Placeholder
 */
fun doNothing() {}

/**
 * Can assign only once
 */
class AssignOnce<T> {

    private var _backing: T? = null

    operator fun getValue(thisRef: Any, property: KProperty<*>): T = _backing!!
    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        synchronized(this) {
            if (_backing == null) _backing = value
            else throw IllegalStateException("Value already set")
        }
    }

}
fun <T> once(): AssignOnce<T> = AssignOnce()
