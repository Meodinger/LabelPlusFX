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
 * Provide a convenient way to run functions in `Action`.
 * Note that this class doesn't logically force the
 * revert function to revert the changes of commit function
 * has made. So it's an internal class.
 */
internal class FunctionAction(
    private val commitFunc: () -> Unit,
    private val revertFunc: () -> Unit,
) : Action {

    override val type: ActionType get() = throw UnsupportedOperationException("ActionType is not fit to FunctionAction")

    override fun commit() = commitFunc()
    override fun revert() = revertFunc()

}


/**
 * A `ComplexAction` is an `Action` that is made up of a bench of `Action`.
 * Its commit method will call each action's commit method by order, and
 * its revert method will call each action's revert method by reversed order.
 * Notice that all actions may not have the same type, its type is set to `CHANGE`
 */
class ComplexAction(private val actions: List<Action>) : Action {

    companion object {
        fun of(vararg actions: Action) = ComplexAction(actions.toList())
        fun of(vararg actions: List<Action>) = ComplexAction(contact(*actions))
    }

    override val type: ActionType = ActionType.CHANGE

    override fun commit() {
        actions.forEach(Action::commit)
    }

    override fun revert() {
        actions.reversed().forEach(Action::revert)
    }

}
