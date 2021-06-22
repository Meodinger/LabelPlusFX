package info.meodinger.LabelPlusFX.Component;

import info.meodinger.LabelPlusFX.State;
import info.meodinger.LabelPlusFX.I18N;
import info.meodinger.LabelPlusFX.Type.TransFile;
import info.meodinger.LabelPlusFX.Type.TransLabel;
import info.meodinger.LabelPlusFX.Util.CColor;
import info.meodinger.LabelPlusFX.Util.CDialog;
import info.meodinger.LabelPlusFX.Util.CString;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.Optional;

/**
 * @author Meodinger
 * Date: 2021/5/29
 * Location: info.meodinger.LabelPlusFX.Component
 */
public class CTreeMenu {

    public final TargetMenu rootMenu;
    public final TargetMenu groupMenu;
    public final TargetMenu labelMenu;
    public final TreeMenu treeMenu;

    public CTreeMenu(State state) {
        rootMenu = new RootMenu(state);
        groupMenu = new GroupMenu(state);
        labelMenu = new LabelMenu(state);
        treeMenu = new TreeMenu(state);
    }

    public abstract static class TargetMenu extends ContextMenu {

        private final State state;
        private TreeItem<String> item;

        public TargetMenu(State state) {
            super();
            this.state = state;
            render();
        }

        public void setItem(TreeItem<String> item) {
            this.item = item;
        }

        public TreeItem<String> getItem() {
            return item;
        }

        public State getState() {
            return state;
        }

        protected abstract void render();
        public abstract void init(TreeItem<String> item);
    }
    public static class RootMenu extends TargetMenu {

        private MenuItem addGroup;

        private Dialog<TransFile.MeoTransFile.Group> dialog;
        private CColorPicker colorPicker;
        private TextField nameField;

        public RootMenu(State state) {
            super(state);
        }

        @Override
        protected void render() {
            addGroup = new MenuItem(I18N.MENU_ITEM_ADD_GROUP);
            getItems().add(addGroup);

            colorPicker = new CColorPicker();
            nameField = new TextField();
            nameField.setTextFormatter(new TextFormatter<String>(change -> {
                change.setText(change.getText().trim().replaceAll(" ", "_"));
                return change;
            }));

            dialog = new Dialog<>();
            dialog.getDialogPane().getButtonTypes().addAll(
                    new ButtonType(I18N.SUBMIT, ButtonBar.ButtonData.OK_DONE),
                    new ButtonType(I18N.CANCEL, ButtonBar.ButtonData.CANCEL_CLOSE)
            );
            HBox hBox = new HBox(nameField, colorPicker);
            hBox.setAlignment(Pos.CENTER);
            dialog.getDialogPane().setContent(hBox);
            dialog.setResultConverter((type) -> {
                if (type.getButtonData() == ButtonBar.ButtonData.OK_DONE){
                    return new TransFile.MeoTransFile.Group(
                            nameField.getText().trim().replaceAll(" ", "_"),
                            CColor.toHex(colorPicker.getValue())
                    );
                }
                return null;
            });
            colorPicker.hide();
        }

        public void init(TreeItem<String> item) {
            setItem(item);

            if (item == null) {
                addGroup.setOnAction(null);
                return;
            }

            addGroup.setOnAction(e -> {
                TreeItem<String> rootItem = getItem();
                State state = getState();

                int newGroupId = state.getGroupCount();
                nameField.setText(String.format(I18N.FORMAT_NEW_GROUP_NAME, newGroupId + 1));
                Color color;
                if (newGroupId < 9) {
                    color = Color.web(TransFile.MeoTransFile.DEFAULT_COLOR_LIST[newGroupId]);
                } else {
                    color = Color.WHITE;
                }
                colorPicker.setValue(color);

                dialog.setTitle(I18N.DIALOG_TITLE_ADD_GROUP);
                dialog.setHeaderText(I18N.DIALOG_CONTENT_ADD_GROUP);
                dialog.setResult(null);

                Optional<TransFile.MeoTransFile.Group> result = dialog.showAndWait();
                if (result.isPresent()) {
                    TransFile.MeoTransFile.Group group = result.get();

                    // Edit data
                    state.getGroups().add(group);
                    // Update view
                    state.getControllerAccessor().updateGroupList();
                    state.getControllerAccessor().addLabelLayer();
                    rootItem.getChildren().add(new TreeItem<>(group.name, new Circle(8, Color.web(group.color))));
                    // Mark change
                    state.setChanged(true);
                }
            });
        }
    }
    public static class GroupMenu extends TargetMenu {

