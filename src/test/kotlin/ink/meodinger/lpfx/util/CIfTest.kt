package ink.meodinger.lpfx.util

import org.junit.Assert.assertEquals
import org.junit.Test


/**
 * Author: Meodinger
 * Date: 2021/12/11
 * Have fun with my code!
 */

class CIfTest {

    @Test
    fun ifUnit() {
        val a = _if_({ }) { 0 } _else_ { 1 }
        assertEquals(1, a())
    }

    @Test
    fun ifBoolean() {
        val a = _if_({ true }) { 0 } _else_ { 1 }
        assertEquals(0, a())

        val b = _if_({ false }) { 0 } _else_ { 1 }
        assertEquals(1, b())
    }

    @Test
    fun ifNumber() {
        val a = _if_({ 0 }) { 0 } _else_ { 1 }
        assertEquals(1, a())

        val b = _if_({ 1 }) { 0 } _else_ { 1 }
        assertEquals(0, b())
    }

    @Test
    fun ifString() {
        val a = _if_({ "" }) { 0 } _else_ { 1 }
        assertEquals(1, a())

        val b = _if_({ " " }) { 0 } _else_ { 1 }
        assertEquals(0, b())
    }

    @Test
    fun ifNullable() {
        val a = _if_({ null }) { 0 } _else_ { 1 }
        assertEquals(1, a())

        val b = _if_({ Any() }) { 0 } _else_ { 1 }
        assertEquals(0, b())
    }

    @Test
    fun ifTrue() {
        val a = _if_({ true }) { 0 }
        assertEquals(0, a())
    }

    @Test
    fun ifFalse() {
        val a = _if_({ false }) { 0 }
        try {
            a()
        } catch (_ : Throwable) {
            return
        }
        assert(false)
    }

    @Test
    fun ifTrueElse() {
        val a = _if_({ true }) { 0 } _else_ { 1 }
        assertEquals(0, a())
    }

    @Test
    fun ifFalseElse() {
        val a = _if_({ false }) { 0 } _else_ { 1 }
        assertEquals(1, a())
    }

    @Test
    fun ifTrueElseIfTrue() {
        val a = _if_({ true }) { 0 } _else_ _if_({ true }) { 1 }
        assertEquals(0, a())
    }

    @Test
    fun ifTrueElseIfFalse() {
        val a = _if_({ true }) { 0 } _else_ _if_({ false }) { 1 }
        assertEquals(0, a())
    }

    @Test
    fun ifFalseElseIfTrue() {
        val a = _if_({ false }) { 0 } _else_ _if_({ true }) { 1 }
        assertEquals(1, a())
    }

    @Test
    fun ifFalseElseIfFalse() {
        val a = _if_({ false }) { 0 } _else_ _if_({ false }) { 1 }
        try {
            a()
        } catch (_ : Throwable) {
            return
        }
        assert(false)
    }

    @Test
    fun ifTrueElseIfTrueElse() {
        val a = _if_({ true }) { 0 } _else_ _if_({ true }) { 1 } _else_ { 2 }
        assertEquals(0, a())
    }

    @Test
    fun ifTrueElseIfFalseElse() {
        val a = _if_({ true }) { 0 } _else_ _if_({ false }) { 1 } _else_ { 2 }
        assertEquals(0, a())
    }

    @Test
    fun ifFalseElseIfTrueElse() {
        val a = _if_({ false }) { 0 } _else_ _if_({ true }) { 1 } _else_ { 2 }
        assertEquals(1, a())
    }

    @Test
    fun ifFalseElseIfFalseElse() {
        val a = _if_({ false }) { 0 } _else_ _if_({ false }) { 1 } _else_ { 2 }
        assertEquals(2, a())
    }

    @Test
    fun ifNot() {
        val a = _if_({ false }) { 0 } _else_ _if_not_({ false }) { 1 } _else_ { 2 }
        assertEquals(1, a())
    }

    @Test
    fun ifNull() {
        val a = _if_({ false }) { 0 } _else_ _if_null_({ null }) { 1 } _else_ { 2 }
        assertEquals(1, a())
    }

    @Test
    fun ifZero() {
        val a = _if_({ false }) { 0 } _else_ _if_zero_({ 0 }) { 1 } _else_ { 2 }
        assertEquals(1, a())
    }

    @Test
    fun ifNeg1() {
        val a = _if_({ false }) { 0 } _else_ _if_neg1_({ -1 }) { 1 } _else_ { 2 }
        assertEquals(1, a())
    }

    @Test
    fun ifSelfCall() {
        val a = (_if_({ false }) { 0 } _else_ _if_({ false }) { 1 } _else_ { 2 })()
        assertEquals(2, a)
    }

    @Test
    fun ifFunctionCall() {
        val arr = ArrayList<Int>()

        val a = (_if_({
            val temp = arr.isNotEmpty() // false
            arr.add(0)
            temp
        }) {
             0
        } _else_ _if_ ({
            arr.isNotEmpty() // true here
        }) {
            1
        } _else_ {
            2
        })()
        assertEquals(1 , a)
    }

}
