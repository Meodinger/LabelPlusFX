package info.meodinger.lpfx

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import java.net.URL
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx
 */
class Controller : Initializable {

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

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        //TODO: fill
    }

    @FXML fun newTranslation() {}
    @FXML fun openTranslation() {}
    @FXML fun saveTranslation() {}
    @FXML fun saveAsTranslation() {}
    @FXML fun bakRecovery() {}
    @FXML fun close() {}

    @FXML fun exportTransFile() {}
    @FXML fun exportTransPack() {}

    @FXML fun setComment() {}

    @FXML fun about() {}


}