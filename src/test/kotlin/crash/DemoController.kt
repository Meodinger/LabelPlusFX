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
object DemoController : Initializable {

    init {
        println("Controller init {}")
    }

    @FXML lateinit var button: Button
    @FXML fun buttonAction() {
        println("Clicked")
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        println("fun initialize")
        button.text = "Click Me"
    }
}