        private MenuItem rename, changeColor, delete;
        private CColorPicker picker;

        public GroupMenu(State state) {
            super(state);
        }

        @Override
        protected void render() {
            rename = new MenuItem(I18N.MENU_ITEM_RENAME);
            changeColor = new MenuItem();
            delete = new MenuItem(I18N.MENU_ITEM_DELETE);

            getItems().add(rename);
            getItems().add(changeColor);
            getItems().add(new SeparatorMenuItem());
            getItems().add(delete);

            picker = new CColorPicker();
            picker.valueProperty().addListener((observable, oldValue, newValue) -> changeColor.setText(CColor.toHex(newValue)));
            picker.setPrefSize(40, 20);
            changeColor.setGraphic(picker);
        }

        @Override
        public void init(TreeItem<String> item) {
            setItem(item);

            if (item == null) {
                rename.setOnAction(null);
                picker.setOnAction(null);
                delete.setOnAction(null);
                return;
            }

            rename.setOnAction(e -> {
                TreeItem<String> groupItem = getItem();
                State state = getState();

                Optional<String> result = CDialog.showInput(state.stage, I18N.DIALOG_TITLE_RENAME, I18N.DIALOG_CONTENT_RENAME, groupItem.getValue());

                if (result.isPresent() && !CString.isBlank(result.get())) {
                    String name = result.get().trim().replaceAll(" ", "_");
                    int groupId = state.getGroupIdByName(groupItem.getValue());

                    if (state.isLPFile() && state.getGroupNames().contains(name)) {
                        CDialog.showAlert(I18N.EXPORTER_SAME_GROUP_NAME);
                        return;
                    }

                    // Edit data
                    state.getGroupAt(groupId).name = name;
                    // Update view
                    state.getControllerAccessor().updateGroupList();
                    groupItem.setValue(name);
                    // Mark change
                    state.setChanged(true);
                }
            });

            picker.setValue((Color) ((Circle) getItem().getGraphic()).getFill());
            picker.setOnAction(e -> {
                State state = getState();
                Color color = ((ColorPicker) e.getSource()).getValue();
                int groupId = state.getGroupIdByName(getItem().getValue());

                // Edit data
                state.getGroupAt(groupId).color = CColor.toHex(color);
                // Update view
                ((Circle) getItem().getGraphic()).setFill(color);
                state.getControllerAccessor().updateLabelLayer(groupId);
                // Mark change
                state.setChanged(true);

            });
            changeColor.setText(CColor.toHex(picker.getValue()));


            int labelCount = 0, thisGroupId = getState().getGroupIdByName(getItem().getValue());
            for (List<TransLabel> labels : getState().getTransMap().values()) {
                for (TransLabel label : labels) {
                    if (label.getGroupId() == thisGroupId) labelCount++;
                }
            }
            delete.setDisable(labelCount != 0);
            delete.setOnAction(e -> {
                TreeItem<String> groupItem = getItem();
                State state = getState();
                int groupId = state.getGroupIdByName(groupItem.getValue());

                // Edit data
                for (List<TransLabel> labels : state.getTransMap().values()) for (TransLabel label : labels)
                    if (label.getGroupId() >= groupId) label.setGroupId(label.getGroupId() - 1);
                state.getGroups().remove(groupId);
                // Update view
                groupItem.getParent().getChildren().remove(groupItem);
                state.getControllerAccessor().updateGroupList();
                state.getControllerAccessor().removeLabelLayer(groupId);
                // Mark change
                state.setChanged(true);
            });
        }
    }
    public static class LabelMenu extends TargetMenu {

        private MenuItem editGroup, delete;

        public LabelMenu(State state) {
            super(state);
        }

        @Override
        protected void render() {
            editGroup = new MenuItem(I18N.MENU_ITEM_MOVE_TO);
            delete = new MenuItem(I18N.MENU_ITEM_DELETE);

            getItems().add(editGroup);
            getItems().add(new SeparatorMenuItem());
            getItems().add(delete);
        }

