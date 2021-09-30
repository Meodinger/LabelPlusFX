@file:Suppress("DuplicatedCode")

package info.meodinger.lpfx.util.property

import javafx.beans.binding.*
import javafx.beans.property.*
import javafx.beans.value.ObservableNumberValue

/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Location: info.meodinger.lpfx.util.property
 */

/**
 * Shadow .not()
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
operator fun BooleanProperty.not(): BooleanBinding = not()

operator fun IntegerProperty.unaryMinus(): IntegerBinding = negate()
operator fun IntegerProperty.plus(other: ObservableNumberValue): NumberBinding = add(other)
operator fun IntegerProperty.minus(other: ObservableNumberValue): NumberBinding = subtract(other)
operator fun IntegerProperty.times(other: ObservableNumberValue): NumberBinding = multiply(other)
operator fun IntegerProperty.div(other: ObservableNumberValue): NumberBinding = divide(other)
operator fun <T : Number> IntegerProperty.plus(other: T): NumberBinding {
    if (other is Int) return add(other)
    if (other is Long) return add(other)
    if (other is Float) return add(other)
    if (other is Double) return add(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> IntegerProperty.minus(other: T): NumberBinding {
    if (other is Int) return subtract(other)
    if (other is Long) return subtract(other)
    if (other is Float) return subtract(other)
    if (other is Double) return subtract(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> IntegerProperty.times(other: T): NumberBinding {
    if (other is Int) return multiply(other)
    if (other is Long) return multiply(other)
    if (other is Float) return multiply(other)
    if (other is Double) return multiply(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> IntegerProperty.div(other: T): NumberBinding {
    if (other is Int) return divide(other)
    if (other is Long) return divide(other)
    if (other is Float) return divide(other)
    if (other is Double) return divide(other)
    else throw IllegalArgumentException("$other invalid")
}

operator fun LongProperty.unaryMinus(): LongBinding = negate()
operator fun LongProperty.plus(other: ObservableNumberValue): NumberBinding = add(other)
operator fun LongProperty.minus(other: ObservableNumberValue): NumberBinding = subtract(other)
operator fun LongProperty.times(other: ObservableNumberValue): NumberBinding = multiply(other)
operator fun LongProperty.div(other: ObservableNumberValue): NumberBinding = divide(other)
operator fun <T : Number> LongProperty.plus(other: T): NumberBinding {
    if (other is Int) return add(other)
    if (other is Long) return add(other)
    if (other is Float) return add(other)
    if (other is Double) return add(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> LongProperty.minus(other: T): NumberBinding {
    if (other is Int) return subtract(other)
    if (other is Long) return subtract(other)
    if (other is Float) return subtract(other)
    if (other is Double) return subtract(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> LongProperty.times(other: T): NumberBinding {
    if (other is Int) return multiply(other)
    if (other is Long) return multiply(other)
    if (other is Float) return multiply(other)
    if (other is Double) return multiply(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> LongProperty.div(other: T): NumberBinding {
    if (other is Int) return divide(other)
    if (other is Long) return divide(other)
    if (other is Float) return divide(other)
    if (other is Double) return divide(other)
    else throw IllegalArgumentException("$other invalid")
}

operator fun FloatProperty.unaryMinus(): FloatBinding = negate()
operator fun FloatProperty.plus(other: ObservableNumberValue): NumberBinding = add(other)
operator fun FloatProperty.minus(other: ObservableNumberValue): NumberBinding = subtract(other)
operator fun FloatProperty.times(other: ObservableNumberValue): NumberBinding = multiply(other)
operator fun FloatProperty.div(other: ObservableNumberValue): NumberBinding = divide(other)
operator fun <T : Number> FloatProperty.plus(other: T): NumberBinding {
    if (other is Int) return add(other)
    if (other is Long) return add(other)
    if (other is Float) return add(other)
    if (other is Double) return add(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> FloatProperty.minus(other: T): NumberBinding {
    if (other is Int) return subtract(other)
    if (other is Long) return subtract(other)
    if (other is Float) return subtract(other)
    if (other is Double) return subtract(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> FloatProperty.times(other: T): NumberBinding {
    if (other is Int) return multiply(other)
    if (other is Long) return multiply(other)
    if (other is Float) return multiply(other)
    if (other is Double) return multiply(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> FloatProperty.div(other: T): NumberBinding {
    if (other is Int) return divide(other)
    if (other is Long) return divide(other)
    if (other is Float) return divide(other)
    if (other is Double) return divide(other)
    else throw IllegalArgumentException("$other invalid")
}

operator fun DoubleProperty.unaryMinus(): DoubleBinding = negate()
operator fun DoubleProperty.plus(other: ObservableNumberValue): NumberBinding = add(other)
operator fun DoubleProperty.minus(other: ObservableNumberValue): NumberBinding = subtract(other)
operator fun DoubleProperty.times(other: ObservableNumberValue): NumberBinding = multiply(other)
operator fun DoubleProperty.div(other: ObservableNumberValue): NumberBinding = divide(other)
operator fun <T : Number> DoubleProperty.plus(other: T): NumberBinding {
    if (other is Int) return add(other)
    if (other is Long) return add(other)
    if (other is Float) return add(other)
    if (other is Double) return add(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> DoubleProperty.minus(other: T): NumberBinding {
    if (other is Int) return subtract(other)
    if (other is Long) return subtract(other)
    if (other is Float) return subtract(other)
    if (other is Double) return subtract(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> DoubleProperty.times(other: T): NumberBinding {
    if (other is Int) return multiply(other)
    if (other is Long) return multiply(other)
    if (other is Float) return multiply(other)
    if (other is Double) return multiply(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> DoubleProperty.div(other: T): NumberBinding {
    if (other is Int) return divide(other)
    if (other is Long) return divide(other)
    if (other is Float) return divide(other)
    if (other is Double) return divide(other)
    else throw IllegalArgumentException("$other invalid")
}

operator fun NumberBinding.unaryMinus(): NumberBinding = negate()
operator fun NumberBinding.plus(other: ObservableNumberValue): NumberBinding = add(other)
operator fun NumberBinding.minus(other: ObservableNumberValue): NumberBinding = subtract(other)
operator fun NumberBinding.times(other: ObservableNumberValue): NumberBinding = multiply(other)
operator fun NumberBinding.div(other: ObservableNumberValue): NumberBinding = divide(other)
operator fun <T : Number> NumberBinding.plus(other: T): NumberBinding {
    if (other is Int) return add(other)
    if (other is Long) return add(other)
    if (other is Float) return add(other)
    if (other is Double) return add(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> NumberBinding.minus(other: T): NumberBinding {
    if (other is Int) return subtract(other)
    if (other is Long) return subtract(other)
    if (other is Float) return subtract(other)
    if (other is Double) return subtract(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> NumberBinding.times(other: T): NumberBinding {
    if (other is Int) return multiply(other)
    if (other is Long) return multiply(other)
    if (other is Float) return multiply(other)
    if (other is Double) return multiply(other)
    else throw IllegalArgumentException("$other invalid")
}
operator fun <T : Number> NumberBinding.div(other: T): NumberBinding {
    if (other is Int) return divide(other)
    if (other is Long) return divide(other)
    if (other is Float) return divide(other)
    if (other is Double) return divide(other)
    else throw IllegalArgumentException("$other invalid")
}