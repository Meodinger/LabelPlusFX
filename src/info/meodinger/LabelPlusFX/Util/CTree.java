package info.meodinger.LabelPlusFX.Util;

import javafx.scene.control.TreeItem;

/**
 * @author Meodinger
 * Date: 2021/5/29
 * Location: info.meodinger.LabelPlusFX.Util
 */
public class CTree {

    public static <T> TreeItem<T> getRootOf(TreeItem<T> item) {
        if (item == null) return null;

        TreeItem<T> root = item;
        while (root.getParent() != null) root = root.getParent();
        return root;
    }

    public static void expandAll(TreeItem<?> item) {
        if (item == null) return;

        if (item.getChildren().size() > 0) {
            item.setExpanded(true);
            for (TreeItem<?> i : item.getChildren()) {
                expandAll(i);
            }
        }
    }
}
