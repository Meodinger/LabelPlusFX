@file:Suppress("unused", "KDocMissingDocumentation")

package ink.meodinger.lpfx.util.property

import javafx.beans.binding.*
import javafx.beans.property.*
import javafx.beans.value.*
import javafx.collections.*


/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Have fun with my code!
 */

operator fun IntegerExpression.unaryMinus(): IntegerBinding = negate()
operator fun IntegerExpression.plus(other: ObservableIntegerValue): IntegerBinding = add(other) as IntegerBinding
operator fun IntegerExpression.plus(other: ObservableLongValue): LongBinding = add(other) as LongBinding
operator fun IntegerExpression.plus(other: ObservableFloatValue): FloatBinding = add(other) as FloatBinding
operator fun IntegerExpression.plus(other: ObservableDoubleValue): DoubleBinding = add(other) as DoubleBinding
operator fun IntegerExpression.plus(other: Int): IntegerBinding = add(other)
operator fun IntegerExpression.plus(other: Long): LongBinding = add(other)
operator fun IntegerExpression.plus(other: Float): FloatBinding = add(other)
operator fun IntegerExpression.plus(other: Double): DoubleBinding = add(other)
operator fun IntegerExpression.minus(other: ObservableIntegerValue): IntegerBinding = subtract(other) as IntegerBinding
operator fun IntegerExpression.minus(other: ObservableLongValue): LongBinding = subtract(other) as LongBinding
operator fun IntegerExpression.minus(other: ObservableFloatValue): FloatBinding = subtract(other) as FloatBinding
operator fun IntegerExpression.minus(other: ObservableDoubleValue): DoubleBinding = subtract(other) as DoubleBinding
operator fun IntegerExpression.minus(other: Int): IntegerBinding = subtract(other)
operator fun IntegerExpression.minus(other: Long): LongBinding = subtract(other)
operator fun IntegerExpression.minus(other: Float): FloatBinding = subtract(other)
operator fun IntegerExpression.minus(other: Double): DoubleBinding = subtract(other)
operator fun IntegerExpression.times(other: ObservableIntegerValue): IntegerBinding = multiply(other) as IntegerBinding
operator fun IntegerExpression.times(other: ObservableLongValue): LongBinding = multiply(other) as LongBinding
operator fun IntegerExpression.times(other: ObservableFloatValue): FloatBinding = multiply(other) as FloatBinding
operator fun IntegerExpression.times(other: ObservableDoubleValue): DoubleBinding = multiply(other) as DoubleBinding
operator fun IntegerExpression.times(other: Int): IntegerBinding = multiply(other)
operator fun IntegerExpression.times(other: Long): LongBinding = multiply(other)
operator fun IntegerExpression.times(other: Float): FloatBinding = multiply(other)
operator fun IntegerExpression.times(other: Double): DoubleBinding = multiply(other)
operator fun IntegerExpression.div(other: ObservableIntegerValue): IntegerBinding = divide(other) as IntegerBinding
operator fun IntegerExpression.div(other: ObservableLongValue): LongBinding = divide(other) as LongBinding
operator fun IntegerExpression.div(other: ObservableFloatValue): FloatBinding = divide(other) as FloatBinding
operator fun IntegerExpression.div(other: ObservableDoubleValue): DoubleBinding = divide(other) as DoubleBinding
operator fun IntegerExpression.div(other: Int): IntegerBinding = divide(other)
operator fun IntegerExpression.div(other: Long): LongBinding = divide(other)
operator fun IntegerExpression.div(other: Float): FloatBinding = divide(other)
operator fun IntegerExpression.div(other: Double): DoubleBinding = divide(other)

