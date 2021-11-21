package ink.meodinger.lpfx.util

import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.LongProperty
import javafx.beans.property.SimpleLongProperty
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/11/21
 * Have fun with my code!
 */

/**
 * Generate a TimerTask by a lambda
 * @param task What the TimerTask to do
 * @return A TimerTask
 */
inline fun genTask(crossinline task: () -> Unit): TimerTask {
    return object : TimerTask() {
        override fun run() {
            task()
        }
    }
}


/**
 * Generate a Timer only has one task
 *
 * Clear -> Refresh -> Schedule
 *
 * @param task What the only TimerTask to do
 */
class TimerTaskManager(
    delay: Long = DEFAULT_DELAY,
    period: Long = DEFAULT_PERIOD,
    private val task: () -> Unit
): Timer() {

    companion object {
        const val DEFAULT_DELAY = 1000L
        const val DEFAULT_PERIOD = 1000L
    }

    private var timerTask: TimerTask = genTask(task)

    val delayProperty: LongProperty = SimpleLongProperty(delay)
    fun delayProperty(): LongProperty = delayProperty
    var delay: Long by delayProperty

    val periodProperty: LongProperty = SimpleLongProperty(period)
    fun periodProperty(): LongProperty = periodProperty
    var period: Long by periodProperty

    fun refresh() {
        timerTask = genTask(task)
    }

    fun schedule() {
        this.schedule(timerTask, delay, period)
    }

    fun clear() {
        timerTask.cancel()
        this.purge()
    }

}