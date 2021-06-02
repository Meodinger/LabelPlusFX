package info.meodinger.LabelPlusFX.Component;

import info.meodinger.LabelPlusFX.Config;
import javafx.geometry.Pos;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

/**
 * Author: Meodinger
 * Date: 2021/6/1
 * Location: info.meodinger.LabelPlusFX.Component
 */
public class CTreeCell extends TreeCell<String> {

    private final Config config;
    private final CTreeMenu menu;

    public CTreeCell(Config config, CTreeMenu menu) {
        super();
        this.config = config;
        this.menu = menu;
        init();
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setAlignment(Pos.CENTER_LEFT);
            setGraphic(getTreeItem().getGraphic());
            setText(getTreeItem().getValue());
        }
    }

    public void init() {
        setOnMouseClicked(event -> {
            TreeItem<String> i = getTreeItem();

            if (i != null) {
                if (i.getClass() == CTreeItem.class) {
                    menu.labelMenu.init(i);
                    setContextMenu(menu.labelMenu);
                } else if (i.getParent() == null) {
                    if (config.getViewMode() == Config.VIEW_MODE_GROUP) {
                        menu.rootMenu.init(i);
                        setContextMenu(menu.rootMenu);
                    } else {
                        menu.rootMenu.init(null);
                        setContextMenu(null);
                    }
                } else {
                    menu.groupMenu.init(i);
                    setContextMenu(menu.groupMenu);
                }
            } else {
                menu.rootMenu.init(null);
                menu.groupMenu.init(null);
                menu.labelMenu.init(null);
                setContextMenu(null);
            }
        });
    }
}
