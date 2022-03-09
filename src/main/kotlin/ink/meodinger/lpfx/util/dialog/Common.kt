package ink.meodinger.lpfx.util.dialog

import ink.meodinger.lpfx.COMMON_GAP
import ink.meodinger.lpfx.PANE_HEIGHT
import ink.meodinger.lpfx.PANE_WIDTH
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.util.component.add
import ink.meodinger.lpfx.util.component.boxVGrow
import ink.meodinger.lpfx.util.component.withContent
import ink.meodinger.lpfx.util.component.withOwner
import ink.meodinger.lpfx.util.event.isDoubleClick
import ink.meodinger.lpfx.util.image.resizeByRadius
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.ICON
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.resource.loadAsImage
import ink.meodinger.lpfx.util.string.omitHighText
import ink.meodinger.lpfx.util.string.omitWideText

import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Window
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/8/8
 * Have fun with my code!
 */

/**
 * Constant
 */
const val DIALOG_ICON_RADIUS = 32.0

val iconImageView    = ImageView(ICON.resizeByRadius(DIALOG_ICON_RADIUS))
val confirmImageView = ImageView(loadAsImage("/file/image/dialog/Confirm.png").resizeByRadius(DIALOG_ICON_RADIUS))
val infoImageView    = ImageView(loadAsImage("/file/image/dialog/Info.png").resizeByRadius(DIALOG_ICON_RADIUS))
val alertImageView   = ImageView(loadAsImage("/file/image/dialog/Alert.png").resizeByRadius(DIALOG_ICON_RADIUS))
val errorImageView   = ImageView(loadAsImage("/file/image/dialog/Error.png").resizeByRadius(DIALOG_ICON_RADIUS))
fun showDialog(owner: Window? = null, graphic: Node? = null, title: String, header: String?, content: String, vararg buttonTypes: ButtonType): Optional<ButtonType> {
    val dialog = Dialog<ButtonType>()
    dialog.initOwner(owner)
    dialog.graphic = graphic
    dialog.title = title
    dialog.headerText = header
    dialog.contentText = omitWideText(omitHighText(content), dialog.width / 3 * 2)
    dialog.dialogPane.buttonTypes.addAll(buttonTypes)
    dialog.dialogPane.setOnMouseClicked {
        if (it.isDoubleClick) {
            val maxWidth = dialog.width / 3 * 2
            val nowText = Text(dialog.contentText).apply { wrappingWidth = maxWidth }
            val newText = Text(content).apply { wrappingWidth = maxWidth }
            val expandHeight = newText.boundsInLocal.height - nowText.boundsInLocal.height
            dialog.contentText = content
            dialog.height += expandHeight
        }
    }

    return dialog.showAndWait()
}

/**
 * Show message for confirm
 * @param content Message to show
 * @return ButtonType? YES | NO
 */
fun showConfirm(owner: Window?, content: String): Optional<ButtonType> {
    return showConfirm(owner, null, content, I18N["common.confirm"])
}
/**
 * Show message for confirm
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? YES | NO
 */
fun showConfirm(owner: Window?, header: String?, content: String, title: String): Optional<ButtonType> {
    return showDialog(owner, confirmImageView, title, header, content, ButtonType.YES, ButtonType.NO)
}

/**
 * Show alert
 * @param content Alert to show
 * @return ButtonType? YES | NO | CANCEL
 */
fun showAlert(owner: Window?, content: String): Optional<ButtonType> {
    return showAlert(owner, null, content, I18N["common.alert"])
}
/**
 * Show alert
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? YES | NO | CANCEL
 */
fun showAlert(owner: Window?, header: String?, content: String, title: String): Optional<ButtonType> {
    return showDialog(owner, alertImageView, title, header, content, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
}

/**
 * Show information
 * @param content Info to show
 * @return ButtonType? OK
 */
fun showInfo(owner: Window?, content: String): Optional<ButtonType> {
    return showInfo(owner, null, content, I18N["common.info"])
}
/**
 * Show information
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? OK
 */
fun showInfo(owner: Window?, header: String?, content: String, title: String): Optional<ButtonType> {
    return showDialog(owner, infoImageView, title, header, content, ButtonType.OK)
}

/**
 * Show warning
 * @param content Warning to show
 * @return ButtonType? YES | CLOSE
 */
fun showWarning(owner: Window?, content: String): Optional<ButtonType> {
    return showWarning(owner, null, content, I18N["common.warning"])
}
/**
 * Show warning
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? YES | CLOSE
 */
fun showWarning(owner: Window?, header: String?, content: String, title: String): Optional<ButtonType> {
    return showDialog(owner, alertImageView, title, header, content, ButtonType.YES, ButtonType.CLOSE)
}

/**
 * Show error
 * @param content Error to show
 * @return ButtonType? OK
 */
fun showError(owner: Window?, content: String): Optional<ButtonType> {
    return showError(owner, null, content, I18N["common.error"])
}
/**
 * Show error
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? Ok
 */
fun showError(owner: Window?, header: String?, content: String, title: String): Optional<ButtonType> {
    return showDialog(owner, errorImageView, title, header, content, ButtonType.OK)
}


////////////////////////////////////////////////////////////
///// Others
////////////////////////////////////////////////////////////

/**
 * Show stack trace in expandable content
 * @param e Exception to print
 * @return ButtonType? Cancel
 */
fun showException(owner: Window?, e: Throwable): Optional<ButtonType> {
    val sendBtnType = ButtonType(I18N["common.report"], ButtonBar.ButtonData.OK_DONE)
    val dialog = Dialog<ButtonType>() withOwner owner

    dialog.title = I18N["common.error"]
    dialog.isResizable = true
    dialog.headerText = e.javaClass.name
    dialog.dialogPane.prefWidth = PANE_WIDTH
    dialog.dialogPane.prefHeight = PANE_HEIGHT
    dialog.dialogPane.buttonTypes.addAll(sendBtnType, ButtonType.CANCEL)
    dialog.withContent(VBox()) {
        spacing = COMMON_GAP / 2
        add(Label(omitWideText(e.message ?: e.javaClass.name, 400.0)))
        add(Separator())
        add(Label("The exception stacktrace is:"))
        add(TextArea(e.stackTraceToString())) {
            isEditable = false
            boxVGrow = Priority.ALWAYS
        }
    }

    for (buttonType in dialog.dialogPane.buttonTypes) {
        val button = dialog.dialogPane.lookupButton(buttonType) as Button
        button.isDefaultButton = buttonType != sendBtnType
    }

    val applyBtn = dialog.dialogPane.lookupButton(sendBtnType) as Button
    applyBtn.addEventFilter(ActionEvent.ACTION) { event ->
        val button = event.source as Button
        button.text = I18N["common.sending"]
        Logger.sendLog(Logger.log,
            { button.text = I18N["common.sent"] },
            { button.text = I18N["common.failed"] }
        )
        event.consume()
    }

    return dialog.showAndWait()
}
