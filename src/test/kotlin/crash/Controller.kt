import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import java.net.URL
import java.util.*

/**
 * Author: Meodinger
 * Date: 2021/8/23
 * Location:
 */
object Controller : Initializable {

    @FXML private lateinit var button: Button
    @FXML fun buttonAction() {
        println("Clicked")
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        button.text = "Click Me"
    }
}