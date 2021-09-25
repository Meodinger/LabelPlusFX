package info.meodinger.lpfx

import info.meodinger.lpfx.options.Logger
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.type.TransFile
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.type.TransLabel

import javafx.application.Application
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.Stage
import tornadofx.getValue
import tornadofx.setValue
import java.io.File


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */

/**
 * Modal & Manager for LPFX
 */
object State {

    lateinit var application: Application
    lateinit var controller: Controller
    lateinit var stage: Stage

    val isOpenedProperty = SimpleBooleanProperty(false)
    val isChangedProperty = SimpleBooleanProperty(false)
    val transFileProperty = SimpleObjectProperty(TransFile.DEFAULT_FILE)
    val transPathProperty = SimpleStringProperty("")
    val currentPicNameProperty = SimpleStringProperty("")
    val currentGroupIdProperty = SimpleIntegerProperty(0)
    val currentLabelIndexProperty = SimpleIntegerProperty(NOT_FOUND)
    val viewModeProperty = SimpleObjectProperty(ViewMode.GroupMode)
    val workModeProperty = SimpleObjectProperty(WorkMode.InputMode)

    var isOpened: Boolean by isOpenedProperty
    var isChanged: Boolean by isChangedProperty
    var transFile: TransFile by transFileProperty
    var transPath: String by transPathProperty
    var currentPicName: String by currentPicNameProperty
    var currentGroupId: Int by currentGroupIdProperty
    var currentLabelIndex: Int by currentLabelIndexProperty
    var viewMode: ViewMode by viewModeProperty
    var workMode: WorkMode by workModeProperty

    fun reset() {
        isOpened = false
        transFile = TransFile.DEFAULT_FILE
        transPath = ""
        //currentPicName = ""
        //currentGroupId = 0
        //currentLabelIndex = NOT_FOUND
        isChanged = false
        workMode = WorkMode.InputMode
        viewMode = ViewMode.getMode(Settings[Settings.ViewModePreference].asStringList()[0])

        controller.reset()

        Logger.info("Reset")
    }

    fun addTransGroup(transGroup: TransGroup) {
        transFile.groupList.add(transGroup)

        Logger.info("Added $transGroup", "State")
    }
    fun removeTransGroup(transGroup: TransGroup) {
        transFile.groupList.remove(transGroup)

        Logger.info("Removed $transGroup", "State")
    }
    fun setTransGroupName(groupId: Int, name: String) {
        transFile.getTransGroupAt(groupId).name = name

        Logger.info("Set GroupID=$groupId @name=$name", "State")
    }
    fun setTransGroupColor(groupId: Int, color: String) {
        transFile.getTransGroupAt(groupId).color = color

        Logger.info("Set GroupID=$groupId @color=$color", "State")
    }

    fun addPicture(picName: String) {
        transFile.transMap[picName] = ArrayList()

        Logger.info("Added picture $picName", "State")
    }
    fun removePicture(picName: String) {
        transFile.transMap.remove(picName)

        Logger.info("Removed picture $picName", "State")
    }

    fun addTransLabel(picName: String, transLabel: TransLabel) {
        transFile.getTransLabelListOf(picName).add(transLabel)

        Logger.info("Added $picName @ $transLabel", "State")
    }
    fun removeTransLabel(picName: String, transLabel: TransLabel) {
        transFile.getTransLabelListOf(picName).remove(transLabel)

        Logger.info("Removed $picName @ $transLabel", "State")
    }
    fun setTransLabelIndex(picName: String, index: Int, newIndex: Int) {
        transFile.getTransLabelAt(picName, index).index = newIndex

        Logger.info("Set $picName->Index=$index @index=$newIndex", "State")
    }
    fun setTransLabelGroup(picName: String, index: Int, groupId: Int) {
        transFile.getTransLabelAt(picName, index).groupId = groupId

        Logger.info("Set $picName->Index=$index @groupId=$groupId", "State")
    }

    fun setComment(comment: String) {
        transFile.comment = comment

        Logger.info("Set comment @ ${comment.replace("\n", " ")}")
    }

    fun getFileFolder(): String = File(transPath).parent
    fun getBakFolder(): String = getFileFolder() + File.separator + FOLDER_NAME_BAK
    fun getPicPathOf(picName: String): String = getFileFolder() + File.separator + picName
    fun getPicPathNow(): String = getPicPathOf(currentPicName)
}