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

/**
 * Float Range with automatic start-end detection
 */
fun Float.autoRangeTo(that: Float): ClosedFloatingPointRange<Float> {
    return if (this < that) this.rangeTo(that) else that.rangeTo(this)
}

/**
 * Long Range with automatic start-end detection
 */
fun Long.autoRangeTo(that: Long): ClosedRange<Long> {
    return if (this < that) this.rangeTo(that) else that.rangeTo(this)
}

/**
 * Int Range with automatic start-end detection
 */
fun Int.autoRangeTo(that: Int): ClosedRange<Int> {
    return if (this < that) this.rangeTo(that) else that.rangeTo(this)
}