operator fun LongExpression.unaryMinus(): LongBinding = negate()
operator fun LongExpression.plus(other: ObservableNumberValue): LongBinding = add(other) as LongBinding
operator fun LongExpression.plus(other: ObservableFloatValue): FloatBinding = add(other) as FloatBinding
operator fun LongExpression.plus(other: ObservableDoubleValue): DoubleBinding = add(other) as DoubleBinding
operator fun LongExpression.plus(other: Number): LongBinding = add(other.toLong())
operator fun LongExpression.plus(other: Float): FloatBinding = add(other)
operator fun LongExpression.plus(other: Double): DoubleBinding = add(other)
operator fun LongExpression.minus(other: ObservableNumberValue): LongBinding = subtract(other) as LongBinding
operator fun LongExpression.minus(other: ObservableFloatValue): FloatBinding = subtract(other) as FloatBinding
operator fun LongExpression.minus(other: ObservableDoubleValue): DoubleBinding = subtract(other) as DoubleBinding
operator fun LongExpression.minus(other: Number): LongBinding = subtract(other.toLong())
operator fun LongExpression.minus(other: Float): FloatBinding = subtract(other)
operator fun LongExpression.minus(other: Double): DoubleBinding = subtract(other)
operator fun LongExpression.times(other: ObservableNumberValue): LongBinding = multiply(other) as LongBinding
operator fun LongExpression.times(other: ObservableFloatValue): FloatBinding = multiply(other) as FloatBinding
operator fun LongExpression.times(other: ObservableDoubleValue): DoubleBinding = multiply(other) as DoubleBinding
operator fun LongExpression.times(other: Number): LongBinding = multiply(other.toLong())
operator fun LongExpression.times(other: Float): FloatBinding = multiply(other)
operator fun LongExpression.times(other: Double): DoubleBinding = multiply(other)
operator fun LongExpression.div(other: ObservableNumberValue): LongBinding = divide(other) as LongBinding
operator fun LongExpression.div(other: ObservableFloatValue): FloatBinding = divide(other) as FloatBinding
operator fun LongExpression.div(other: ObservableDoubleValue): DoubleBinding = divide(other) as DoubleBinding
operator fun LongExpression.div(other: Number): LongBinding = divide(other.toLong())
operator fun LongExpression.div(other: Float): FloatBinding = divide(other)
operator fun LongExpression.div(other: Double): DoubleBinding = divide(other)

operator fun FloatExpression.unaryMinus(): FloatBinding = negate()
operator fun FloatExpression.plus(other: ObservableNumberValue): FloatBinding = add(other) as FloatBinding
operator fun FloatExpression.plus(other: ObservableDoubleValue): DoubleBinding = add(other) as DoubleBinding
operator fun FloatExpression.plus(other: Number): FloatBinding = add(other.toFloat())
operator fun FloatExpression.plus(other: Double): DoubleBinding = add(other)
operator fun FloatExpression.minus(other: ObservableNumberValue): FloatBinding = subtract(other) as FloatBinding
operator fun FloatExpression.minus(other: ObservableDoubleValue): DoubleBinding = subtract(other) as DoubleBinding
operator fun FloatExpression.minus(other: Number): FloatBinding = subtract(other.toFloat())
operator fun FloatExpression.minus(other: Double): DoubleBinding = subtract(other)
operator fun FloatExpression.times(other: ObservableNumberValue): FloatBinding = multiply(other) as FloatBinding
operator fun FloatExpression.times(other: ObservableDoubleValue): DoubleBinding = multiply(other) as DoubleBinding
operator fun FloatExpression.times(other: Number): FloatBinding = multiply(other.toFloat())
operator fun FloatExpression.times(other: Double): DoubleBinding = multiply(other)
operator fun FloatExpression.div(other: ObservableNumberValue): FloatBinding = divide(other) as FloatBinding
operator fun FloatExpression.div(other: ObservableDoubleValue): DoubleBinding = divide(other) as DoubleBinding
operator fun FloatExpression.div(other: Number): FloatBinding = divide(other.toFloat())
operator fun FloatExpression.div(other: Double): DoubleBinding = divide(other)

operator fun DoubleExpression.unaryMinus(): DoubleBinding = negate()
operator fun DoubleExpression.plus(other: ObservableNumberValue): DoubleBinding = add(other) as DoubleBinding
operator fun DoubleExpression.plus(other: Number): DoubleBinding = add(other.toDouble())
operator fun DoubleExpression.minus(other: ObservableNumberValue): DoubleBinding = subtract(other) as DoubleBinding
operator fun DoubleExpression.minus(other: Number): DoubleBinding = subtract(other.toDouble())
operator fun DoubleExpression.times(other: ObservableNumberValue): DoubleBinding = multiply(other) as DoubleBinding
operator fun DoubleExpression.times(other: Number): DoubleBinding = multiply(other.toDouble())
operator fun DoubleExpression.div(other: ObservableNumberValue): DoubleBinding = divide(other) as DoubleBinding
operator fun DoubleExpression.div(other: Number): DoubleBinding = divide(other.toDouble())

// Boolean bindings
// Kotlin will translate `!` to `.not()`. So this operator function is unnecessary
// operator fun BooleanExpression.not(): BooleanBinding = not()

