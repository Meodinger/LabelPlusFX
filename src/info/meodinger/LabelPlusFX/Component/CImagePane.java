package info.meodinger.LabelPlusFX.Component;

import info.meodinger.LabelPlusFX.Config;
import info.meodinger.LabelPlusFX.Type.TransLabel;
import info.meodinger.LabelPlusFX.Util.CColor;
import info.meodinger.LabelPlusFX.Util.CDialog;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Author: Meodinger
 * Date: 2021/6/1
 * Location: info.meodinger.LabelPlusFX.Component
 *
 *           |   Layout   | Width
 * -----------------------------
 * pane      | -          | actual width
 * container | 0 - Fixed  | ?
 * image     | left-top   | image width
 * root      | with image | image width
 * layer     | with image | image width
 */
public class CImagePane extends ScrollPane {

    private final static int LABEL_RADIUS = 40;
    private final static int DISPLAY_INSET = 10;
    private final static int SHIFT = 20;
    private final static double LINE_HEIGHT_RATIO = 0.5;

    private final static int LABEL_FONT_SIZE = 32;
    private final static javafx.scene.text.Font LABEL_FONT = new javafx.scene.text.Font(LABEL_FONT_SIZE);

    private final static int DISPLAY_FONT_SIZE = 28;
    private final static javafx.scene.text.Font DISPLAY_FONT = new Font(DISPLAY_FONT_SIZE);

    public final static int TEXT_LAYER = -1;
    public final static int NOT_FOUND = -1;

    /**
     * For contain, event handle,
     */
    private final AnchorPane container;
    /**
     * For display, scale, drag, locate label
     */
    private final StackPane root;
    private final ImageView view;
    private final List<Canvas> layers;
    private final Canvas textLayer;

    private Config config;
    private double shiftX = 0;
    private double shiftY = 0;
    private double maxScale = NOT_FOUND;
    private double minScale = NOT_FOUND;

    private static class Position {
        double x,y;
        Position(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    private final ArrayList<Position> positions = new ArrayList<>();

    private final DoubleProperty scale;
    private final IntegerProperty selectedLabel;

    public CImagePane() {
        super();

        this.container = new AnchorPane();
        this.root = new StackPane();
        this.view = new ImageView();
        this.layers = new ArrayList<>();
        this.textLayer = new Canvas();
        this.scale = new SimpleDoubleProperty(1);
        this.selectedLabel = new SimpleIntegerProperty(NOT_FOUND);

        init();
        render();
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    private void init() {

        textLayer.getGraphicsContext2D().setFont(DISPLAY_FONT);

        scaleProperty().addListener((observable, oldValue, newValue) -> resize());

        // ScenePos -> CursorPos; LayoutPos -> CtxPos
        // nLx = Lx + (nSx - Sx); nLy = Ly + (nSy - Sy)
        // nLx = (Lx - Sx) + nSx; nLy = (Ly - Sy) + nSy
        root.setOnMousePressed(event -> {
            shiftX = root.getLayoutX() - event.getSceneX();
            shiftY = root.getLayoutY() - event.getSceneY();
            root.setCursor(javafx.scene.Cursor.MOVE);
        });
        root.setOnMouseDragged(event -> {
            double nX = event.getSceneX() + shiftX;
            double nY = event.getSceneY() + shiftY;
            root.setLayoutX(nX);
            root.setLayoutY(nY);
        });
        root.setOnMouseReleased(event -> {
            switch (config.getWorkMode()) {
                case Config.WORK_MODE_CHECK:
                case Config.WORK_MODE_INPUT:
                    root.setCursor(Cursor.DEFAULT);
                    break;
                case Config.WORK_MODE_LABEL:
                    root.setCursor(Cursor.CROSSHAIR);
                    break;
            }
        });

        root.setOnMouseEntered(event -> {
            switch (config.getWorkMode()) {
                case Config.WORK_MODE_LABEL:
                    root.setCursor(Cursor.CROSSHAIR);
                    break;
                case Config.WORK_MODE_CHECK:
                case Config.WORK_MODE_INPUT:
                    root.setCursor(Cursor.DEFAULT);
                    break;
            }
        });
        root.setOnMouseMoved(event -> {
            switch (config.getWorkMode()) {
                case Config.WORK_MODE_INPUT:
                case Config.WORK_MODE_CHECK:
                    handleCheckMode(event);
                    root.setCursor(Cursor.DEFAULT);
                    break;
                case Config.WORK_MODE_LABEL:
                    handleLabelMode(event);
                    root.setCursor(Cursor.CROSSHAIR);
                    break;
            }
            if (getIndexOf(new Position(event.getX(), event.getY())) != NOT_FOUND) {
                root.setCursor(Cursor.HAND);
            }
        });
        root.setOnMouseExited(event -> {
            root.setCursor(Cursor.DEFAULT);
            cleatText();
        });

        root.setOnMouseClicked(event -> {
            int index = getIndexOf(new Position(event.getX(), event.getY()));
            setSelectedLabel(index);

            if (event.isStillSincePress()) {
                switch (config.getWorkMode()) {
                    case Config.WORK_MODE_CHECK:
                        break;
                    case Config.WORK_MODE_LABEL:
                        handleLabelMode(event);
                        break;
                    case Config.WORK_MODE_INPUT:
                        handleInputMode(event);
                        break;
                }
            }
        });

        container.addEventHandler(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown() || event.isAltDown()) {
                setScale(getScale() + (event.getDeltaY() / 400));
            }
        });

    }
    private void render() {

        root.setAlignment(Pos.CENTER);

        view.setPreserveRatio(true);
        view.setPickOnBounds(true);
        root.getChildren().add(view);
        root.getChildren().add(textLayer);

        container.getChildren().add(root);
        setContent(container);
    }
    public void reset() {
        setImage(null);
        setupLayers(0);
        setSelectedLabel(NOT_FOUND);
        positions.clear();

        root.setLayoutX(0);
        root.setLayoutY(0);
        setScale(1);
    }

