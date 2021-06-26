package info.meodinger.LabelPlusFX.Component;

import info.meodinger.LabelPlusFX.Util.CColor;
import info.meodinger.LabelPlusFX.Util.CString;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ColorPickerSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Meodinger
 * Date: 2021/5/28
 * Location: info.meodinger.LabelPlusFX.Component
 */
public class CColorPicker extends ColorPicker {

    private final TextField colorHexField;

    public CColorPicker() {
        super();
        this.colorHexField = new TextField();
        init();
    }

    public CColorPicker(Color color) {
        this();
        setValue(color);
    }

    private void init() {

        colorHexField.setTextFormatter(new TextFormatter<String>(change -> {
            change.setText(change.getText().toUpperCase());
            if (change.isAdded()) {
                StringBuilder builder = new StringBuilder();
                for (char c : change.getText().toCharArray()) {
                    if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                        builder.append(c);
                    }
                }
                change.setText(builder.toString());
            }
            return change;
        }));
        colorHexField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.fireEvent(new ActionEvent());
                this.hide();
                // Though enter pressed and picker hide, but if not invoke hide(), picker will not show again
            }
        });

        setOnShown(event -> colorHexProperty().set(CColor.toHex(getValue())));
        setOnHidden(event -> colorHexProperty().set(CColor.toHex(getValue())));

        colorHexProperty().addListener((observable, oldValue, newValue) -> {
            String colorHex = newValue;
            int length = colorHex.length();

            switch (length) {
                case 1:
                    colorHex = CString.repeat(colorHex, 6);
                    break;
                case 3:
                    StringBuilder builder = new StringBuilder();
                    for (char c : colorHex.toCharArray()) {
                        builder.append(CString.repeat(c, 2));
                    }
                    colorHex = builder.toString();
                    break;
            }
            length = colorHex.length();

            if (length == 6 || length == 9) {
                setValue(Color.web(colorHex));
            }
        });
    }

    public StringProperty colorHexProperty() {
        return colorHexField.textProperty();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CustomColorPickerSkin(this);
    }

    private class CustomColorPickerSkin extends ColorPickerSkin {

        public CustomColorPickerSkin(ColorPicker control) {
            super(control);
        }

        @Override
        protected Node getPopupContent() {
            // This is an instance of private API ColorPalette which extends Region
            Node colorPalette = super.getPopupContent();
            Region region = (Region) colorPalette;

            // This ColorPalette contains a VBox which contains the Hyperlink we want to remove.
            List<Node> nVBoxes = region.getChildrenUnmodifiable().stream().filter(e -> e instanceof VBox).collect(Collectors.toList());
            for (Node n : nVBoxes) {
                VBox vbox = (VBox) n;
                List<Node> hyperlinks = vbox.getChildren().stream().filter(e -> e instanceof Hyperlink).collect(Collectors.toList());

                vbox.getChildren().removeAll(hyperlinks); // Remove the hyperlink
                if (hyperlinks.size() > 0) {
                    vbox.setAlignment(Pos.CENTER);

                    vbox.getChildren().add(colorHexField);
                }
            }
            return colorPalette;
        }

    }

}
