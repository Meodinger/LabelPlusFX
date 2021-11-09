@file:Suppress("UNUSED_VARIABLE")

package ink.meodinger.lpfx.util

import kotlin.IllegalArgumentException
import kotlin.RuntimeException

/**
 * Author: Meodinger
 * Date: 2021/11/9
 * Have fun with my code!
 */

const val WAIT = 500L

fun main() {
    resolvePromiseStaticTest()
    Thread.sleep(WAIT)
    resolvePromiseStaticTest()
    Thread.sleep(WAIT)
    resolveThenableStaticTest()
    Thread.sleep(WAIT)
    resolveValueStaticTest()
    Thread.sleep(WAIT)
    resolveStaticTest()
    Thread.sleep(WAIT)
    rejectNewStaticTest()
    Thread.sleep(WAIT)
    rejectStaticTest()
    Thread.sleep(WAIT)
    allTest()
    Thread.sleep(WAIT)
    raceTest()
    Thread.sleep(WAIT)
    thenTest()
    Thread.sleep(WAIT)
    catchTest()
    Thread.sleep(WAIT)
    finallyTest()
    Thread.sleep(WAIT)
}

// ----- static tests ----- //

fun resolvePromiseStaticTest() {
    val promise = Promise<Int> { re, _ -> re(1) }

    val p0 = Promise.resolve(promise)
    val p1 = p0 then {
        assert(1 == it)
        println("Promise.resolve(Promise<T>): ok")
    }
}

fun resolveThenableStaticTest() {
    val then: (Resolve<Int>) -> Unit = { resolve -> resolve(2) }

    val p0 = Promise.resolve(then)
    val p1 = p0 then {
        assert(2 == it)
        println("Promise.resolve((Resolve<T>) -> Unit): ok")
    }
}

fun resolveValueStaticTest() {
    val value = 3

    val p0 = Promise.resolve(value)
    val p1 = p0 then {
        assert(3 == it)
        println("Promise.resolve(T): ok")
    }
}

fun resolveStaticTest() {
    val p0 = Promise.resolve()
    val p1 = p0 then {
        assert(Unit == it)
        println("Promise.resolve(): ok")
    }
}

fun rejectStaticTest() {
    val throwable = RuntimeException("1")

    val p0 = Promise.reject(throwable)
    val p1 = p0 catch { it: RuntimeException ->
        assert(throwable == it)
        println("Promise.reject(Throwable): ok")
        it
    }
}

fun rejectNewStaticTest() {
    val throwable = IllegalArgumentException("1")
    val catch: (Reject<IllegalArgumentException>) -> Unit = { it(throwable) }

    val p0 = Promise.reject(catch)
    p0 catch { it: IllegalArgumentException ->
        assert(throwable == it)
        println("Promise.reject((Reject<U>) -> Unit): ok")
        it
    }
}

fun allTest() {
    val size = 3
    val promises = List(size) { Promise<Int> { re, _ -> re(it) } }

    Promise.all(promises) then {
        for (i in it.indices) assert(i == it[i])
        println("Promise.all(List<Promise<T>>): ok")
    }
}

fun raceTest() {
    val size = 3
    val promises = List(size) { Promise<Int> { re, _ -> Thread.sleep(200L - it * 100L); re(it) } }

    Promise.all(promises) then {
        for (i in it.indices) assert(size - i == it[i])
        println("Promise.race(List<Promise<T>>): ok")
    }
}

// ----- instance tests ----- //

fun thenTest() {
    println("Then tests:")
    val p0 = Promise<Int> { re, _ -> re(1) }
    val p1 = p0 then {
        assert(1 == it)
        println("--then $it")
        it + 1
    }
    val p2 = p1 catch { it: Throwable ->
        assert(false)
        println("--catch $it")
        it
    }
    val p3 = p2 then {
        assert(2 == it)
        println("--then $it")
        it + 1
    }
    val p3b = p2 then {
        assert(2 == it)
        println("--then another $it")
    }
    val p4 = p3 thenPromise ({
        assert(3 == it)
        println("--then promise $it")
        Promise<Int> { re, _ -> re(it + 1) }
    })
    val p5 = p4 then {
        assert(4 == it)
        println("--then promise $it")
    }
    val pz = p5 finally {
        println("--finally")
    }
}

fun catchTest() {
    println("Catch tests:")
    val p0 = Promise<Int> { re, _ -> re(1) }
    val p1 = p0 then {
        assert(1 == it)
        println("--then $it")
        it / 0
    }
    val p2 = p1 catch { it: Throwable ->
        assert(true)
        println("--catch $it")
        IllegalStateException("new")
    }
    val p3 = p2 catch { it: IllegalStateException ->
        assert(true)
        println("--catch $it")
        it
    }
    val p3b = p2 catch  { it: IllegalStateException ->
        assert(true)
        println("--catch another branch $it")
        it
    }
    val p4 = p3 catch { it: Throwable ->
        assert(true)
        println("--catch $it")
        it
    }
    val pz = p4 finally {
        println("--finally")
    }
}

fun finallyTest() {
    println("Catch tests:")
    val p0 = Promise<Int> { re, _ -> Thread.sleep(5000); re(1) }
    p0 finally {
        println("--finally")
    }

    val p1 = p0 then { }
    p1 finally {
        println("--finally then")
    }

    val p2 = p0 catch { it: Throwable -> doNothing(); it}
    p2 finally {
        println("--finally catch")
    }
}