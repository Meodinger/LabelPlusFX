package info.meodinger.lpfx.util

import java.util.concurrent.ConcurrentLinkedDeque


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */

/**
 * All streams want to be auto-closed should run `autoClose()`
 */
inline fun using(crossinline block: ResourceManager.() -> Unit): Catcher {
    val manager = ResourceManager()
    try {
        // use manager's auto close
        manager.use(block)
    } catch (t: Throwable) {
        manager.throwable = t
    }
    return manager.getCatcher()
}
class ResourceManager : AutoCloseable {

    private val resourceQueue = ConcurrentLinkedDeque<AutoCloseable>()
    var throwable: Throwable? = null

    fun <T: AutoCloseable> T.autoClose(): T {
        resourceQueue.addFirst(this)
        return this
    }

    override fun close() {
        for (closeable in resourceQueue) {
            try {
                closeable.close()
            } catch (t: Throwable) {
                if (this.throwable == null) {
                    this.throwable = t
                } else {
                    this.throwable!!.addSuppressed(t)
                }
            }
        }
    }

    fun getCatcher(): Catcher {
        return Catcher(this)
    }
}
class Catcher(manager: ResourceManager) {
    var throwable: Throwable? = null
    var thrown: Throwable? = null

    init {
        throwable = manager.throwable
    }

    inline infix fun <reified T : Throwable> catch(block: (T) -> Unit): Catcher {
        if (throwable is T) {
            try {
                block(throwable as T)
            } catch (thrown: Throwable) {
                this.thrown = thrown
            } finally {
                // It's been caught, so set it to null
                throwable = null
            }
        }
        return this
    }

    inline infix fun finally(block: () -> Unit) {
        try {
            block()
        } catch (thrown: Throwable) {
            if (throwable == null) {
                // we've caught the exception, or none was thrown
                if (this.thrown == null) {
                    // No exception was thrown in the catch blocks
                    throw thrown
                } else {
                    // An exception was thrown in the catch block
                    this.thrown!!.let {
                        it.addSuppressed(thrown)
                        throw it
                    }
                }
            } else {
                // We never caught the exception
                // So this.thrown is also null
                throwable!!.let {
                    it.addSuppressed(thrown)
                    throw it
                }
            }
        }

        // At this point the `finally` block did not throw an exception
        // We need to see if there are still any exceptions left to throw
        throwable?.let { t ->
            thrown?.let { t.addSuppressed(it) }
            throw t
        }
        thrown?.let { throw it }
    }
}

/**
 * Placeholder for when expression
 */
fun doNothing() {}