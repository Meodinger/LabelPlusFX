package ink.meodinger.lpfx.action

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.type.TransLabel

import javafx.collections.FXCollections
import java.io.File

/**
 * Author: Meodinger
 * Date: 2022/3/9
 * Have fun with my code!
 */

/**
 * Action changes Picture.
 * The `targetPicName` indicates which picture that will be processed.
 *
 * If action type is `ADD`, `targetPicName` will be added to TransFile's
 * transMap. Only when `targetPicFile` exists picture file will be updated.
 *
 * If action type is `REMOVE`, `targetPicName` will be removed from
 * TransFile's transMap. The argument `targetPicFile` will not be used,
 * just leave it as default.
 *
 * If action type is `CHANGE`, file related to `targetPicName` will be
 * updated to non-nullable `targetPicFile`.
 *
 * @see Action
 */
class PictureAction(
    override val type: ActionType,
    private val state: State,
    private val targetPicName: String,
    private val targetPicFile: File? = null
) : Action {

    private val oriTransList: List<TransLabel> = state.transFile.transMapObservable[targetPicName] ?: emptyList()
    private val oriPicFile: File = state.transFile.getFile(targetPicName)

    private fun applyPicFile(picFile: File?) {
        val lastFile = state.transFile.getFile(targetPicName)
        state.transFile.setFile(targetPicName, picFile)
        val currFile = state.transFile.getFile(targetPicName)

        Logger.info("Change file of picture <$targetPicFile> ${lastFile.path} -> ${currFile.path}", "Action")
    }
    private fun addPicture(picName: String, transList: List<TransLabel>, picFile: File?) {
        if (state.transFile.transMapObservable.contains(picName))
            throw IllegalArgumentException(String.format(I18N["exception.action.picture_repeated.s"], picName))

        state.transFile.transMapObservable[picName] = FXCollections.observableArrayList(transList)
        state.transFile.setFile(picName, picFile)
        @Suppress("DEPRECATION") state.transFile.getTransList(picName).forEach(state.transFile::installLabel)

        Logger.info("Added picture <$picName>: ${state.transFile.getFile(picName).path}", "Action")
    }
    private fun removePicture(picName: String) {
        if (!state.transFile.transMapObservable.contains(picName))
            throw IllegalArgumentException(String.format(I18N["exception.action.picture_not_found.s"], picName))

        @Suppress("DEPRECATION") state.transFile.getTransList(picName).forEach(state.transFile::installLabel)
        state.transFile.setFile(picName, null)
        state.transFile.transMapObservable.remove(picName)

        Logger.info("Removed picture <$picName>", "Action")
    }

    override fun commit() {
        when (type) {
            ActionType.ADD    -> addPicture(targetPicName, emptyList(), targetPicFile)
            ActionType.REMOVE -> removePicture(targetPicName)
            ActionType.CHANGE -> applyPicFile(targetPicFile)
        }
    }

    override fun revert() {
        when (type) {
            ActionType.ADD    -> removePicture(targetPicName)
            ActionType.REMOVE -> addPicture(targetPicName, oriTransList, oriPicFile)
            ActionType.CHANGE -> applyPicFile(oriPicFile)
        }
    }

}
