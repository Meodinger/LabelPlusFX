package ink.meodinger.lpfx.action

import ink.meodinger.lpfx.util.collection.contact

/**
 * Author: Meodinger
 * Date: 2022/3/9
 * Have fun with my code!
 */

/**
 * An `Action` indicates what an action will do, and how to undo.
 */
interface Action {

    val type: ActionType

    /**
     * Do the action
     */
    fun commit()

    /**
     * Undo the action
     */
    fun revert()

}

enum class ActionType { ADD, REMOVE, CHANGE }

/**
 * A `ComplexAction` is an `Action` that is made up of a bench of `Action`.
 * Its commit method will call each action's commit method by order, and
 * its revert method will call each action's revert method by reversed order.
 * Known that all actions may not have the same type, so it will throw
 * `UnsupportedOperationException` if `type` is requested.
 */
class ComplexAction(private val actions: List<Action>) : Action {

    companion object {
        fun of(vararg actions: Action) = ComplexAction(actions.toList())
        fun of(vararg actions: List<Action>) = ComplexAction(contact(*actions))
    }

    override val type: ActionType get() = throw UnsupportedOperationException("ActionType is not fit to ComplexAction")

    override fun commit() {
        for (i in actions.indices) actions[i].commit()
    }

    override fun revert() {
        for (i in actions.indices.reversed()) actions[i].revert()
    }

}
