package ink.meodinger.lpfx.util

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

    init {
        Thread {
            try {
                block(this::resolve, this::reject)
            } catch (e: Throwable) {
                reject(e)
            }
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