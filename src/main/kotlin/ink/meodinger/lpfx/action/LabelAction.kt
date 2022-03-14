package ink.meodinger.lpfx.action

import ink.meodinger.lpfx.LOGSRC_ACTION
import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.string.deleteTailLF
import ink.meodinger.lpfx.util.string.emptyString
import ink.meodinger.lpfx.util.string.fixed
import ink.meodinger.lpfx.util.string.replaceLineFeed

/**
 * Author: Meodinger
 * Date: 2022/3/9
 * Have fun with my code!
 */

/**
 * Action changes TransLabel.
 * The `targetPicName` and `targetTransLabel` indicate the where
 * the action will be taken and which TransLabel will be processed.
 *
 * If action type is `ADD` or `REMOVE`, all parameters starts with
 * "new" could leave as default, they will not be used in the procedure.
 *
 * If action type is `CHANGE`, all parameters starts with "new" and
 * is passed non-default value will be used to update the target
 * TransLabel's properties.
 *
 * @see LPFXAction
 */
class LabelAction(
    actionType: ActionType,
    state: State,
    private val targetPicName: String,
    private val targetTransLabel: TransLabel,
    private val newLabelIndex: Int = NOT_FOUND,
    private val newGroupId: Int = NOT_FOUND,
    private val newX: Double = Double.NaN,
    private val newY: Double = Double.NaN,
    private val newText: String = emptyString()
) : LPFXAction(actionType, state) {

    private val oriLabelIndex: Int = targetTransLabel.index
    private val oriGroupId: Int = targetTransLabel.groupId
    private val oriX: Double = targetTransLabel.x
    private val oriY: Double = targetTransLabel.y
    private val oriText: String = targetTransLabel.text

    private fun applyLabelProps(index: Int, groupId: Int, x: Double, y: Double, text: String) {
        val builder = StringBuilder().apply { appendLine("Applied new props to $targetPicName:$oriLabelIndex") }

        if (newLabelIndex != NOT_FOUND) targetTransLabel.index = index.also {
            builder.appendLine("index: $oriLabelIndex -> $index")
        }
        if (newGroupId != NOT_FOUND) targetTransLabel.groupId = groupId.also {
            builder.appendLine("groupId: $oriGroupId -> $groupId")
        }
        if (!newX.isNaN()) targetTransLabel.x = x.also {
            builder.appendLine("x: ${oriX.fixed(4)} -> ${x.fixed(4)}")
        }
        if (!newY.isNaN()) targetTransLabel.y = y.also {
            builder.appendLine("y: ${oriY.fixed(4)} -> ${y.fixed(4)}")
        }
        if (newText.isNotEmpty()) targetTransLabel.text = text.also {
            builder.appendLine("text: ${text.replaceLineFeed()}")
        }

        Logger.info(builder.deleteTailLF().toString(), LOGSRC_ACTION)
    }
    private fun addTransLabel(picName: String, transLabel: TransLabel) {
        val labelIndex = transLabel.index

        for (label in state.transFile.getTransList(picName)) if (label.index >= labelIndex) label.index++
        state.transFile.addTransLabel(picName, transLabel)

        Logger.info("Added $picName @ $transLabel", LOGSRC_ACTION)
    }
    private fun removeTransLabel(picName: String, transLabel: TransLabel) {
        val labelIndex = transLabel.index

        state.transFile.removeTransLabel(picName, labelIndex)
        for (label in state.transFile.getTransList(picName)) if (label.index > labelIndex) label.index--

        Logger.info("Removed $picName @ $transLabel", LOGSRC_ACTION)
    }

    override fun commit() {
        when (type) {
            ActionType.ADD    -> addTransLabel(targetPicName, targetTransLabel)
            ActionType.REMOVE -> removeTransLabel(targetPicName, targetTransLabel)
            ActionType.CHANGE -> applyLabelProps(newLabelIndex, newGroupId, newX, newY, newText)
        }
    }

    override fun revert() {
        when (type) {
            ActionType.ADD    -> removeTransLabel(targetPicName, targetTransLabel)
            ActionType.REMOVE -> addTransLabel(targetPicName, targetTransLabel)
            ActionType.CHANGE -> applyLabelProps(oriLabelIndex, oriGroupId, oriX, oriY, oriText)
        }
    }

}
