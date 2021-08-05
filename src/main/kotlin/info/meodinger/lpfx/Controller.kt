package info.meodinger.lpfx

import info.meodinger.lpfx.component.CComboBox
import info.meodinger.lpfx.component.CFileChooser
import info.meodinger.lpfx.component.CLabelPane
import info.meodinger.lpfx.component.CTextSlider
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get
import javafx.application.Platform.runLater

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.stage.FileChooser
import java.net.URL
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */
class Controller : Initializable {

    @FXML private lateinit var tTransText: TextArea
    @FXML private lateinit var bSwitchViewMode: Button
    @FXML private lateinit var bSwitchWorkMode: Button
    @FXML private lateinit var pMain: SplitPane
    @FXML private lateinit var pRight: SplitPane
    @FXML private lateinit var pText: AnchorPane
    @FXML private lateinit var vTree: TreeView<String>
    @FXML private lateinit var cSlider: CTextSlider
    @FXML private lateinit var cPicBox: CComboBox<String>
    @FXML private lateinit var cGroupBox: CComboBox<String>
    @FXML private lateinit var cLabelPane: CLabelPane
    @FXML private lateinit var mmFile: Menu
    @FXML private lateinit var mNew: MenuItem
    @FXML private lateinit var mOpen: MenuItem
    @FXML private lateinit var mOpenRecent: Menu
    @FXML private lateinit var mSave: MenuItem
    @FXML private lateinit var mSaveAs: MenuItem
    @FXML private lateinit var mBakRecover: MenuItem
    @FXML private lateinit var mClose: MenuItem
    @FXML private lateinit var mmExport: Menu
    @FXML private lateinit var mExportAsLp: MenuItem
    @FXML private lateinit var mExportAsMeo: MenuItem
    @FXML private lateinit var mExportAsMeoPack: MenuItem
    @FXML private lateinit var mEditComment: MenuItem
    @FXML private lateinit var mmAbout: Menu
    @FXML private lateinit var mAbout: MenuItem

    private val fileFilter = FileChooser.ExtensionFilter(I18N["filetype.translation"], "*${EXTENSION_MEO}", "*${EXTENSION_LP}")
    private val meoFilter = FileChooser.ExtensionFilter(I18N["filetype.translation_meo"], "*${EXTENSION_MEO}")
    private val lpFilter = FileChooser.ExtensionFilter(I18N["filetype.translation_lp"], "*${EXTENSION_LP}")
    private val bakFilter = FileChooser.ExtensionFilter(I18N["filetype.bak"], "*${EXTENSION_BAK}")
    private val packFilter = FileChooser.ExtensionFilter(I18N["filetype.pack"], "*${EXTENSION_PACK}")
    private val fileChooser = CFileChooser()
    private val bakChooser = CFileChooser()
    private val exportChooser = CFileChooser()
    private val exportPackChooser = CFileChooser()

    private val symbolMenu = object : ContextMenu() {

        private val radius = 6.0
        private val symbols = listOf(
            Pair("※", true),
            Pair("◎", true),
            Pair("★", true),
            Pair("☆", true),
            Pair("～", true),
            Pair("♡", false),
            Pair("♥", false),
            Pair("♢", false),
            Pair("♦", false),
            Pair("♪", false)
        )

        fun createSymbolItem(symbol: String, displayable: Boolean): MenuItem {
            return MenuItem(
                symbol,
                if (displayable) Circle(radius, Color.GREEN)
                else Circle(radius, Color.RED)
            ).also {
                it.style = "-fx-font-family: \"Segoe UI Symbol\""
            }
        }

        init {
            for (symbol in symbols) items.add(createSymbolItem(symbol.first, symbol.second).also {
                it.setOnAction { tTransText.insertText(tTransText.caretPosition, symbol.first) }
            })
        }
    }

    private val timer = Timer()
    private val task = object : TimerTask() {
        override fun run() {
            if (State.isChanged) {
                this@Controller.silentBackup()
            }
        }
    }

    init {

    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
    }

    private fun setText() {}
    private fun setDisable(isDisable: Boolean) {}
    private fun disable() {}
    private fun enable() {}

    private fun reset() {}
    private fun updateGroupList() {}
    private fun updateTreeView() {}
    private fun updateTreeViewByGroup() {}
    private fun updateTreeViewByIndex() {}

    private fun updateRecentFiles() {}

    private fun silentBackup() {}

    private fun prepare() {}
    private fun stay() {}

    private fun new(path: String) {}
    private fun open(path: String) {}
    private fun save(path: String, isSilent: Boolean) {}

    // new & open
    @FXML fun newTranslation() {}
    // open
    @FXML fun openTranslation() {}
    // save
    @FXML fun saveTranslation() {}
    // save
    @FXML fun saveAsTranslation() {}
    // open & save
    @FXML fun bakRecovery() {}

    @FXML fun close() {}

    @FXML fun exportTransFile() {}
    @FXML fun exportTransPack() {}

    @FXML fun setComment() {}

    @FXML fun about() {
    }

    @FXML fun switchViewMode() {}
    @FXML fun switchWorkMode() {}


}