package ink.meodinger.lpfx.type

import javafx.concurrent.Task


/**
 * Author: Meodinger
 * Date: 2021/8/31
 * Have fun with my code!
 */

/**
 * LPFX Task for long-time procedure
 */
open class LPFXTask<T>(private val task: () -> T) : Task<T>() {

    fun setOnSucceeded(callback: (T) -> Unit): LPFXTask<T> {
        super.setOnSucceeded {
            @Suppress("UNCHECKED_CAST")
            callback(it.source.value as T)
        }

        return this
    }

    fun setOnFailed(callback: (Throwable) -> Unit): LPFXTask<T> {
        super.setOnFailed {
            callback(it.source.exception)
        }

        return this
    }

    fun startInNewThread() = Thread(this).start()

    override fun call(): T = task()

}
