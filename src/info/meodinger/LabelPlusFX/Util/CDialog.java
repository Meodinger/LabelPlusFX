package info.meodinger.LabelPlusFX.Util;

import info.meodinger.LabelPlusFX.I18N;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

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
        dialog.getDialogPane().getButtonTypes().add(new ButtonType(I18N.OK, ButtonBar.ButtonData.OK_DONE));
        dialog.getDialogPane().setExpandableContent(expContent);
        return dialog.showAndWait();
    }

    public static Optional<ButtonType> showInfo(String info) {
        return showInfo(I18N.INFO, info);
    }
    public static Optional<ButtonType> showInfo(String title, String info) {
        initDialog();
        dialog.setTitle(title);
        dialog.setContentText(info);
        dialog.getDialogPane().getButtonTypes().add(new ButtonType(I18N.OK, ButtonBar.ButtonData.OK_DONE));
        return dialog.showAndWait();
    }

    public static Optional<ButtonType> showAlert(String alert) {
        return showAlert(I18N.ALERT, alert);
    }
    public static Optional<ButtonType> showAlert(String title, String alert) {
        return showAlert(title, alert, I18N.YES, I18N.NO);
    }
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

    public static Optional<ButtonType> showConfirm(String msg) {
        return showConfirm(I18N.CONFIRM, msg);
    }
    public static Optional<ButtonType> showConfirm(String title, String msg) {
        return showConfirm(title, null, msg);
    }
    public static Optional<ButtonType> showConfirm(String title, String header, String msg) {
        initDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(msg);
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType(I18N.YES, ButtonBar.ButtonData.YES),
                new ButtonType(I18N.NO, ButtonBar.ButtonData.NO)
        );
        return dialog.showAndWait();
    }

    public static <T> Optional<T> showChoice(String title, String msg, List<T> choices) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle(title);
        dialog.setContentText(msg);

        return dialog.showAndWait();
    }

    public static Optional<String> showInput(String title, String msg, String placeholder) {
        TextInputDialog dialog = new TextInputDialog(placeholder);
        dialog.setTitle(title);
        dialog.setHeaderText(msg);

        return dialog.showAndWait();
    }

    public static <T> Optional<List<T>> showListChoose(String title, List<T> list) {
        Dialog<List<T>> dialog = new Dialog<>();
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
            btn.setPrefWidth(40);
            btn.setPrefHeight(40);
        }
        add.setOnAction(event -> {
            ObservableList<T> selectedItems = left.getSelectionModel().getSelectedItems();
            for (T s: selectedItems) {
                left.getItems().remove(s);
                right.getItems().add(s);
            }
        });
        addAll.setOnAction(event -> {
            right.getItems().addAll(left.getItems());
            left.getItems().clear();
        });
        remove.setOnAction(event -> {
            ObservableList<T> selectedItems = right.getSelectionModel().getSelectedItems();
            for (T s: selectedItems) {
                right.getItems().remove(s);
                left.getItems().add(s);
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