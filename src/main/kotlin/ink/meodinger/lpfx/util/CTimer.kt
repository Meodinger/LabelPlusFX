package ink.meodinger.lpfx.util.timer

import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.BooleanProperty
import javafx.beans.property.LongProperty
import javafx.beans.property.SimpleBooleanProperty
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
inline fun genTask(crossinline task: () -> Unit): TimerTask = object : TimerTask() { override fun run() = task() }

/**
 * Generate a Timer only has one task
 *
 * Clear -> Schedule -> Clear
 *
 * @param task What the only TimerTask to do
 */
class TimerTaskManager(
    delay: Long,
    period: Long,
    task: TimerTaskManager.() -> Unit
): Timer() {

    companion object {
        fun once(delay: Long, task: TimerTaskManager.() -> Unit) {
            TimerTaskManager(delay, 0) {
                task(this)
                cancel()
            }.schedule()
        }
    }

    private val _task: () -> Unit = { task(this) }
    private var timerTask: TimerTask = genTask(_task)

    private val runningProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun runningProperty(): BooleanProperty = runningProperty

    /**
     * Indicate whether the task is running.
     *
     * This boolean will be true if the task is scheduled and running,
     * and will be false when the task is not scheduled or cancelled.
     */
    var running: Boolean by runningProperty
        private set

    private val delayProperty: LongProperty = SimpleLongProperty(delay)
    fun delayProperty(): LongProperty = delayProperty
    var delay: Long
        get() = delayProperty.get()
        set(value) {
            if (running) throw IllegalStateException("You cannot change the delay when task is running")
            if (value < 0) throw IllegalArgumentException("Delay must not be negative")
            delayProperty.set(value)
        }

    private val periodProperty: LongProperty = SimpleLongProperty(period)
    fun periodProperty(): LongProperty = periodProperty
    var period: Long
        get() = periodProperty.get()
        set(value) {
            if (running) throw IllegalStateException("You cannot change the period when task is running")
            if (value < 0) throw IllegalArgumentException("Period must not be negative")
            periodProperty.set(value)
        }

    fun schedule() {
        // if (running) throw IllegalStateException("Task already scheduled")
        if (running) return

        schedule(timerTask, delay, period)
        running = true
    }

    fun clear() {
        // if (!running) throw IllegalStateException("Task not running")
        if (!running) return

        timerTask.cancel()
        purge()
        running = false

        // Re-generate the timer task for the next turn
        timerTask = genTask(_task)
    }

}
