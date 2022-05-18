package ink.meodinger.lpfx.type

import javafx.application.Platform
import javafx.concurrent.Task


/**
 * Author: Meodinger
 * Date: 2021/8/31
 * Have fun with my code!
 */

/**
 * LPFX Task for long-time procedure
 */
abstract class LPFXTask<T> : Task<T>() {

    companion object {
        /**
         * Create a Task by lambda
         * @param task Lambda task
         */
        fun <T> createTask(task: LPFXTask<T>.() -> T): LPFXTask<T> {
            return object : LPFXTask<T>() { override fun call(): T = task(this) }
        }
    }

    /**
     * What to do when task succeeded
     * @param callback Take returned value of the task as it
     */
    fun setOnSucceeded(callback: (T) -> Unit): LPFXTask<T> {
        super.setOnSucceeded {
            @Suppress("UNCHECKED_CAST")
            callback(it.source.value as T)
        }

        return this
    }

    /**
     * What to do when task failed
     * @param callback Take thrown exception while running task as it
     */
    fun setOnFailed(callback: (Throwable) -> Unit): LPFXTask<T> {
        super.setOnFailed {
            callback(it.source.exception)
        }

        return this
    }

    /**
     * Start the task in a new thread
     */
    fun startInNewThread() = Thread(this).start()

    /**
     * Start the task in FX thread
     */
    fun startInFXThread() = Platform.runLater(this)

    /**
     * Alias for [startInNewThread]
     */
    operator fun invoke() = startInNewThread()

}
