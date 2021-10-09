package info.meodinger.lpfx.util.property

import javafx.beans.binding.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableNumberValue

/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Location: info.meodinger.lpfx.util.property
 */

/**
 * Kotlin will translate `!` to `.not()`
 * So this operator function is unnecessary
 */
// operator fun BooleanProperty.not(): BooleanBinding = not()

operator fun IntegerExpression.unaryMinus(): IntegerBinding = negate()
operator fun IntegerExpression.plus(other: ObservableNumberValue): NumberBinding = add(other)
operator fun IntegerExpression.plus(other: Int): IntegerBinding = add(other)
operator fun IntegerExpression.plus(other: Long): LongBinding = add(other)
operator fun IntegerExpression.plus(other: Float): FloatBinding = add(other)
operator fun IntegerExpression.plus(other: Double): DoubleBinding = add(other)
operator fun IntegerExpression.minus(other: ObservableNumberValue): NumberBinding = subtract(other)
operator fun IntegerExpression.minus(other: Int): IntegerBinding = subtract(other)
operator fun IntegerExpression.minus(other: Long): LongBinding = subtract(other)
operator fun IntegerExpression.minus(other: Float): FloatBinding = subtract(other)
operator fun IntegerExpression.minus(other: Double): DoubleBinding = subtract(other)
operator fun IntegerExpression.times(other: ObservableNumberValue): NumberBinding = multiply(other)
operator fun IntegerExpression.times(other: Int): IntegerBinding = multiply(other)
operator fun IntegerExpression.times(other: Long): LongBinding = multiply(other)
operator fun IntegerExpression.times(other: Float): FloatBinding = multiply(other)
operator fun IntegerExpression.times(other: Double): DoubleBinding = multiply(other)
operator fun IntegerExpression.div(other: ObservableNumberValue): NumberBinding = divide(other)
operator fun IntegerExpression.div(other: Int): IntegerBinding = divide(other)
operator fun IntegerExpression.div(other: Long): LongBinding = divide(other)
operator fun IntegerExpression.div(other: Float): FloatBinding = divide(other)
operator fun IntegerExpression.div(other: Double): DoubleBinding = divide(other)

operator fun LongExpression.unaryMinus(): LongBinding = negate()
operator fun LongExpression.plus(other: ObservableNumberValue): NumberBinding = add(other)
operator fun LongExpression.plus(other: Number): LongBinding = add(other.toLong())
operator fun LongExpression.plus(other: Float): FloatBinding = add(other)
operator fun LongExpression.plus(other: Double): DoubleBinding = add(other)
operator fun LongExpression.minus(other: ObservableNumberValue): NumberBinding = subtract(other)
operator fun LongExpression.minus(other: Number): LongBinding = subtract(other.toLong())
operator fun LongExpression.minus(other: Float): FloatBinding = subtract(other)
operator fun LongExpression.minus(other: Double): DoubleBinding = subtract(other)
operator fun LongExpression.times(other: ObservableNumberValue): NumberBinding = multiply(other)
operator fun LongExpression.times(other: Number): LongBinding = multiply(other.toLong())
operator fun LongExpression.times(other: Float): FloatBinding = multiply(other)
operator fun LongExpression.times(other: Double): DoubleBinding = multiply(other)
operator fun LongExpression.div(other: ObservableNumberValue): NumberBinding = divide(other)
operator fun LongExpression.div(other: Number): LongBinding = divide(other.toLong())
operator fun LongExpression.div(other: Float): FloatBinding = divide(other)
operator fun LongExpression.div(other: Double): DoubleBinding = divide(other)

operator fun FloatExpression.unaryMinus(): FloatBinding = negate()
operator fun FloatExpression.plus(other: ObservableNumberValue): NumberBinding = add(other)
operator fun FloatExpression.plus(other: Number): FloatBinding = add(other.toFloat())
operator fun FloatExpression.plus(other: Double): DoubleBinding = add(other)
operator fun FloatExpression.minus(other: ObservableNumberValue): NumberBinding = subtract(other)
operator fun FloatExpression.minus(other: Number): FloatBinding = subtract(other.toFloat())
operator fun FloatExpression.minus(other: Double): DoubleBinding = subtract(other)
operator fun FloatExpression.times(other: ObservableNumberValue): NumberBinding = multiply(other)
operator fun FloatExpression.times(other: Number): FloatBinding = multiply(other.toFloat())
operator fun FloatExpression.times(other: Double): DoubleBinding = multiply(other)
operator fun FloatExpression.div(other: ObservableNumberValue): NumberBinding = divide(other)
operator fun FloatExpression.div(other: Number): FloatBinding = divide(other.toFloat())
operator fun FloatExpression.div(other: Double): DoubleBinding = divide(other)

operator fun DoubleExpression.unaryMinus(): DoubleBinding = negate()
operator fun DoubleExpression.plus(other: ObservableNumberValue): NumberBinding = add(other)
operator fun DoubleExpression.plus(other: Number): DoubleBinding = add(other.toDouble())
operator fun DoubleExpression.minus(other: ObservableNumberValue): NumberBinding = subtract(other)
operator fun DoubleExpression.minus(other: Number): DoubleBinding = subtract(other.toDouble())
operator fun DoubleExpression.times(other: ObservableNumberValue): NumberBinding = multiply(other)
operator fun DoubleExpression.times(other: Number): DoubleBinding = multiply(other.toDouble())
operator fun DoubleExpression.div(other: ObservableNumberValue): NumberBinding = divide(other)
operator fun DoubleExpression.div(other: Number): DoubleBinding = divide(other.toDouble())

