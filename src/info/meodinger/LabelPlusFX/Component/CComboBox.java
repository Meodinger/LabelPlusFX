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
    private boolean isWrapped;

    public CComboBox() {
        super();

        this.comboBox = new ComboBox<>();
        this.back = new Button("<");
        this.next = new Button(">");

        this.index = 0;
        this.isWrapped = false;

        init();
        render();
    }

    private void init() {
        comboBox.valueProperty().addListener((observableValue, oldValue, newValue) -> index = comboBox.getItems().indexOf(newValue));

        back.setOnMouseClicked(e -> prev());
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
    public int getListSize() {
        return comboBox.getItems().size();
    }

    public void setWrapped(boolean wrapped) {
        isWrapped = wrapped;
    }
    public boolean isWrapped() {
        return isWrapped;
    }

    public void prev() {
        if (isWrapped) {
            if (index <= 0) index += getListSize();
        }
        if (index > 0) {
            comboBox.setValue(comboBox.getItems().get(--index));
        }
    }
    public void next() {
        if (isWrapped) {
            if (index >= getListSize() - 1) index -= getListSize();
        }
        if (index < getListSize() - 1) {
            comboBox.setValue(comboBox.getItems().get(++index));
        }
    }
    public void moveTo(int index) {
        if (index >= 0 && index < getListSize()) {
            comboBox.setValue(comboBox.getItems().get(index));
        }
    }
    public ObjectProperty<T> valueProperty() {
        return comboBox.valueProperty();
    }
}
