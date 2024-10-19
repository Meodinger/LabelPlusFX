package ink.meodinger.lpfx.action

import ink.meodinger.lpfx.*
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
            if (targetTransLabel.index  == index) return
            val transLabel = TransLabel(
                index,
                targetTransLabel.groupId,
                targetTransLabel.x,
                targetTransLabel.y,
                targetTransLabel.text
            )
            removeTransLabel(targetPicName,targetTransLabel)
            addTransLabel(targetPicName,transLabel)
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

        Logger.info(builder.deleteTrailingEOL().toString(), "Action")
    }
    private fun addTransLabel(picName: String, transLabel: TransLabel) {
        val list = state.transFile.transMapObservable[picName]
            ?: throw IllegalArgumentException(String.format(I18N["exception.action.picture_not_found.s"], picName))

        // Check group index invalid
        if (transLabel.groupId >= state.transFile.groupCount)
            throw IllegalArgumentException(String.format(I18N["exception.action.label_group_invalid.i"], transLabel.groupId))

        for (label in list) if (label.index >= transLabel.index) label.index++
        list.add(transLabel.index -1 ,transLabel)
        @Suppress("DEPRECATION") state.transFile.installLabel(transLabel)

        Logger.info("Added $picName @ $transLabel", "Action")
    }
    private fun removeTransLabel(picName: String, transLabel: TransLabel) {
        val list = state.transFile.transMapObservable[picName]
            ?: throw IllegalArgumentException(String.format(I18N["exception.action.picture_not_found.s"], picName))

        @Suppress("DEPRECATION") state.transFile.disposeLabel(transLabel)
        list.remove(transLabel)
        for (label in list) if (label.index > transLabel.index) label.index--

        Logger.info("Removed $picName @ $transLabel", "Action")
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
