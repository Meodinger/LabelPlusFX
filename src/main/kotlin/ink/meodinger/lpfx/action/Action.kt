package ink.meodinger.lpfx.action

import ink.meodinger.lpfx.State

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
 * LPFX Action interface for undo-redo-stack.
 * Every `LPFXAction` accepts a `State` to process data
 * and an ActionType to indicate the action type.
 *
 * @see Action
 */
abstract class AbstractAction(
    override val type: ActionType,
    protected val state: State
) : Action
