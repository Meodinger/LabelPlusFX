package ink.meodinger.lpfx.util.dialog

import ink.meodinger.lpfx.COMMON_GAP
import ink.meodinger.lpfx.DIALOG_HEIGHT
import ink.meodinger.lpfx.DIALOG_WIDTH
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.util.image.resizeByRadius
import ink.meodinger.lpfx.util.string.omitHighText
import ink.meodinger.lpfx.util.string.omitWideText
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.resource.loadAsImage
import ink.meodinger.lpfx.util.component.add
import ink.meodinger.lpfx.util.component.vGrow
import ink.meodinger.lpfx.util.resource.ICON
import ink.meodinger.lpfx.io.LogSender

import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.text.Text
import javafx.stage.Window
import java.util.Optional


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
fun showDialog(graphic: Node?, title: String, header: String?, content: String, owner: Window?, vararg buttonTypes: ButtonType): Optional<ButtonType> {
    val dialog = Dialog<ButtonType>()
    dialog.initOwner(owner)
    dialog.graphic = graphic
    dialog.title = title
    dialog.headerText = header
    dialog.contentText = omitWideText(omitHighText(content), dialog.width / 3 * 2)
    dialog.dialogPane.buttonTypes.addAll(buttonTypes)
    dialog.dialogPane.setOnMouseClicked {
        if (it.clickCount > 1) {
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
fun showConfirm(content: String, owner: Window?): Optional<ButtonType> {
    return showConfirm(I18N["common.confirm"], null, content, owner)
}
/**
 * Show message for confirm
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? YES | NO
 */
fun showConfirm(title: String, header: String?, content: String, owner: Window?): Optional<ButtonType> {
    return showDialog(confirmImageView, title, header, content, owner, ButtonType.YES, ButtonType.NO)
}

/**
 * Show alert
 * @param content Alert to show
 * @return ButtonType? YES | NO | CANCEL
 */
fun showAlert(content: String, owner: Window?): Optional<ButtonType> {
    return showAlert(I18N["common.alert"], null, content, owner)
}
/**
 * Show alert
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? YES | NO | CANCEL
 */
fun showAlert(title: String, header: String?, content: String, owner: Window?): Optional<ButtonType> {
    return showDialog(alertImageView, title, header, content, owner, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
}

/**
 * Show information
 * @param content Info to show
 * @return ButtonType? OK
 */
fun showInfo(content: String, owner: Window?): Optional<ButtonType> {
    return showInfo(I18N["common.info"], null, content, owner)
}
/**
 * Show information
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? OK
 */
fun showInfo(title: String, header: String?, content: String, owner: Window?): Optional<ButtonType> {
    return showDialog(infoImageView, title, header, content, owner, ButtonType.OK)
}

/**
 * Show warning
 * @param content Warning to show
 * @return ButtonType? YES | CLOSE
 */
fun showWarning(content: String, owner: Window?): Optional<ButtonType> {
    return showWarning(I18N["common.warning"], null, content, owner)
}
/**
 * Show warning
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? YES | CLOSE
 */
fun showWarning(title: String, header: String?, content: String, owner: Window?): Optional<ButtonType> {
    return showDialog(alertImageView, title, header, content, owner, ButtonType.YES, ButtonType.CLOSE)
}

/**
 * Show error
 * @param content Error to show
 * @return ButtonType? OK
 */
fun showError(content: String, owner: Window?): Optional<ButtonType> {
    return showError(I18N["common.error"], null, content, owner)
}
/**
 * Show error
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? Ok
 */
fun showError(title: String, header: String?, content: String, owner: Window?): Optional<ButtonType> {
    return showDialog(errorImageView, title, header, content, owner, ButtonType.OK)
}


////////////////////////////////////////////////////////////
///// Others
////////////////////////////////////////////////////////////

/**
 * Show stack trace in expandable content
 * @param e Exception to print
 * @return ButtonType? Cancel
 */
fun showException(e: Throwable, owner: Window?): Optional<ButtonType> {
    val sendBtnType = ButtonType(I18N["common.report"], ButtonBar.ButtonData.OK_DONE)
    val dialog = Dialog<ButtonType>().apply { initOwner(owner) }

    dialog.title = I18N["common.error"]
    dialog.isResizable = true
    dialog.headerText = e.javaClass.name
    dialog.dialogPane.prefWidth = DIALOG_WIDTH
    dialog.dialogPane.prefHeight = DIALOG_HEIGHT
    dialog.dialogPane.content = VBox().apply {
        spacing = COMMON_GAP / 2
        add(Label(omitWideText(e.message ?: e.javaClass.name, 400.0)))
        add(Separator())
        add(Label("The exception stacktrace is:"))
        add(TextArea(e.stackTraceToString())) {
            isEditable = false
            vGrow = Priority.ALWAYS
        }
    }
    dialog.dialogPane.buttonTypes.addAll(sendBtnType, ButtonType.CANCEL)

    for (buttonType in dialog.dialogPane.buttonTypes) {
        val button = dialog.dialogPane.lookupButton(buttonType) as Button
        button.isDefaultButton = buttonType != sendBtnType
    }

    val applyBtn = dialog.dialogPane.lookupButton(sendBtnType) as Button
    applyBtn.addEventFilter(ActionEvent.ACTION) { event ->
        val button = event.source as Button
        button.text = I18N["common.sending"]
        LogSender.send(Logger.log,
            { button.text = I18N["common.sent"] },
            { button.text = I18N["common.failed"] }
        )
        event.consume()
    }

    return dialog.showAndWait()
}
