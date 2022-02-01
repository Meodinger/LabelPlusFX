package ink.meodinger.lpfx.util

import javafx.application.Application
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.regex.Pattern


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
    private val shutdownHooks = LinkedHashMap<String, (() -> Unit) -> Unit>()

    /**
     * Add a shutdown hook
     * Should use the resolve function as callback
     */
    fun addShutdownHook(key: String, onShutdown: (() -> Unit) -> Unit) {
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
            val hooks = shutdownHooks.values.toList()
            Promise.all(List(shutdownHooks.size) {
                Promise<Unit> { resolve, _ -> hooks[it] { resolve(Unit) } }
            }) catch { e: Throwable ->
                e.also(onError)
            } finally {
                callback()
            }
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

typealias Resolve<T>       = (T) -> Unit // T    : Any?
typealias OnResolved<T, R> = (T) -> R    // T, R : Any?
typealias Reject<U>        = (U) -> Unit // U    : Throwable
typealias OnRejected<U, V> = (U) -> V    // U, V : Throwable

/**
 * Promise
 */
class Promise<T>(private val block: (Resolve<T>, Reject<Throwable>) -> Unit) {

    companion object {
        fun <T> resolve(promise: Promise<T>): Promise<T> = promise
        fun <T> resolve(then: (Resolve<T>) -> Unit) = Promise<T> { _resolve, _ -> then(_resolve) }
        fun <T> resolve(value: T): Promise<T> = Promise { _resolve, _ -> _resolve(value) }
        fun resolve() : Promise<Unit> = Promise { _resolve, _ -> _resolve(Unit) }

        fun <U : Throwable> reject(catch: (Reject<U>) -> Unit) = Promise<U> { _, _reject -> catch(_reject) }
        fun <U : Throwable> reject(throwable: U) = Promise<U> { _, _reject -> _reject(throwable) }

        fun <T> all(promises: List<Promise<T>>) = Promise<List<T>> { _resolve, _reject ->
            if (promises.isEmpty()) {
                _resolve(emptyList())
                return@Promise
            }

            val count = promises.size
            val rets = MutableList<T?>(count) { null }
            var resolved = 0

            @Suppress("RedundantLambdaArrow", "UNCHECKED_CAST")
            promises.forEachIndexed { index, promise ->
                resolve(promise).then { result ->
                    resolved++
                    rets[index] = result
                    if (resolved == count) _resolve(rets as List<T>)
                } catch { it: Throwable ->
                    _reject(it)
                    it
                }
            }
        }

        fun <T> race(promises: List<Promise<T>>) = Promise<List<T>> { _resolve, _reject ->
            if (promises.isEmpty()) {
                _resolve(emptyList())
                return@Promise
            }

            val count = promises.size
            val rets = ArrayList<T>()
            var resolved = 0

            @Suppress("RedundantLambdaArrow")
            promises.forEach { promise ->
                resolve(promise).then { result ->
                    resolved++
                    rets.add(result)
                    if (resolved == count) _resolve(rets)
                } catch { it: Throwable ->
                    _reject(it)
                    it
                }
            }

        }
    }

    enum class State { RUNNING, RESOLVED, REJECTED }

    private val resolves: MutableList<Pair<OnResolved<T, Any?>?, Resolve<Any?>>> = ArrayList()
    private val rejects: MutableList<Pair<OnRejected<Throwable, Throwable>?, Reject<Throwable>>> = ArrayList()

    private var state: State = State.RUNNING
    private var value: T? = null
    private var exception: Throwable? = null

    val thread: Thread

    init {
        Thread {
            try {
                block(this::resolve, this::reject)
            } catch (e: Throwable) {
                reject(e)
            }
        }.also {
            this.thread = it
        }.start()
    }

    private fun resolve(value: T) {
        if (this.state != State.RUNNING) return

        @Suppress("UNCHECKED_CAST")
        if (value is Promise<*>) {
            value then {
                this.resolve(it as T)
            } catch { e: Throwable ->
                this.reject(e)
                e
            }
            return
        }

        this.value = value
        this.state = State.RESOLVED

        for (i in resolves.indices) {
            try {
                this.handle(
                    resolves[i].first, resolves[i].second,
                    rejects[i].first, rejects[i].second
                )
            } catch (e: Throwable) {
                reject(e)
                return
            }
        }
    }

    private fun reject(exception: Throwable) {
        if (this.state != State.RUNNING) return

        this.state = State.REJECTED
        this.exception = exception

        for (i in resolves.indices) {
            this.handle(
                resolves[i].first, resolves[i].second,
                rejects[i].first, rejects[i].second
            )
        }
    }

    private fun handle(
        onResolved: OnResolved<T, Any?>?,
        newResolve: Resolve<Any?>,
        onRejected: OnRejected<Throwable, Throwable>?,
        newReject: Reject<Throwable>
    ) {
        when (this.state) {
            State.RUNNING -> {
                this.resolves.add(onResolved to newResolve)
                this.rejects.add(onRejected to newReject)
            }
            State.REJECTED -> {
                if (onRejected == null) newReject(this.exception!!)
                else newReject(onRejected(this.exception!!))
            }
            State.RESOLVED -> {
                if (onResolved == null) newResolve(this.value!!)
                else newResolve(onResolved(this.value!!))
            }
        }
    }

    /**
     * The param function will be invoked when the Promise state changes to `RESOLVED` or `REJECTED`.
     * @param onResolved Accept the resolved value and return a `Promise<R>`
     * @param onRejected Accept the rejected exception U and return a `Promise<V : Throwable>` for catch
     * @return `Promise<R>`
     */
    fun <R, U : Throwable, V : Throwable> then(onResolved: OnResolved<T, R>, onRejected: OnRejected<U, V>): Promise<R> {
        return Promise { resolve, reject ->
            @Suppress("UNCHECKED_CAST")
            this.handle(onResolved, resolve as Resolve<Any?>, onRejected as OnRejected<Throwable, Throwable>, reject)
        }
    }

    /**
     * NOTE: Didn't use overload because java will eraser the type info while compiling.
     *
     * The param function will be invoked when the Promise state changes to `RESOLVED`.
     * @param onResolved Accept the resolved value and return a Promise whose resolved value
     *                   or rejected exception will be passed to the returned new `Promise<R>`
     * @return `Promise<R>`
     */
    infix fun <R> thenPromise(onResolved: OnResolved<T, Promise<R>>): Promise<R> {
        return Promise { resolve, reject ->
            @Suppress("UNCHECKED_CAST")
            this.handle(onResolved, resolve as Resolve<Any?>, null, reject)
        }
    }

    /**
     * The param function will be invoked when the Promise state changes to `RESOLVED`.
     * @param onResolved Accept the resolved value and return a Promise of returned value
     * @return `Promise<R>`
     */
    infix fun <R> then(onResolved: OnResolved<T, R>): Promise<R> {
        return Promise { resolve, reject ->
            @Suppress("UNCHECKED_CAST")
            this.handle(onResolved, resolve as Resolve<Any?>, null, reject)
        }
    }

    /**
     * The param function will be invoked when the Promise state changes to `REJECTED`.
     * @param onRejected Accept the rejected exception and return a Promise of exception (new or current if you like)
     * @return `Promise<T>`
     */
    infix fun <U : Throwable, V: Throwable> catch(onRejected: OnRejected<U, V>): Promise<T> {
        return Promise { resolve, reject ->
            @Suppress("UNCHECKED_CAST")
            this.handle(null, resolve as Resolve<Any?>, onRejected as OnRejected<Throwable, Throwable>, reject)
        }
    }

    /**
     * The param function will be invoked when the Promise state is not `RUNNING`
     * @param onDone What to do when the Promise run.
     */
    infix fun finally(onDone: () -> Unit) {
        Promise<T> { resolve, reject ->
            @Suppress("UNCHECKED_CAST")
            this.handle({ onDone() }, resolve as Resolve<Any?>, { e: Throwable -> onDone(); e }, reject)
        }
    }
}

/**
 * Refreshable Lazy
 */
class ReLazy<T>(private val initializer: () -> T) : Lazy<T> {

    private var _initialized: Boolean = false
    private var _value: T? = null

    override fun isInitialized(): Boolean = _initialized

    override val value: T
        get() = synchronized(this) get@ {
            if (!_initialized) {
                _value = initializer()
                _initialized = true
            }

            return@get _value ?: throw IllegalStateException("Should not bu null")
        }

    fun refresh() {
        _initialized = false
    }
}
