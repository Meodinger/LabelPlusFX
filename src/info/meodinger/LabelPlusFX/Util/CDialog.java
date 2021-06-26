package info.meodinger.LabelPlusFX.Util;

import info.meodinger.LabelPlusFX.I18N;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * @author Meodinger
 * Date: 2021/5/28
 * Location: info.meodinger.LabelPlusFX
 */
public class CDialog {

    private static final Dialog<ButtonType> dialog = new Dialog<>();

    private static void initDialog() {
        dialog.setTitle("");
        dialog.setContentText("");
        dialog.setDialogPane(new DialogPane());
    }

    public static void initOwner(Window window) {
        dialog.initOwner(window);
    }

    /**
     * Show stack trace in expandable content
     * @param e Exception to print
     * @return ButtonType.OK?
     */
    public static Optional<ButtonType> showException(Exception e) {

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        GridPane expContent = new GridPane();
        Label label = new Label("The exception stacktrace is:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.prefWidthProperty().bind(expContent.widthProperty());
        textArea.prefHeightProperty().bind(expContent.heightProperty());
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        initDialog();
        dialog.setTitle(I18N.ERROR);
        dialog.setHeaderText(e.getClass().toString());
        dialog.setContentText(e.getMessage());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().setExpandableContent(expContent);
        return dialog.showAndWait();
    }

    /**
     * Show information
     * @param info Info to show
     * @return ButtonType.OK?
     */
    public static Optional<ButtonType> showInfo(String info) {
        return showInfo(I18N.INFO, info);
    }
    /**
     * Show information with specific title
     * @param title Dialog title
     * @param info Info to show
     * @return ButtonType.OK?
     */
    public static Optional<ButtonType> showInfo(String title, String info) {
        initDialog();
        dialog.setTitle(title);
        dialog.setContentText(info);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        return dialog.showAndWait();
    }
    /**
     * Show information with specific title and link
     * @param title Dialog title
     * @param info Info to show
     * @param linkText Text for hyperlink
     * @param handler Handler for action of link
     * @return ButtonType.OK?
     */
    public static Optional<ButtonType> showInfoWithLink(String title, String info, String linkText, EventHandler<ActionEvent> handler) {
        initDialog();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        Label label = new Label(info);
        Separator separator = new Separator();
        Hyperlink hyperlink = new Hyperlink(linkText);

        separator.setPadding(new Insets(8 , 0, 8, 0));
        hyperlink.setOnAction(handler);

        VBox content = new VBox(label, separator, hyperlink);
        dialog.getDialogPane().setContent(content);

        return dialog.showAndWait();
    }

    /**
     * Show alert
     * @param alert Alert to show
     * @return ButtonType? with ButtonDate YES | NO
     */
    public static Optional<ButtonType> showAlert(String alert) {
        return showAlert(I18N.ALERT, alert);
    }
    /**
     * Show alert with specific title
     * @param title Dialog title
     * @param alert Alert to show
     * @return ButtonType? with ButtonDate YES | NO
     */
    public static Optional<ButtonType> showAlert(String title, String alert) {
        return showAlert(title, alert, I18N.YES, I18N.NO);
    }
    /**
     * Show alert with specific title and yes/no text
     * @param title Dialog title
     * @param alert Alert to show
     * @param yes Text for ButtonType with ButtonData.YES
     * @param no  Text for ButtonType with ButtonData.NO
     * @return ButtonType? with ButtonDate YES | NO
     */
    public static Optional<ButtonType> showAlert(String title, String alert, String yes, String no) {
        initDialog();
        dialog.setTitle(title);
        dialog.setContentText(alert);
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType(yes, ButtonBar.ButtonData.YES),
                new ButtonType(no, ButtonBar.ButtonData.NO)
        );
        return dialog.showAndWait();
    }

    /**
     * Show message for confirm
     * @param msg Message to show
     * @return ButtonType?.YES | NO
     */
    public static Optional<ButtonType> showConfirm(String msg) {
        return showConfirm(I18N.CONFIRM, msg);
    }
    /**
     * Show message for confirm with specific title
     * @param title Dialog title
     * @param msg Message to show
     * @return ButtonType?.YES | NO
     */
    public static Optional<ButtonType> showConfirm(String title, String msg) {
        return showConfirm(title, null, msg);
    }
    /**
     * Show message for confirm
     * @param title Dialog title
     * @param header Dialog header text
     * @param msg Message to show
     * @return ButtonType?.YES | NO
     */
    public static Optional<ButtonType> showConfirm(String title, String header, String msg) {
        initDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(msg);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        return dialog.showAndWait();
    }

    /**
     *  For specific usage
     */

    public static <T> Optional<T> showChoice(Window owner, String title, String msg, List<T> choices) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setContentText(msg);

        return dialog.showAndWait();
    }

    public static Optional<String> showInput(Window owner, String title, String msg, String placeholder) {
        TextInputDialog dialog = new TextInputDialog(placeholder);
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(msg);

        return dialog.showAndWait();
    }

    public static Optional<String> showInputArea(Window owner, String title, String placeholder) {
        Dialog<String> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);

        TextArea textArea = new TextArea(placeholder);
        dialog.getDialogPane().setContent(new HBox(textArea));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) return textArea.getText();
            return placeholder;
        });

        return dialog.showAndWait();
    }

    public static <T> Optional<List<T>> showListChoose(Window owner, String title, List<T> list) {
        Dialog<List<T>> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);

        ListView<T> left = new ListView<>();
        ListView<T> right = new ListView<>();
        left.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        right.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        left.getItems().addAll(list);

        Button add = new Button(I18N.ADD_PIC);
        Button addAll = new Button(I18N.ADD_ALL_PIC);
        Button remove = new Button(I18N.REMOVE_PIC);
        Button removeAll = new Button(I18N.REMOVE_ALL_PIC);
        VBox vBox = new VBox(add, addAll, remove, removeAll);
        vBox.setAlignment(Pos.CENTER);
        for (Node b : vBox.getChildren()) {
            Button btn = (Button) b;
            btn.setPrefWidth(48);
            btn.setPrefHeight(48);
        }

        add.setOnAction(event -> {
            List<T> selectedItems = new ArrayList<>(left.getSelectionModel().getSelectedItems());
            for (T item: selectedItems) {
                right.getItems().add(item);
                left.getItems().remove(item);
            }
        });
        addAll.setOnAction(event -> {
            right.getItems().addAll(left.getItems());
            left.getItems().clear();
        });
        remove.setOnAction(event -> {
            List<T> selectedItems = new ArrayList<>(left.getSelectionModel().getSelectedItems());
            for (T item: selectedItems) {
                left.getItems().add(item);
                right.getItems().remove(item);
            }
        });
        removeAll.setOnAction(event -> {
            left.getItems().addAll(right.getItems());
            right.getItems().clear();
        });

        GridPane pane = new GridPane();
        pane.add(new Label(I18N.PICS_POTENTIAL), 0, 0);
        pane.add(new Label(I18N.PICS_SELECTED), 2, 0);
        pane.add(left, 0, 1);
        pane.add(vBox, 1, 1);
        pane.add(right, 2, 1);
        pane.setHgap(20);
        pane.setPrefWidth(600);
        pane.setPrefHeight(400);

        dialog.getDialogPane().setContent(pane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        dialog.setResultConverter(type -> new ArrayList<>(right.getItems()));

        return dialog.showAndWait();
    }
}