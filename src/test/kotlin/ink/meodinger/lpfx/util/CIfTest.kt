package ink.meodinger.lpfx.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Author: Meodinger
 * Date: 2021/12/11
 * Have fun with my code!
 */

class CIfTest {

    @Test
    fun ifTrue() {
        val a = _if_(true) { 0 }
        assertEquals(a(), 0)
    }

    @Test
    fun ifFalse() {
        val a = _if_(false) { 0 }
        try {
            a()
        } catch (_ : Exception) {
            return
        }
        assert(false)
    }

    @Test
    fun ifTrueElse() {
        val a = _if_(true) { 0 } _else_ { 1 }
        assertEquals(a(), 0)
    }

    @Test
    fun ifFalseElse() {
        val a = _if_(false) { 0 } _else_ { 1 }
        assertEquals(a(), 1)
    }

    @Test
    fun ifTrueElseIfTrue() {
        val a = _if_(true) { 0 } _else_ _if_(true) { 1 }
        assertEquals(a(), 0)
    }

    @Test
    fun ifTrueElseIfFalse() {
        val a = _if_(true) { 0 } _else_ _if_(false) { 1 }
        assertEquals(a(), 0)
    }

    @Test
    fun ifFalseElseIfTrue() {
        val a = _if_(false) { 0 } _else_ _if_(true) { 1 }
        assertEquals(a(), 1)
    }

    @Test
    fun ifFalseElseIfFalse() {
        val a = _if_(false) { 0 } _else_ _if_(false) { 1 }
        try {
            a()
        } catch (_ : Exception) {
            return
        }
        assert(false)
    }

    @Test
    fun ifTrueElseIfTrueElse() {
        val a = _if_(true) { 0 } _else_ _if_(true) { 1 } _else_ { 2 }
        assertEquals(a(), 0)
    }

    @Test
    fun ifTrueElseIfFalseElse() {
        val a = _if_(true) { 0 } _else_ _if_(false) { 1 } _else_ { 2 }
        assertEquals(a(), 0)
    }

    @Test
    fun ifFalseElseIfTrueElse() {
        val a = _if_(false) { 0 } _else_ _if_(true) { 1 } _else_ { 2 }
        assertEquals(a(), 1)
    }

    @Test
    fun ifFalseElseIfFalseElse() {
        val a = _if_(false) { 0 } _else_ _if_(false) { 1 } _else_ { 2 }
        assertEquals(a(), 2)
    }

    @Test
    fun ifNot() {
        val a = _if_(false) { 0 } _else_ _if_not_(false) { 1 } _else_ { 2 }
        assertEquals(a(), 1)

        val b = _if_(false) { 0 } _else_ _if_not_(null) { 1 } _else_ { 2 }
        assertEquals(b(), 1)

        val c = _if_(false) { 0 } _else_ _if_not_(0) { 1 } _else_ { 2 }
        assertEquals(c(), 1)

        val d = _if_(false) { 0 } _else_ _if_not_("") { 1 } _else_ { 2 }
        assertEquals(d(), 1)
    }

    @Test
    fun ifNull() {
        val a = _if_(false) { 0 } _else_ _if_null_(false) { 1 } _else_ { 2 }
        assertEquals(a(), 2)

        val b = _if_(false) { 0 } _else_ _if_null_(null) { 1 } _else_ { 2 }
        assertEquals(b(), 1)

        val c = _if_(false) { 0 } _else_ _if_null_(0) { 1 } _else_ { 2 }
        assertEquals(c(), 2)

        val d = _if_(false) { 0 } _else_ _if_null_("") { 1 } _else_ { 2 }
        assertEquals(d(), 2)
    }

    @Test
    fun ifZero() {
        val c = _if_(false) { 0 } _else_ _if_zero_(0) { 1 } _else_ { 2 }
        assertEquals(c(), 1)

        val d = _if_(false) { 0 } _else_ _if_zero_(-1) { 1 } _else_ { 2 }
        assertEquals(d(), 2)
    }

    @Test
    fun ifNeg1() {
        val c = _if_(false) { 0 } _else_ _if_neg1_(0) { 1 } _else_ { 2 }
        assertEquals(c(), 2)

        val d = _if_(false) { 0 } _else_ _if_neg1_(-1) { 1 } _else_ { 2 }
        assertEquals(d(), 1)
    }

    @Test
    fun ifSelfCall() {
        val a = (_if_(false) { 0 } _else_ _if_not_(false) { 1 } _else_ { 2 })()
        assertEquals(a, 1)
    }

    @Test
    fun ifAll() {
        val a = (_if_(false) {
            0
        } _else_ _if_(false) {
            1
        } _else_ _if_not_(true) {
            2
        } _else_ _if_null_(true) {
            3
        } _else_ _if_zero_(-1) {
            4
        } _else_ _if_neg1_(0) {
            5
        } _else_ {
            6
        })()
        assertEquals(a, 6)
    }

}