    private void setupImage() {
        try {
            setImage(new Image(String.valueOf(new File(config.getCurrentPicPath()).toURI().toURL())));
        } catch (IOException e) {
            CDialog.showException(e);
        }
    }
    private void setupLayers(int layerCount) {
        for (Canvas layer : layers) {
            layer.getGraphicsContext2D().clearRect(0, 0, layer.getWidth(), layer.getHeight());
            root.getChildren().remove(layer);
        }
        layers.clear();
        textLayer.getGraphicsContext2D().clearRect(0, 0, textLayer.getWidth(), textLayer.getHeight());

        for (int i = 0; i < layerCount; i++) {
            Canvas canvas = new Canvas(getViewWidth(), getViewHeight());
            root.getChildren().add(canvas);
            layers.add(canvas);
        }
        if (layerCount > 0) {
            textLayer.setWidth(getViewWidth());
            textLayer.setHeight(getViewHeight());
        }

        textLayer.toFront();
    }
    private void setupLabels() {
        List<TransLabel> labels = config.getLabelsNow();
        for (TransLabel label : labels) {
            drawLabel(label);
        }
    }

    private void drawLabel(TransLabel label) {
        GraphicsContext gc = getGraphicsContextAt(label.getGroupId());
        Canvas canvas = gc.getCanvas();
        double x = canvas.getWidth() * label.getX();
        double y = canvas.getHeight() * label.getY();
        double radius = LABEL_RADIUS;

        gc.setFill(javafx.scene.paint.Color.web(config.getGroupColors().get(label.getGroupId())));
        gc.fillOval(x, y,  radius, radius);

        Text text = new Text(String.valueOf(label.getIndex()));
        text.setFont(LABEL_FONT);
        double textWidth = text.getBoundsInLocal().getWidth();
        double textHeight = text.getBoundsInLocal().getHeight();
        double X = x + (radius - textWidth) / 2;
        double Y = (y + radius) - (radius - (textHeight - LABEL_FONT_SIZE * LINE_HEIGHT_RATIO) ) / 2 ;
        gc.setFill(Color.WHITE);
        gc.setFont(LABEL_FONT);
        gc.fillText(String.valueOf(label.getIndex()), X, Y);

        recordPosition(label.getIndex(), new Position(x, y));
    }
    private void drawText(String text, MouseEvent event) {
        GraphicsContext gc = textLayer.getGraphicsContext2D();
        int lineCount = text.length() - text.replaceAll("\n", "").length() + 1;
        Text t = new Text(text);
        t.setFont(DISPLAY_FONT);

        double textWidth = t.getBoundsInLocal().getWidth(), textHeight = t.getBoundsInLocal().getHeight();
        double w = textWidth + 2 * DISPLAY_INSET, h = textHeight + 2 * DISPLAY_INSET;
        double x = event.getX() + (SHIFT - DISPLAY_INSET);
        double X = event.getX() + SHIFT;
        double y = event.getY() - (textHeight / lineCount);
        double Y = event.getY();

        if (x + w > getViewWidth()) {
            x = x - w - SHIFT + DISPLAY_INSET;
            X = X - w - SHIFT + DISPLAY_INSET;
        }
        if (y - textHeight / lineCount < 0) {
            y = y + textHeight / lineCount;
            Y = Y + textHeight / lineCount;
        }

        gc.setFill(Color.web(CColor.toHex(Color.WHEAT) + "C0"));
        gc.fillRect(x, y, w, h);
        gc.setStroke(Color.DARKGRAY);
        gc.strokeRect(x, y, w, h);
        gc.setFill(Color.BLACK);
        gc.fillText(text, X, Y);
    }
    private void cleatText() {
        textLayer.getGraphicsContext2D().clearRect(0, 0, textLayer.getWidth(), textLayer.getHeight());
    }

