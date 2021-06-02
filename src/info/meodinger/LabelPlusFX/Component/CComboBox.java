package info.meodinger.LabelPlusFX.Component;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * @author Meodinger
 * Date: 2021/5/27
 * Location: info.meodinger.LabelPlusFX.Component
 */
public class CComboBox<T> extends HBox {

    private final ComboBox<T> comboBox;
    private final Button back, next;

    private int index;

    public CComboBox() {
        super();

        this.comboBox = new ComboBox<>();
        this.back = new Button("<");
        this.next = new Button(">");
        this.index = 0;

        init();
        render();
    }

    private void init() {
        comboBox.valueProperty().addListener((observableValue, oldValue, newValue) -> index = comboBox.getItems().indexOf(newValue));

        back.setOnMouseClicked(e -> back());
        next.setOnMouseClicked(e -> next());
    }
    private void render() {
        comboBox.setPrefWidth(150);
        back.setTextAlignment(TextAlignment.CENTER);
        next.setTextAlignment(TextAlignment.CENTER);

        getChildren().addAll(comboBox, back, next);
    }
    public void reset() {
        comboBox.getItems().clear();
        comboBox.setValue(null);
        index = 0;
    }

    public void setList(List<T> list) {
        reset();
        comboBox.getItems().addAll(list);
        if (list.size() > 0) comboBox.setValue(comboBox.getItems().get(0));
    }

    public void back() {
        if (index > 0) {
            comboBox.setValue(comboBox.getItems().get(--index));
        }
    }
    public void next() {
        if (index < comboBox.getItems().size() - 1) {
            comboBox.setValue(comboBox.getItems().get(++index));
        }
    }
    public void moveTo(int index) {
        if (index >= 0 && index < comboBox.getItems().size()) {
            comboBox.setValue(comboBox.getItems().get(index));
        }
    }
    public ObjectProperty<T> valueProperty() {
        return comboBox.valueProperty();
    }
}
