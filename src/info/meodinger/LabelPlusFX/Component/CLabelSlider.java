package info.meodinger.LabelPlusFX.Component;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;

/**
 * @author Meodinger
 * Date: 2021/5/27
 * Location: info.meodinger.LabelPlusFX.Component
 */
public class CLabelSlider extends HBox {

    private final Slider slider;
    private final Label label;

    public CLabelSlider() {
        super();

        this.slider = new Slider();
        this.label = new Label();

        init();
        render();
    }

    public void init() {
        scaleProperty().addListener((observable, oldValue, newValue) -> label.setText(Math.round(((Double) newValue * 100)) + "%"));
    }

    public void render() {
        setAlignment(Pos.CENTER);

        getChildren().addAll(slider, label);
    }

    public double getMinScale() {
        return minScaleProperty().get();
    }
    public void setMinScale(double minScale) {
        if (minScale >= 0 && minScale < getMaxScale()) {
            minScaleProperty().set(minScale);
        }
    }
    public DoubleProperty minScaleProperty() {
        return slider.minProperty();
    }

    public double getMaxScale() {
        return maxScaleProperty().get();
    }
    public void setMaxScale(double maxScale) {
        if (maxScale > 0 && maxScale > getMinScale()) {
            maxScaleProperty().set(maxScale);
        }
    }
    public DoubleProperty maxScaleProperty() {
        return slider.maxProperty();
    }

    public double getScale() {
        return scaleProperty().get();
    }
    public void setScale(double scale) {
        if (scale >= getMinScale() && scale <= getMaxScale()) {
            scaleProperty().set(scale);
        }
    }
    public DoubleProperty scaleProperty() {
        return slider.valueProperty();
    }
}
