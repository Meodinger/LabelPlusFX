package ink.meodinger.lpfx.action

import ink.meodinger.lpfx.LOGSRC_ACTION
import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.type.TransFile
import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.file.exists

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
    private val oriPicFile: File? = state.transFile.getFile(targetPicName)

    private fun applyPicFile(picFile: File) {
        val curFile = state.transFile.getFile(targetPicName)
            ?: throw TransFile.TransFileException.pictureNotFound(targetPicName)

        state.transFile.setFile(targetPicName, picFile)

        Logger.info("Change file of picture <$targetPicFile> ${curFile.path} -> ${picFile.path}", LOGSRC_ACTION)
    }
    private fun addPicture(picName: String, transList: List<TransLabel>, picFile: File?) {
        if (state.transFile.transMapObservable.contains(picName))
            throw TransFile.TransFileException.pictureNameRepeated(picName)

        state.transFile.transMapObservable[picName] = FXCollections.observableArrayList(transList)
        if (picFile.exists()) state.transFile.setFile(picName, picFile!!)

        Logger.info("Added picture <$picName>: ${state.transFile.getFile(picName)!!.path}", LOGSRC_ACTION)
    }
    private fun removePicture(picName: String) {
        if (!state.transFile.transMapObservable.contains(picName))
            throw TransFile.TransFileException.pictureNotFound(picName)

        state.transFile.setFile(picName, null)
        state.transFile.transMapObservable.remove(picName)

        Logger.info("Removed picture <$picName>", LOGSRC_ACTION)
    }

    override fun commit() {
        when (type) {
            ActionType.ADD    -> addPicture(targetPicName, emptyList(), targetPicFile)
            ActionType.REMOVE -> removePicture(targetPicName)
            ActionType.CHANGE -> applyPicFile(targetPicFile!!)
        }
    }

    override fun revert() {
        when (type) {
            ActionType.ADD    -> removePicture(targetPicName)
            ActionType.REMOVE -> addPicture(targetPicName, oriTransList, oriPicFile)
            ActionType.CHANGE -> applyPicFile(oriPicFile!!)
        }
    }

}