infix fun BooleanExpression.and(other: Boolean): BooleanBinding = and(ReadOnlyBooleanWrapper(other))
infix fun BooleanExpression.and(other: ObservableBooleanValue): BooleanBinding = and(other)
infix fun BooleanExpression.or(other: Boolean): BooleanBinding = or(ReadOnlyBooleanWrapper(other))
infix fun BooleanExpression.or(other: ObservableBooleanValue): BooleanBinding = or(other)
infix fun BooleanExpression.xor(other: Boolean): BooleanBinding = Bindings.createBooleanBinding( { get() xor other }, this )
infix fun BooleanExpression.xor(other: ObservableBooleanValue): BooleanBinding = Bindings.createBooleanBinding( { get() xor other.get() }, this )
infix fun BooleanExpression.eq(other: Boolean): BooleanBinding = isEqualTo(ReadOnlyBooleanWrapper(other))
infix fun BooleanExpression.eq(other: ObservableBooleanValue): BooleanBinding = isEqualTo(other)

// operator function `compareTo` must return Int
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

// Transform, note that if overload with specified return type (at least Number) will have conflicts.
inline fun <R> IntegerExpression.transform(crossinline transformer: (Int) -> R): ObjectBinding<R> = Bindings.createObjectBinding({ transformer(get()) }, this)
inline fun <R> LongExpression.transform(crossinline transformer: (Long) -> R): ObjectBinding<R> = Bindings.createObjectBinding({ transformer(get()) }, this)
inline fun <R> FloatExpression.transform(crossinline transformer: (Float) -> R): ObjectBinding<R> = Bindings.createObjectBinding({ transformer(get()) }, this)
inline fun <R> DoubleExpression.transform(crossinline transformer: (Double) -> R): ObjectBinding<R> = Bindings.createObjectBinding({ transformer(get()) }, this)
inline fun <R> BooleanExpression.transform(crossinline transformer: (Boolean) -> R): ObjectBinding<R> = Bindings.createObjectBinding({ transformer(get()) }, this)
inline fun <R> StringExpression.transform(crossinline transformer: (String) -> R): ObjectBinding<R> = Bindings.createObjectBinding({ transformer(get()) }, this)
inline fun <T, R> ObjectExpression<T>.transform(crossinline transformer: (T) -> R): ObjectBinding<R> = Bindings.createObjectBinding({ transformer(get()) }, this)

// Get the primitive value
fun ObjectBinding<Int>.primitive(): IntegerBinding = Bindings.createIntegerBinding(this::get, this)
fun ObjectBinding<Long>.primitive(): LongBinding = Bindings.createLongBinding(this::get, this)
fun ObjectBinding<Float>.primitive(): FloatBinding = Bindings.createFloatBinding(this::get, this)
fun ObjectBinding<Double>.primitive(): DoubleBinding = Bindings.createDoubleBinding(this::get, this)
fun ObjectBinding<Boolean>.primitive(): BooleanBinding = Bindings.createBooleanBinding(this::get, this)
fun ObjectBinding<String>.primitive(): StringBinding = Bindings.createStringBinding(this::get, this)

// isEmpty
fun <E> ObservableList<E>.emptyProperty(): BooleanBinding = Bindings.createBooleanBinding(this::isEmpty, this)
fun <E> ObservableSet<E>.emptyProperty(): BooleanBinding = Bindings.createBooleanBinding(this::isEmpty, this)
fun <K, V> ObservableMap<K ,V>.emptyProperty(): BooleanBinding = Bindings.createBooleanBinding(this::isEmpty, this)

// first & last
fun <E> ListProperty<E>.firstElement(): ObjectBinding<E> = Bindings.createObjectBinding({ if (isEmpty()) null else get(0) }, this)
fun <E> ListProperty<E>.lastElement(): ObjectBinding<E> = Bindings.createObjectBinding({ if (isEmpty()) null else get(size - 1) }, this)

// Get read-only property from expression
fun IntegerExpression.readonly(): ReadOnlyIntegerProperty = SimpleIntegerProperty().also { it.bind(this) }
fun LongExpression.readonly(): ReadOnlyLongProperty = SimpleLongProperty().also { it.bind(this) }
fun FloatExpression.readonly(): ReadOnlyFloatProperty = SimpleFloatProperty().also { it.bind(this) }
fun DoubleExpression.readonly(): ReadOnlyDoubleProperty = SimpleDoubleProperty().also { it.bind(this) }
fun BooleanExpression.readonly(): ReadOnlyBooleanProperty = SimpleBooleanProperty().also { it.bind(this) }
fun StringExpression.readonly(): ReadOnlyStringProperty = SimpleStringProperty().also { it.bind(this) }
fun <T> ObjectExpression<T>.readonly(): ReadOnlyObjectProperty<T> = SimpleObjectProperty<T>().also { it.bind(this) }
