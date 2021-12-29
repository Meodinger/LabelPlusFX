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
open class LPFXTask<T>(private val call: () -> T) : Task<T>() {

    fun setOnSucceeded(callback: (T) -> Unit): LPFXTask<T> {
        super.setOnSucceeded {
            @Suppress("UNCHECKED_CAST")
            callback.invoke(it.source.value as T)
        }

        return this
    }

    fun setOnFailed(callback: (Throwable) -> Unit): LPFXTask<T> {
        super.setOnFailed {
            callback.invoke(it.source.exception)
        }

        return  this
    }

    fun startInNewThread() {
        Thread(this).start()
    }

    override fun call(): T {
        return this.call.invoke()
    }
}
