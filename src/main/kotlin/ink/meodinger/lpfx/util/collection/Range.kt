package ink.meodinger.lpfx.util.collection

/**
 * Author: Meodinger
 * Date: 2022/4/29
 * Have fun with my code!
 */

/**
 * Double Range with automatic start-end detection
 */
fun Double.autoRangeTo(that: Double): ClosedFloatingPointRange<Double> {
    return if (this < that) this.rangeTo(that) else that.rangeTo(this)
}

operator fun ClosedRange<Double>.times(that: Double): ClosedRange<Double> {
    return (start * that).rangeTo(endInclusive * that)
}

operator fun ClosedRange<Double>.div(that: Double): ClosedRange<Double> {
    return (start / that).rangeTo(endInclusive / that)
}