        @Override
        public void init(TreeItem<String> item) {
            setItem(item);

            if (item == null) {
                editGroup.setOnAction(null);
                delete.setOnAction(null);
                return;
            }

            editGroup.setOnAction(e -> {
                CTreeItem labelItem = (CTreeItem) getItem();
                State state = getState();
                int prevGroupId = labelItem.meta.getGroupId();
                Optional<String> result = CDialog.showChoice(state.stage, I18N.DIALOG_TITLE_MOVE_TO, I18N.DIALOG_CONTENT_MOVE_TO, state.getGroupNames());

                if (result.isPresent()) {
                    // Edit data
                    labelItem.setGroupName(result.get());
                    // Update view
                    state.getControllerAccessor().updateLabelLayer(prevGroupId);
                    state.getControllerAccessor().updateLabelLayer(labelItem.meta.getGroupId());
                    state.getControllerAccessor().updateTree();
                    // Mark change
                    state.setChanged(true);
                }
            });

            /*
             * Please also edit `CImagePane#handleLabelMode#SECONDARY`
             */
            delete.setOnAction(e -> {
                CTreeItem labelItem = (CTreeItem) getItem();
                State state = getState();

                Optional<ButtonType> result = CDialog.showConfirm(I18N.DIALOG_TITLE_DELETE_LABEL, I18N.DIALOG_CONTENT_DELETE_LABEL, labelItem.getValue());

                if (result.isPresent() && result.get() == ButtonType.YES) {

                    // Edit data
                    for (TransLabel l : state.getLabelsNow())
                        if (l.getIndex() > labelItem.meta.getIndex()) l.setIndex(l.getIndex() - 1);
                    state.getLabelsNow().remove(labelItem.meta);
                    // Update view
                    state.getControllerAccessor().updateLabelLayer(labelItem.meta.getGroupId()); // Also update positions
                    state.getControllerAccessor().updateTree();
                    // Mark change
                    state.setChanged(true);
                }
            });
        }
    }

    public static class TreeMenu extends ContextMenu {

        private final State state;
        private MenuItem editGroup, delete;
        private TreeView<String> view;

        public TreeMenu(State state) {
            super();
            this.state = state;
            render();
        }

        private void render() {
            editGroup = new MenuItem(I18N.MENU_ITEM_MOVE_TO);
            delete = new MenuItem(I18N.MENU_ITEM_DELETE);

            getItems().add(editGroup);
            getItems().add(new SeparatorMenuItem());
            getItems().add(delete);
        }

        public void init(TreeView<String> treeView) {
            view = treeView;

            editGroup.setOnAction(e -> {
                ObservableList<TreeItem<String>> selectedItems = view.getSelectionModel().getSelectedItems();

                Optional<String> result = CDialog.showChoice(state.stage, I18N.DIALOG_TITLE_MOVE_TO, I18N.DIALOG_CONTENT_MOVE_TO, state.getGroupNames());

                // Edit data
                if (result.isPresent()) {
                    String name = result.get();
                    for (TreeItem<String> item : selectedItems) {
                        ((CTreeItem) item).setGroupName(name);
                    }
                }
                // Update view
                state.getControllerAccessor().updatePane();
                state.getControllerAccessor().updateTree();
                // Mark change
                state.setChanged(true);
            });

            delete.setOnAction(e -> {
                ObservableList<TreeItem<String >> selectedItems = view.getSelectionModel().getSelectedItems();

                Optional<ButtonType> result = CDialog.showConfirm(I18N.DIALOG_TITLE_DELETE_LABEL, I18N.DIALOG_CONTENT_DELETE_LABELS);

                if (result.isPresent() && result.get() == ButtonType.YES) {
                    // Edit data
                    for (TreeItem<String> item : selectedItems) {
                        TransLabel label = ((CTreeItem) item).meta;
                        for (TransLabel l : state.getLabelsNow())
                            if (l.getIndex() > label.getIndex()) l.setIndex(l.getIndex() - 1);
                        state.getLabelsNow().remove(label);
                    }
                    // Update view
                    state.getControllerAccessor().updatePane(); // Also update positions
                    state.getControllerAccessor().updateTree();
                    // Mark change
                    state.setChanged(true);
                }
            });
        }

        public void update() {
            editGroup.setDisable(true);
            delete.setDisable(true);

            ObservableList<TreeItem<String>> selectedItems = view.getSelectionModel().getSelectedItems();

            if (selectedItems.size() == 0) return;
            for (TreeItem<String> item : selectedItems) {
                if (item.getClass() != CTreeItem.class) {
                    return;
                }
            }

            editGroup.setDisable(false);
            delete.setDisable(false);
        }
    }


}
