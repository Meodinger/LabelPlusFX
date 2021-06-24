package info.meodinger.LabelPlusFX.Component;

import info.meodinger.LabelPlusFX.State;
import info.meodinger.LabelPlusFX.Type.TransLabel;
import info.meodinger.LabelPlusFX.Util.CAccelerator;
import info.meodinger.LabelPlusFX.Util.CColor;
import info.meodinger.LabelPlusFX.Util.CDialog;

import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final static String ALPHA = "80";

    /**
     * X shift for text & shape display
     */
    private final static int SHIFT = 20;
    /**
     * Ratio for padding height of text line
     */
    private final static double LINE_HEIGHT_RATIO = 0.5;

    private final static int LABEL_FONT_SIZE = 32;
    private final static javafx.scene.text.Font LABEL_FONT = new javafx.scene.text.Font(LABEL_FONT_SIZE);

    private final static int DISPLAY_FONT_SIZE = 28;
    private final static javafx.scene.text.Font DISPLAY_FONT = new Font(DISPLAY_FONT_SIZE);

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

    private State state;
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

    public void setConfig(State state) {
        this.state = state;
    }

    private void init() {

        textLayer.getGraphicsContext2D().setFont(DISPLAY_FONT);

        scaleProperty().addListener((observable, oldValue, newValue) -> resize());

        // Label mode draggable
        AtomicBoolean isLabel = new AtomicBoolean(false);
        AtomicInteger labelIndex = new AtomicInteger(NOT_FOUND);
        AtomicInteger labelGroupId = new AtomicInteger(NOT_FOUND);
        root.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (state.getWorkMode() == State.WORK_MODE_LABEL) {
                int index = getIndexOf(new Position(event.getX(), event.getY()));
                if (index != NOT_FOUND) {
                    event.consume();
                    isLabel.set(true);
                    labelIndex.set(index);
                    labelGroupId.set(state.getLabelAt(index).getGroupId());
                    return;
                }
            }

            isLabel.set(false);
            labelIndex.set(NOT_FOUND);
            labelGroupId.set(NOT_FOUND);
        });
        root.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (isLabel.get()) {
                event.consume();
                if (event.getX() < 0 || event.getX() + LABEL_RADIUS > getViewWidth()) return;
                if (event.getY() < 0 || event.getY() + LABEL_RADIUS > getViewHeight()) return;

                cleatText();
                double x_percent = event.getX() / getViewWidth();
                double y_percent = event.getY() / getViewHeight();
                TransLabel label = state.getLabelAt(labelIndex.get());

                label.setX(x_percent);
                label.setY(y_percent);

                updateLabelLayer(labelGroupId.get());
            }
        });

        // ScenePos -> CursorPos; LayoutPos -> CtxPos
        // nLx = Lx + (nSx - Sx); nLy = Ly + (nSy - Sy)
        // nLx = (Lx - Sx) + nSx; nLy = (Ly - Sy) + nSy
        root.setOnMousePressed(event -> {
            if (!event.isConsumed()) {
                shiftX = root.getLayoutX() - event.getSceneX();
                shiftY = root.getLayoutY() - event.getSceneY();
                root.setCursor(javafx.scene.Cursor.MOVE);
            }
        });
        root.setOnMouseDragged(event -> {
            if (!event.isConsumed()) {
                double nX = event.getSceneX() + shiftX;
                double nY = event.getSceneY() + shiftY;
                root.setLayoutX(nX);
                root.setLayoutY(nY);
            }
        });
        root.setOnMouseReleased(event -> {
            switch (state.getWorkMode()) {
                case State.WORK_MODE_CHECK:
                case State.WORK_MODE_INPUT:
                    root.setCursor(Cursor.DEFAULT);
                    break;
                case State.WORK_MODE_LABEL:
                    root.setCursor(Cursor.CROSSHAIR);
                    break;
            }
        });

        root.setOnMouseEntered(event -> {
            switch (state.getWorkMode()) {
                case State.WORK_MODE_LABEL:
                    root.setCursor(Cursor.CROSSHAIR);
                    break;
                case State.WORK_MODE_CHECK:
                case State.WORK_MODE_INPUT:
                    root.setCursor(Cursor.DEFAULT);
                    break;
            }
        });
        root.setOnMouseMoved(event -> {
            switch (state.getWorkMode()) {
                case State.WORK_MODE_INPUT:
                case State.WORK_MODE_CHECK:
                    handleCheckMode(event);
                    root.setCursor(Cursor.DEFAULT);
                    break;
                case State.WORK_MODE_LABEL:
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
                switch (state.getWorkMode()) {
                    case State.WORK_MODE_CHECK:
                        break;
                    case State.WORK_MODE_LABEL:
                        handleLabelMode(event);
                        break;
                    case State.WORK_MODE_INPUT:
                        handleInputMode(event);
                        break;
                }
            }
        });

        container.setOnScroll(event -> {
            updateTextLayer();
            if (CAccelerator.isControlDown(event) || event.isAltDown()) {
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
            setImage(new Image(String.valueOf(new File(state.getCurrentPicPath()).toURI().toURL())));
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
        List<TransLabel> labels = state.getLabelsNow();
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

        String alpha = state.getWorkMode() == State.WORK_MODE_LABEL ? "FF" : ALPHA;
        String colorWeb = state.getGroupColorById(label.getGroupId()) + alpha;
        gc.setFill(Color.web(colorWeb));
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
        drawText(text, Color.BLACK, event);
    }
    private void drawText(String text, Color textColor, MouseEvent event) {
        GraphicsContext gc = textLayer.getGraphicsContext2D();
        int lineCount = text.length() - text.replaceAll("\n", "").length() + 1;
        Text t = new Text(text);
        t.setFont(DISPLAY_FONT);

        double textWidth = t.getBoundsInLocal().getWidth(), textHeight = t.getBoundsInLocal().getHeight();
        double w = textWidth + 2 * DISPLAY_INSET, h = textHeight + 2 * DISPLAY_INSET;

        double x_shape = event.getX() + (SHIFT - DISPLAY_INSET);
        double y_shape = event.getY() - (textHeight / lineCount);
        double x_text = event.getX() + SHIFT;
        double y_text = event.getY();

        if (x_shape + w > getViewWidth()) {
            x_shape = x_shape - w - SHIFT + DISPLAY_INSET;
            x_text = x_text - w - SHIFT + DISPLAY_INSET;
        }
        if (y_shape - textHeight / lineCount < 0) {
            y_shape = y_shape + textHeight / lineCount;
            y_text = y_text + textHeight / lineCount;
        }

        gc.setFill(Color.web(CColor.toHex(Color.WHEAT) + ALPHA));
        gc.fillRect(x_shape, y_shape, w, h);
        gc.setStroke(Color.DARKGRAY);
        gc.strokeRect(x_shape, y_shape, w, h);
        gc.setFill(textColor);
        gc.fillText(text, x_text, y_text);
    }
    private void cleatText() {
        textLayer.getGraphicsContext2D().clearRect(0, 0, textLayer.getWidth(), textLayer.getHeight());
    }

    private void initPositions() {
        positions.clear();
        int size = state.getLabelsNow().size();
        for (int i = -1; i < size; i++) positions.add(null);
    }
    private void recordPosition(int index, Position position) {
        // Though the app chooses to let indexes in order, we also support unique index like `114514`
        if (index >= positions.size()) {
            int enlargeSize = index - positions.size() + 1;
            for (int i = 0; i < enlargeSize; i++) {
                positions.add(null);
            }
        }
        positions.set(index, position);
    }
    private int getIndexOf(Position position) {
        int size = positions.size();
        int index = NOT_FOUND;
        for (int i = 0; i < size; i++) {
            Position p = positions.get(i);
            if (p != null) {
                if ((position.x >= p.x && position.x <= p.x + LABEL_RADIUS) && (position.y >= p.y && position.y <= p.y + LABEL_RADIUS)) {
                    index = i;
                }
            }
        }
        return index;
    }

    private void handleCheckMode(MouseEvent event) {
        cleatText();
        int index = getIndexOf(new Position(event.getX(), event.getY()));
        if (index != NOT_FOUND) {
            List<TransLabel> labels = state.getLabelsNow();
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
        drawText(state.getCurrentGroupName(),Color.web(state.getGroupColorById(state.getCurrentGroupId())) , event);

        switch (event.getButton()) {
            case PRIMARY: {
                double x_percent = event.getX() / getViewWidth();
                double y_percent = event.getY() / getViewHeight();
                int groupId = state.getCurrentGroupId();
                int index = state.getLabelsNow().size() + 1;

                TransLabel newLabel = new TransLabel(index, x_percent, y_percent, groupId, "");

                // Edit data
                state.getLabelsNow().add(newLabel);
                recordPosition(index, new Position(event.getX(), event.getY()));
                // Update view
                drawLabel(newLabel);
                state.getControllerAccessor().updateTree();
                // Mark change
                state.setChanged(true);
                break;
            }
            case SECONDARY: {
                int index = getSelectedLabel();
                if (index != NOT_FOUND) {
                    CTreeItem labelItem = state.getControllerAccessor().findLabelByIndex(index);

                    // Edit data
                    for (TransLabel l : state.getLabelsNow())
                        if (l.getIndex() > labelItem.meta.getIndex()) l.setIndex(l.getIndex() - 1);
                    state.getLabelsNow().remove(labelItem.meta);
                    // Update view
                    updateLabelLayer(labelItem.meta.getGroupId());
                    state.getControllerAccessor().updateTree();
                    // Mark change
                    state.setChanged(true);
                }
                break;
            }
        }
    }
    private void handleInputMode(MouseEvent event) { }

    private void updatePositions() {
        List<TransLabel> labels = state.getLabelsNow();

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
        List<TransLabel> labels = state.getLabelsNow();
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
        setupLayers(state.getGroupCount());
        setupLabels();
    }
    public void relocate() {
        root.setLayoutX(0);
        root.setLayoutY(0);
    }

    public void addLabelLayer() {
        Canvas layer = new Canvas(getViewWidth(), getViewWidth());
        root.getChildren().add(layer);
        layers.add(layer);
        textLayer.toFront();
    }
    public void removeLabelLayer(int groupId) {
        root.getChildren().remove(groupId + 1); // Bottom is ImageView
        layers.remove(groupId);
    }
    public void updateLabelLayer(int groupId) {
        if (groupId < layers.size()) {
            updatePositions();
            updateLayer(groupId);
        }
    }
    public void updateTextLayer() {
        switch (state.getWorkMode()) {
            case State.WORK_MODE_CHECK:
            case State.WORK_MODE_INPUT: {
                break;
            }
            case State.WORK_MODE_LABEL: {
                cleatText();
                break;
            }
        }
    }
    public void moveToLabel(int index) {
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
