package ink.meodinger.lpfx.util.timer

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

    private val _task: () -> Unit = { task(this) }
    private var timerTask: TimerTask = genTask(_task)

    private val runningProperty: BooleanProperty = SimpleBooleanProperty(false)
    /**
     * Indicate whether the task is running.
     *
     * This boolean will be true if the task is scheduled and running,
     * and will be false when the task is not scheduled or cancelled.
     */
    fun runningProperty(): BooleanProperty = runningProperty
    /**
     * @see runningProperty
     */
    var isRunning: Boolean
        get() = runningProperty.get()
        private set(value) = runningProperty.set(value)

    private val delayProperty: LongProperty = SimpleLongProperty(delay)
    /**
     * The delay of the first run
     */
    fun delayProperty(): LongProperty = delayProperty
    /**
     * @see delayProperty
     */
    var delay: Long
        get() = delayProperty.get()
        set(value) {
            if (isRunning) throw IllegalStateException("Cannot change the delay while task running")
            if (value <= 0) throw IllegalArgumentException("Delay should greater than 0")
            delayProperty.set(value)
        }

    private val intervalProperty: LongProperty = SimpleLongProperty(period)
    /**
     * The interval between two runs
     */
    fun intervalProperty(): LongProperty = intervalProperty
    /**
     * @see intervalProperty
     */
    var interval: Long
        get() = intervalProperty.get()
        set(value) {
            if (isRunning) throw IllegalStateException("Cannot change the interval while task running")
            if (value <= 0) throw IllegalArgumentException("Interval should greater than 0")
            intervalProperty.set(value)
        }

    /**
     * Start the timer task. If the timer already stared, do nothing.
     */
    fun schedule() {
        // if (running) throw IllegalStateException("Task already scheduled")
        if (isRunning) return

        schedule(timerTask, delay, interval)
        isRunning = true
    }

    /**
     * Stop the timer task. If the timer already stopped, do nothing.
     */
    fun clear() {
        // if (!running) throw IllegalStateException("Task not running")
        if (!isRunning) return

        timerTask.cancel()
        purge()
        isRunning = false

        // Re-generate the timer task for the next turn
        timerTask = genTask(_task)
    }

    /**
     * Run the task immediately
     * @param reset Whether reset the timer or not
     */
    fun fire(reset: Boolean = false) {
        _task()
        if (reset) {
            clear()
            schedule()
        }
    }

}
