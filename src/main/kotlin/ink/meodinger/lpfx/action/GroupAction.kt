package ink.meodinger.lpfx.action

import ink.meodinger.lpfx.LOGSRC_ACTION
import ink.meodinger.lpfx.LOGSRC_STATE
import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.string.deleteTailLF
import ink.meodinger.lpfx.util.string.emptyString

/**
 * Author: Meodinger
 * Date: 2022/3/9
 * Have fun with my code!
 */

/**
 * Action changes TransGroup.
 * The `targetTransGroup` indicate which TransGroup will be processed.
 *
 * If action type is `ADD` or `REMOVE`, all parameters starts with
 * "new" could leave as default, they will not be used in the procedure.
 *
 * If action type is `CHANGE`, all explicitly passed parameters
 * starts with "new" and is non-default value will be used to update
 * the target TransGroup's properties..
 *
 * @see Action
 */
class GroupAction(
    override val type: ActionType,
    private val state: State,
    private val targetTransGroup: TransGroup,
    private val newName: String = emptyString(),
    private val newColorHex: String = emptyString(),
) : Action {

    private val oriName: String = targetTransGroup.name
    private val oriColorHex: String = targetTransGroup.colorHex

    private fun applyGroupProps(name: String, colorHex: String) {
        val builder = StringBuilder().apply { appendLine("Applied new props to group $oriName") }

        if (newName.isNotEmpty()) targetTransGroup.name = name.also {
            builder.appendLine("name: $oriName -> $it")
        }
        if (newColorHex.isNotEmpty()) targetTransGroup.colorHex = colorHex.also {
            builder.appendLine("colorHex: $oriColorHex -> $it")
        }

        Logger.info(builder.deleteTailLF().toString(), LOGSRC_ACTION)
    }
    private fun addTransGroup(transGroup: TransGroup) {
        state.transFile.addTransGroup(transGroup)

        Logger.info("Added TransGroup: $transGroup", LOGSRC_ACTION)
    }
    private fun removeTransGroup(transGroup: TransGroup) {
        val toRemoveId = state.transFile.getGroupIdByName(transGroup.name)

        state.transFile.removeTransGroup(toRemoveId)
        for (picName in state.transFile.picNames) for (label in state.transFile.getTransList(picName))
            if (label.groupId >= toRemoveId) label.groupId--

        Logger.info("Removed TransGroup: $transGroup", LOGSRC_STATE)
    }

    override fun commit() {
        when (type) {
            ActionType.ADD    -> addTransGroup(targetTransGroup)
            ActionType.REMOVE -> removeTransGroup(targetTransGroup)
            ActionType.CHANGE -> applyGroupProps(newName, newColorHex)
        }
    }

    override fun revert() {
        when (type) {
            ActionType.ADD    -> removeTransGroup(targetTransGroup)
            ActionType.REMOVE -> addTransGroup(targetTransGroup)
            ActionType.CHANGE -> applyGroupProps(oriName, oriColorHex)
        }
    }

}