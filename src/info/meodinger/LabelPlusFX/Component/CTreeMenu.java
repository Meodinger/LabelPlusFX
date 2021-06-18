package info.meodinger.LabelPlusFX.Component;

import info.meodinger.LabelPlusFX.Config;
import info.meodinger.LabelPlusFX.I18N;
import info.meodinger.LabelPlusFX.Type.TransFile;
import info.meodinger.LabelPlusFX.Type.TransLabel;
import info.meodinger.LabelPlusFX.Util.CColor;
import info.meodinger.LabelPlusFX.Util.CDialog;
import info.meodinger.LabelPlusFX.Util.CString;

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

    public CTreeMenu(Config config) {
        rootMenu = new RootMenu(config);
        groupMenu = new GroupMenu(config);
        labelMenu = new LabelMenu(config);
    }

    public abstract static class TargetMenu extends ContextMenu {

        private final Config config;
        private TreeItem<String> item;

        public TargetMenu(Config config) {
            super();
            this.config = config;
            render();
        }

        public void setItem(TreeItem<String> item) {
            this.item = item;
        }

        public TreeItem<String> getItem() {
            return item;
        }

        public Config getConfig() {
            return config;
        }

        protected abstract void render();
        public abstract void init(TreeItem<String> item);
    }
    public static class RootMenu extends TargetMenu {

        private MenuItem addGroup;

        private Dialog<TransFile.MeoTransFile.Group> dialog;
        private CColorPicker colorPicker;
        private TextField nameField;

        public RootMenu(Config config) {
            super(config);
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
                Config config = getConfig();

                int newGroupId = config.getGroupCount();
                nameField.setText(String.format(I18N.FORMAT_NEW_GROUP_NAME, newGroupId + 1));
                Color color;
                if (newGroupId < 9) {
                    color = Color.web(TransFile.MeoTransFile.DEFAULT_COLOR_LIST[newGroupId]);
                } else {
                    color = Color.WHITE;
                }
                colorPicker.setValue(color);

                dialog.setTitle(I18N.TITLE_ADD_GROUP);
                dialog.setHeaderText(I18N.CONTENT_ADD_GROUP);
                dialog.setResult(null);

                Optional<TransFile.MeoTransFile.Group> result = dialog.showAndWait();
                if (result.isPresent()) {
                    TransFile.MeoTransFile.Group group = result.get();

                    // Edit data
                    config.getGroups().add(group);
                    // Update view
                    config.getControllerAccessor().updateGroupList();
                    config.getControllerAccessor().addLabelLayer();
                    rootItem.getChildren().add(new TreeItem<>(group.name, new Circle(8, Color.web(group.color))));
                    // Mark change
                    config.setChanged(true);
                }
            });
        }
    }
    public static class GroupMenu extends TargetMenu {

        private MenuItem rename, changeColor, delete;
        private CColorPicker picker;

        public GroupMenu(Config config) {
            super(config);
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
                Config config = getConfig();

                Optional<String> result = CDialog.showInput(config.stage, I18N.TITLE_RENAME, I18N.CONTENT_RENAME, groupItem.getValue());

                if (result.isPresent() && !CString.isBlank(result.get())) {
                    String name = result.get().trim().replaceAll(" ", "_");
                    int groupId = config.getGroupIdByName(groupItem.getValue());

                    if (config.isLPFile() && config.getGroupNames().contains(name)) {
                        CDialog.showAlert(I18N.SAME_GROUP_NAME);
                        return;
                    }

                    // Edit data
                    config.getGroupAt(groupId).name = name;
                    // Update view
                    config.getControllerAccessor().updateGroupList();
                    groupItem.setValue(name);
                    // Mark change
                    config.setChanged(true);
                }
            });

            picker.setValue((Color) ((Circle) getItem().getGraphic()).getFill());
            picker.setOnAction(e -> {
                Config config = getConfig();
                Color color = ((ColorPicker) e.getSource()).getValue();
                int groupId = config.getGroupIdByName(getItem().getValue());

                // Edit data
                config.getGroupAt(groupId).color = CColor.toHex(color);
                // Update view
                ((Circle) getItem().getGraphic()).setFill(color);
                config.getControllerAccessor().updateLabelLayer(groupId);
                // Mark change
                config.setChanged(true);

            });
            changeColor.setText(CColor.toHex(picker.getValue()));


            int labelCount = 0, thisGroupId = getConfig().getGroupIdByName(getItem().getValue());
            for (List<TransLabel> labels : getConfig().getTransMap().values()) {
                for (TransLabel label : labels) {
                    if (label.getGroupId() == thisGroupId) labelCount++;
                }
            }
            delete.setDisable(labelCount != 0);
            delete.setOnAction(e -> {
                TreeItem<String> groupItem = getItem();
                Config config = getConfig();
                int groupId = config.getGroupIdByName(groupItem.getValue());

                // Edit data
                for (List<TransLabel> labels : config.getTransMap().values()) for (TransLabel label : labels)
                    if (label.getGroupId() >= groupId) label.setGroupId(label.getGroupId() - 1);
                config.getGroups().remove(groupId);
                // Update view
                config.getControllerAccessor().updateGroupList();
                groupItem.getParent().getChildren().remove(groupItem);
                config.getControllerAccessor().removeLabelLayer(groupId);
                // Mark change
                config.setChanged(true);
            });
        }
    }
    public static class LabelMenu extends TargetMenu {

        private MenuItem editGroup, delete;

        public LabelMenu(Config config) {
            super(config);
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
                Config config = getConfig();
                int prevGroupId = labelItem.meta.getGroupId();
                Optional<String> result = CDialog.showChoice(config.stage, I18N.TITLE_MOVE_TO, I18N.CONTENT_MOVE_TO, config.getGroupNames());

                // Edit data
                result.ifPresent(labelItem::setGroupName);
                // Update view
                config.getControllerAccessor().updateLabelLayer(prevGroupId);
                config.getControllerAccessor().updateLabelLayer(labelItem.meta.getGroupId());
                config.getControllerAccessor().updateTree();
                // Mark change
                config.setChanged(true);
            });

            /*
             * Please also edit `CImagePane#handleLabelMode#SECONDARY`
             */
            delete.setOnAction(e -> {
                CTreeItem labelItem = (CTreeItem) getItem();
                Config config = getConfig();

                Optional<ButtonType> result = CDialog.showConfirm(I18N.TITLE_DELETE_LABEL, I18N.CONTENT_DELETE_LABEL, labelItem.getValue());

                if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.YES) {
                    TransLabel label = labelItem.meta;

                    // Edit data
                    for (TransLabel l : config.getLabelsNow())
                        if (l.getIndex() > label.getIndex()) l.setIndex(l.getIndex() - 1);
                    config.getLabelsNow().remove(label);
                    // Update view
                    config.getControllerAccessor().updatePane();
                    config.getControllerAccessor().updateTree();
                    // Mark change
                    config.setChanged(true);
                }
            });
        }
    }

}
