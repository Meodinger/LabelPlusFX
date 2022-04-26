package ink.meodinger.lpfx.action

import ink.meodinger.lpfx.LOGSRC_ACTION
import ink.meodinger.lpfx.NOT_FOUND
import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.string.*

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
 * If action type is `CHANGE`, all explicitly passed parameters
 * starts with "new" and is non-default value will be used to update
 * the target TransLabel's properties.
 *
 * @see Action
 */
class LabelAction(
    override val type: ActionType,
    private val state: State,
    private val targetPicName: String,
    private val targetTransLabel: TransLabel,
    private val newLabelIndex: Int = NOT_FOUND,
    private val newGroupId: Int = NOT_FOUND,
    private val newX: Double = Double.NaN,
    private val newY: Double = Double.NaN,
    private val newText: String? = null
) : Action {

    private val oriLabelIndex: Int = targetTransLabel.index
    private val oriGroupId: Int = targetTransLabel.groupId
    private val oriX: Double = targetTransLabel.x
    private val oriY: Double = targetTransLabel.y
    private val oriText: String = targetTransLabel.text

    private fun applyLabelProps(index: Int, groupId: Int, x: Double, y: Double, text: String) {
        val builder = StringBuilder().apply {
            append("Applied new props to <$targetPicName:${targetTransLabel.index.pad(2)}> ")
        }

        if (newLabelIndex != NOT_FOUND) {
            builder.append("@index: ${targetTransLabel.index.pad(2)} -> ${index.pad(2)}; ")
            targetTransLabel.index = index
        }
        if (newGroupId != NOT_FOUND) {
            builder.append("@groupId: ${targetTransLabel.groupId.pad(2)} -> ${groupId.pad(2)}; ")
            targetTransLabel.groupId = groupId
        }
        if (!newX.isNaN()) {
            builder.append("@x: ${targetTransLabel.x.fixed(4)} -> ${x.fixed(4)}; ")
            targetTransLabel.x = x
        }
        if (!newY.isNaN()) {
            builder.append("@y: ${targetTransLabel.y.fixed(4)} -> ${y.fixed(4)}; ")
            targetTransLabel.y = y
        }
        if (newText != null) {
            builder.appendLine()
            builder.appendLine("from: ${targetTransLabel.text.replaceEOL()}")
            builder.appendLine("dest: ${text.replaceEOL()}")
            targetTransLabel.text = text
        }

        Logger.info(builder.deleteTailEOL().toString(), LOGSRC_ACTION)
    }
    private fun addTransLabel(picName: String, transLabel: TransLabel) {
        val list = state.transFile.transMapObservable[picName]
            ?:// TODO: I18N
            throw IllegalStateException("")

        if (transLabel.groupId >= state.transFile.groupCount)
            // TODO: I18N
            throw IllegalStateException("")

        val labelIndex = transLabel.index
        for (label in list) if (label.index >= labelIndex) label.index++
        list.add(transLabel)

        Logger.info("Added $picName @ $transLabel", LOGSRC_ACTION)
    }
    private fun removeTransLabel(picName: String, transLabel: TransLabel) {
        val list = state.transFile.transMapObservable[picName]
            ?:// TODO: I18N
            throw IllegalStateException("")

        val labelIndex = transLabel.index
        list.remove(list.first { it.index == labelIndex })
        for (label in list) if (label.index > labelIndex) label.index--

        Logger.info("Removed $picName @ $transLabel", LOGSRC_ACTION)
    }

    override fun commit() {
        when (type) {
            ActionType.ADD    -> addTransLabel(targetPicName, targetTransLabel)
            ActionType.REMOVE -> removeTransLabel(targetPicName, targetTransLabel)
            ActionType.CHANGE -> applyLabelProps(newLabelIndex, newGroupId, newX, newY, newText ?: emptyString())
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