/*
 * NumberBinding has ambiguous overload with above
 *
operator fun NumberBinding.unaryMinus(): NumberBinding = negate()
operator fun NumberBinding.plus(other: ObservableNumberValue): NumberBinding = add(other)
operator fun NumberBinding.plus(other: Int): NumberBinding = add(other)
operator fun NumberBinding.plus(other: Long): NumberBinding = add(other)
operator fun NumberBinding.plus(other: Float): NumberBinding = add(other)
operator fun NumberBinding.plus(other: Double): NumberBinding = add(other)
operator fun NumberBinding.minus(other: ObservableNumberValue): NumberBinding = subtract(other)
operator fun NumberBinding.minus(other: Int): NumberBinding = subtract(other)
operator fun NumberBinding.minus(other: Long): NumberBinding = subtract(other)
operator fun NumberBinding.minus(other: Float): NumberBinding = subtract(other)
operator fun NumberBinding.minus(other: Double): NumberBinding = subtract(other)
operator fun NumberBinding.times(other: ObservableNumberValue): NumberBinding = multiply(other)
operator fun NumberBinding.times(other: Int): NumberBinding = multiply(other)
operator fun NumberBinding.times(other: Long): NumberBinding = multiply(other)
operator fun NumberBinding.times(other: Float): NumberBinding = multiply(other)
operator fun NumberBinding.times(other: Double): NumberBinding = multiply(other)
operator fun NumberBinding.div(other: ObservableNumberValue): NumberBinding = divide(other)
operator fun NumberBinding.div(other: Int): NumberBinding = divide(other)
operator fun NumberBinding.div(other: Long): NumberBinding = divide(other)
operator fun NumberBinding.div(other: Float): NumberBinding = divide(other)
operator fun NumberBinding.div(other: Double): NumberBinding = divide(other)
*/

infix fun BooleanExpression.and(other: Boolean): BooleanBinding = and(SimpleBooleanProperty(other))
infix fun BooleanExpression.and(other: ObservableBooleanValue): BooleanBinding = and(other)
infix fun BooleanExpression.or(other: Boolean): BooleanBinding = or(SimpleBooleanProperty(other))
infix fun BooleanExpression.or(other: ObservableBooleanValue): BooleanBinding = or(other)
infix fun BooleanExpression.xor(other: Boolean): BooleanBinding = Bindings.createBooleanBinding( { get() xor other }, this )
infix fun BooleanExpression.xor(other: ObservableBooleanValue): BooleanBinding = Bindings.createBooleanBinding( { get() xor other.get() }, this )
infix fun BooleanExpression.eq(other: Boolean): BooleanBinding = isEqualTo(SimpleBooleanProperty(other))
infix fun BooleanExpression.eq(other: ObservableBooleanValue): BooleanBinding = isEqualTo(other)

infix fun NumberExpression.gt(other: Int): BooleanBinding = greaterThan(other)
infix fun NumberExpression.gt(other: Long): BooleanBinding = greaterThan(other)
infix fun NumberExpression.gt(other: Float): BooleanBinding = greaterThan(other)
infix fun NumberExpression.gt(other: Double): BooleanBinding = greaterThan(other)
infix fun NumberExpression.gt(other: ObservableNumberValue): BooleanBinding = greaterThan(other)
infix fun NumberExpression.ge(other: Int): BooleanBinding = greaterThanOrEqualTo(other)
infix fun NumberExpression.ge(other: Long): BooleanBinding = greaterThanOrEqualTo(other)
infix fun NumberExpression.ge(other: Float): BooleanBinding = greaterThanOrEqualTo(other)
infix fun NumberExpression.ge(other: Double): BooleanBinding = greaterThanOrEqualTo(other)
infix fun NumberExpression.ge(other: ObservableNumberValue): BooleanBinding = greaterThanOrEqualTo(other)
infix fun NumberExpression.eq(other: Int): BooleanBinding = isEqualTo(other)
infix fun NumberExpression.eq(other: Long): BooleanBinding = isEqualTo(other)
infix fun NumberExpression.eq(other: ObservableNumberValue): BooleanBinding = isEqualTo(other)
infix fun NumberExpression.le(other: Int): BooleanBinding = lessThanOrEqualTo(other)
infix fun NumberExpression.le(other: Long): BooleanBinding = lessThanOrEqualTo(other)
infix fun NumberExpression.le(other: Float): BooleanBinding = lessThanOrEqualTo(other)
infix fun NumberExpression.le(other: Double): BooleanBinding = lessThanOrEqualTo(other)
infix fun NumberExpression.le(other: ObservableNumberValue): BooleanBinding = lessThanOrEqualTo(other)
infix fun NumberExpression.lt(other: Int): BooleanBinding = lessThan(other)
infix fun NumberExpression.lt(other: Long): BooleanBinding = lessThan(other)
infix fun NumberExpression.lt(other: Float): BooleanBinding = lessThan(other)
infix fun NumberExpression.lt(other: Double): BooleanBinding = lessThan(other)
infix fun NumberExpression.lt(other: ObservableNumberValue): BooleanBinding = lessThan(other)