    private void initPositions() {
        positions.clear();
        List<TransLabel> labels = config.getLabelsNow();
        int maxIndex = 0;
        for (TransLabel label : labels) maxIndex = Math.max(maxIndex, label.getIndex());
        for (int i = -1; i < maxIndex; i++) positions.add(null);

    }
    private void recordPosition(int index, Position position) {
        if (index >= positions.size()) {
            int enLargeSize = index - positions.size() + 1;
            for (int i = 0; i < enLargeSize; i++) {
                positions.add(null);
            }
        }
        positions.set(index, position);
    }
    private int getIndexOf(Position position) {
        int size = positions.size();
        for (int i = 0; i < size; i++) {
            Position p = positions.get(i);
            if (p != null) {
                if ((position.x >= p.x && position.x <= p.x + LABEL_RADIUS) && (position.y >= p.y && position.y <= p.y + LABEL_RADIUS)) {
                    return i;
                }
            }
        }
        return NOT_FOUND;
    }

    private void handleCheckMode(MouseEvent event) {
        cleatText();
        int index = getIndexOf(new Position(event.getX(), event.getY()));
        if (index != NOT_FOUND) {
            List<TransLabel> labels = config.getLabelsNow();
            Optional<TransLabel> result = labels.stream().filter(e -> e.getIndex() == index).findFirst();
            if (result.isPresent()) {
                TransLabel label = result.get();
                String text = label.getText();
                drawText(text, event);
            }
        }
    }
    private void handleLabelMode(MouseEvent event) {
        cleatText();
        drawText(config.getCurrentGroupName(), event);

        switch (event.getButton()) {
            case PRIMARY: {
                double x_percent = event.getX() / getViewWidth();
                double y_percent = event.getY() / getViewHeight();
                int groupId = config.getCurrentGroupId();
                int index = positions.size();

                String groupName = config.getGroupNameById(groupId);
                TransLabel newLabel = new TransLabel(index, x_percent, y_percent, groupId, "");
                TreeItem<String> groupItem = config.getControllerAccessor().findGroupItemByName(groupName);

                // Edit data
                config.getLabelsNow().add(newLabel);
                recordPosition(index, new Position(event.getX(), event.getY()));
                // Update view
                groupItem.getChildren().add(new CTreeItem(config, groupName, newLabel));
                drawLabel(newLabel);
                // Mark change
                config.setChanged(true);
                break;
            }
            case SECONDARY: {
                int index = getSelectedLabel();
                if (index != NOT_FOUND) {
                    CTreeItem item = config.getControllerAccessor().findLabelByIndex(index);
                    TreeItem<String> parent = item.getParent();

                    // Edit data
                    config.getLabelsNow().remove(item.meta);
                    updatePositions();
                    // Update view
                    parent.getChildren().remove(item);
                    update(item.meta.getGroupId());
                    // Mark change
                    config.setChanged(true);
                }
                break;
            }
        }
    }
    private void handleInputMode(MouseEvent event) { }

