package ink.meodinger.lpfx.util

import javafx.application.Application
import java.util.regex.Pattern
import kotlin.reflect.KProperty


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Version
 * @param a Main version number
 * @param b Vice version number
 * @param c Fix version number
 */
data class Version(val a: Int, val b: Int, val c: Int): Comparable<Version> {

    companion object {
        /**
         * Version of v0.0.0
         */
        val V0: Version = Version(0, 0, 0)

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
        /**
         * Construct a Version from a String in the format "vX.Y.Z"
         */
        operator fun invoke(version: String): Version {
            val trimmedVersion = version.trim()
            if (!trimmedVersion.startsWith("v")) return V0
            val parts = trimmedVersion.substring(1).split(".")

            if (parts.size != 3) return V0 // 确保有三部分
            val (major, minor, patch) = parts.map { it.toIntOrNull() } // 转换为整数

            return if (major != null && minor != null && patch != null) {
                Version(major, minor, patch)
            } else {
                V0 // 如果转换失败，返回 V0
            }
        }
    }



    init {
        check(a)
        check(b)
        check(c)
    }

    override fun toString(): String = "v$a.$b.$c"

    override operator fun compareTo(other: Version): Int {
        return this - other
    }

     operator fun minus(other: Version): Int {
        return (this.a - other.a) * 10000 + (this.b - other.b) * 100 + (this.c - other.c)
    }


}

/**
 * Hooked Application. Should override exit to handle exit
 */
abstract class HookedApplication : Application() {

    private val shutdownHooks = LinkedHashMap<String, () -> Unit>()

    /**
     * Add a shutdown hook
     * Should use the resolve function as callback
     */
    fun addShutdownHook(key: String, onShutdown: () -> Unit) {
        shutdownHooks[key] = onShutdown
    }

    /**
     * Remove a shutdown hook
     */
    fun removeShutdownHook(key: String) {
        shutdownHooks.remove(key)
    }

    /**
     * Clear all shutdown hooks
     */
    fun clearShutdownHooks() {
        shutdownHooks.clear()
    }

    /**
     *
     * You MUST run this before or within Application::stop, or hooks will not be executed.
     */
    protected fun runHooks(onError: (Throwable) -> Unit) {
        synchronized(this) {
            shutdownHooks.values.forEach {
                try {
                    it()
                } catch (e: Throwable) {
                    onError(e)
                }
            }
        }
    }

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
/**
 * Manager for AutoCloseable
 */
class ResourceManager : AutoCloseable {

    private val resourceStack = ArrayDeque<AutoCloseable>()

    /**
     * Caught Throwable when running block
     */
    var throwable: Throwable? = null

    /**
     * Register AutoCloseable to auto-close quene
     */
    fun <T: AutoCloseable> T.autoClose(): T {
        // The last opened Steam is the first closed
        resourceStack.addFirst(this)
        return this
    }

    /**
     * Close all AutoCloseables
     */
    override fun close() {
        while (!resourceStack.isEmpty()) {
            val closeable = resourceStack.removeFirst()
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
/**
 * Catcher for ResourceManager
 * @param caught Caught Throwable when running
 */
class Catcher(caught: Throwable? = null) {

    /**
     * Caught Throwable former thrown
     */
    var throwable: Throwable? = caught

    /**
     * Handle thrown in catch block
     */
    var thrown: Throwable? = null

    /**
     * Catch a Throwable, if throw another Throwable during catching, pending it
     */
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

    /**
     * Handle un-caught Throwables and others
     */
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

    /**
     * Get the backing value, we assume user is sure that this field has been set before try getting
     */
    operator fun getValue(thisRef: Any, property: KProperty<*>): T = _backing!!

    /**
     * Set the backing value, make sure the value only will be set once
     */
    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        synchronized(this) {
            if (_backing != null) throw IllegalStateException("Value already set")
            _backing = value
        }
    }

}

/**
 * @see AssignOnce
 */
fun <T> once(): AssignOnce<T> = AssignOnce()
