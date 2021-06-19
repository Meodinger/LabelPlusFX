package info.meodinger.LabelPlusFX.Component;

import info.meodinger.LabelPlusFX.State;
import info.meodinger.LabelPlusFX.Type.TransLabel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * @author Meodinger
 * Date: 2021/5/29
 * Location: info.meodinger.LabelPlusFX.Component
 */
public class CTreeItem extends TreeItem<String> {

    public final TransLabel meta;

    private final State state;
    private final StringProperty groupName;
    private final StringProperty text;

    public CTreeItem(State state, String groupName, TransLabel meta) {
        this.state = state;
        this.groupName = new SimpleStringProperty(groupName);
        this.text = new SimpleStringProperty(meta.getText());
        this.meta = meta;
        init();
    }

    public CTreeItem(State state, String groupName, TransLabel meta, Node node) {
        this(state, groupName, meta);
        setGraphic(node);
    }

    private void init() {
        setValue(genHead() + ":  " + meta.getText().replaceAll("\n", " "));

        textProperty().addListener((observableValue, oldValue, newValue) -> {
            meta.setText(newValue);
            update();
        });
        groupNameProperty().addListener((observableValue, oldValue, newValue) -> {
            meta.setGroupId(state.getGroupIdByName(newValue));
            update();
        });
    }

    private String genHead() {
        return String.format("%02d", meta.getIndex());
    }

    public void update() {
        setValue(genHead() + ":  " + meta.getText().replaceAll("\n", " "));
        if (state.getViewMode() == State.VIEW_MODE_INDEX){
            setGraphic(new Circle(8, Color.web(state.getGroupColorByName(getGroupName()))));
        }
    }

    public String getText() {
        return textProperty().get();
    }
    public void setText(String text) {
        textProperty().set(text);
    }
    public StringProperty textProperty() {
        return text;
    }

    public String getGroupName() {
        return groupNameProperty().get();
    }
    public void setGroupName(String groupName) {
        groupNameProperty().set(groupName);
    }
    public StringProperty groupNameProperty() {
        return groupName;
    }
}