    private void updatePositions() {
        List<TransLabel> labels = config.getLabelsNow();

        int size = positions.size();
        out: for (int i = 0; i < size; i++) {
            if (positions.get(i) != null) {
                for (TransLabel l : labels) {
                    if (l.getIndex() == i) {
                        continue out;
                    }
                }
                positions.set(i, null);
            }
        }
    }
    private void updateLayer(int index) {
        List<TransLabel> labels = config.getLabelsNow();
        GraphicsContext gc = getGraphicsContextAt(index);
        Canvas layer = gc.getCanvas();

        gc.clearRect(0, 0, layer.getWidth(), layer.getHeight());
        for (TransLabel label : labels) {
            if (label.getGroupId() == index) {
                drawLabel(label);
                recordPosition(label.getIndex(), new Position(
                        getViewWidth() * label.getX(),
                        getViewHeight() * label.getY()
                ));
            }
        }
    }

    private GraphicsContext getGraphicsContextAt(int index) {
        return layers.get(index).getGraphicsContext2D();
    }

    public void resize() {
        container.setPrefSize(
                Math.max(root.getBoundsInParent().getMaxX(), getViewportBounds().getWidth()),
                Math.max(root.getBoundsInParent().getMaxY(), getViewportBounds().getHeight())
        );

        root.setScaleX(getScale());
        root.setScaleY(getScale());
    }
    public void update() {
        initPositions();
        setupImage();
        setupLayers(config.getGroupCount());
        setupLabels();
    }

    public void addNewLayer() {
        Canvas layer = new Canvas(getViewWidth(), getViewWidth());
        root.getChildren().add(layer);
        layers.add(layer);
        textLayer.toFront();
    }
    public void removeLayer(int index) {
        root.getChildren().remove(index);
        layers.remove(index);
    }
    public void update(int groupId) {
        if (groupId == TEXT_LAYER){
            if (config.getWorkMode() == Config.WORK_MODE_LABEL) {
                cleatText();
            }
        } else if (groupId < layers.size()) {
            updatePositions();
            updateLayer(groupId);
        }
    }
    public void moveTo(int index) {
        if (index != NOT_FOUND) {
            setVvalue(0);
            setHvalue(0);
            Position position = positions.get(index);
            // ScenePos -> CursorPos; LayoutPos -> CtxPos; LabelLayoutPos -> LayoutPos + Pos
            // Scale -> nL -> Image / 2 * (1 - Scale)

            // Label Center  -> Pos + Radius * 0.5
            double centerX = position.x + LABEL_RADIUS * 0.5;
            double centerY = position.y + LABEL_RADIUS * 0.5;

            // Scaled (fake) -> Image / 2 - (Image / 2 - Center) * Scale -> Image / 2 * (1 - Scale) + Center * Scale
            double fakeX = getViewWidth() * 0.5 - (getViewWidth() * 0.5 - centerX) * getScale();
            double fakeY = getViewHeight() * 0.5 - (getViewHeight() * 0.5 - centerY) * getScale();

            // To center (getW /2, getH /2) = LP + fake
            double X = getWidth() / 2 - fakeX;
            double Y = getHeight() / 2 - fakeY;

            root.setLayoutX(X);
            root.setLayoutY(Y);
        }
    }

    public double getViewWidth() {
        return getImage().getWidth();
    }
    public double getViewHeight() {
        return getImage().getHeight();
    }
    public Image getImage() {
        return imageProperty().get();
    }
    public void setImage(Image image) {
        imageProperty().set(image);
    }
    public ObjectProperty<Image> imageProperty() {
        return view.imageProperty();
    }

    public void setMinScale(double minScale) {
        if (minScale >= 0) {
            if (maxScale != NOT_FOUND) {
                if (minScale <= maxScale) {
                    this.minScale = minScale;
                }
            } else {
                this.minScale = minScale;
            }
        }
    }
    public void setMaxScale(double maxScale) {
        if (maxScale >= 0) {
            if (minScale != NOT_FOUND) {
                if (maxScale >= minScale) {
                    this.maxScale = maxScale;
                }
            } else {
                this.maxScale = maxScale;
            }
        }
    }
    public double getScale() {
        return scale.get();
    }
    public void setScale(double scale) {
        if (scale >= 0) {
            if (minScale != NOT_FOUND) scale = Math.max(scale, minScale);
            if (maxScale != NOT_FOUND) scale = Math.min(scale, maxScale);
            this.scale.set(scale);
        }
    }
    public DoubleProperty scaleProperty() {
        return scale;
    }

    public int getSelectedLabel() {
        return selectedLabelProperty().get();
    }
    public void setSelectedLabel(int index){
        selectedLabelProperty().set(index);
    }
    public IntegerProperty selectedLabelProperty() {
        return selectedLabel;
    }
}
