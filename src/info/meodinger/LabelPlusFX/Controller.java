package info.meodinger.LabelPlusFX;

import info.meodinger.LabelPlusFX.Component.*;
import info.meodinger.LabelPlusFX.IO.*;
import info.meodinger.LabelPlusFX.Property.Config;
import info.meodinger.LabelPlusFX.Property.Settings;
import info.meodinger.LabelPlusFX.Type.TransFile;
import info.meodinger.LabelPlusFX.Type.TransFile.MeoTransFile;
import info.meodinger.LabelPlusFX.Type.TransFile.MeoTransFile.Group;
import info.meodinger.LabelPlusFX.Type.TransLabel;
import info.meodinger.LabelPlusFX.Util.CAccelerator;
import info.meodinger.LabelPlusFX.Util.CDialog;
import info.meodinger.LabelPlusFX.Util.CString;
import info.meodinger.LabelPlusFX.Util.CTree;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.function.Function;

public class Controller implements Initializable {

    private final State state;
    private final FileChooser.ExtensionFilter fileFilter;
    private final FileChooser.ExtensionFilter meoFilter;
    private final FileChooser.ExtensionFilter lpFilter;
    private final FileChooser.ExtensionFilter bakFilter;
    private final FileChooser.ExtensionFilter packFilter;
    private final CFileChooser fileChooser;
    private final CFileChooser bakChooser;
    private final CFileChooser exportChooser;
    private final CFileChooser exportPackChooser;
    private final TransFileLoader.MeoFileLoader loaderMeo;
    private final TransFileLoader.LPFileLoader loaderLP;
    private final TransFileExporter.MeoFileExporter exporterMeo;
    private final TransFileExporter.LPFileExporter exporterLP;
    private final MeoPackager meoPackager;
    private final CTreeMenu menu;

    private class TransLabelTextListener implements ChangeListener<String> {
        private CTreeItem targetItem;

        public void retargetTo(CTreeItem target) {
            this.targetItem = null;
            if (target != null) {
                tTransText.setText(target.getText());
            } else {
                tTransText.setText("");
            }
            this.targetItem = target;
        }

        @Override
        public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
            if (targetItem != null) {
                targetItem.setText(newValue);
                state.setChanged(true);
            }
        }
    }
    private final TransLabelTextListener textListener = new TransLabelTextListener();

    private class ArrowKeyListener implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent event) {
            if (CAccelerator.isControlDown(event) && event.getCode().isArrowKey()) {
                switch (event.getCode()) {
                    case UP:
                    case DOWN:
                        int shift, index;

                        // Shift
                        if (event.getCode() == KeyCode.UP) shift = -1;
                        else shift = 1;

                        index = vTree.getSelectionModel().getSelectedIndex() + shift;
                        vTree.getSelectionModel().clearSelection();
                        // if item == null (to the end), vTree select nothing, return to top

                        TreeItem<String> item = vTree.getTreeItem(index);
                        if (item != null) {
                            if (item.getClass() == CTreeItem.class) {
                                // Label
                                vTree.getSelectionModel().select(index);
                                textListener.retargetTo((CTreeItem) item);
                                cImagePane.moveToLabel(((CTreeItem) item).meta.getIndex());
                                return;
                            } else if (item.getParent() != null) {
                                // Group
                                item.setExpanded(true);
                                index = index + shift;
                            } else {
                                // Root
                                CTree.expandAll(item);
                                if (state.getViewMode() == State.VIEW_MODE_GROUP) index++;
                                while (vTree.getTreeItem(index).getChildren().size() == 0) {
                                    index++;
                                    if (vTree.getTreeItem(index) == null) break;
                                }
                                index = index + shift;
                            }

                            vTree.getSelectionModel().select(index);
                            item = vTree.getTreeItem(index);
                            if (item == null) return;

                            if (item.getClass() == CTreeItem.class) {
                                // Label
                                textListener.retargetTo((CTreeItem) item);
                                cImagePane.moveToLabel(((CTreeItem) item).meta.getIndex());
                            } else {
                                textListener.retargetTo(null);
                            }
                        }
                        break;
                    case LEFT:
                        cPicBox.prev();
                        break;
                    case RIGHT:
                        cPicBox.next();
                        break;
                }
            }
        }
    }
    private final ArrowKeyListener arrowKeyListener = new ArrowKeyListener();

    private final ContextMenu symbolMenu = new ContextMenu() {

        class Symbol {
            final String symbol;
            final boolean isDisplayable;
            Symbol(String symbol, boolean isDisplayable) {
                this.symbol = symbol;
                this.isDisplayable = isDisplayable;
            }
        }

        final Symbol[] symbols = new Symbol[] {
                new Symbol("※", true),
                new Symbol("◎", true),
                new Symbol("★", true),
                new Symbol("☆", true),
                new Symbol("～", true),
                new Symbol("♡", false),
                new Symbol("♥", false),
                new Symbol("♢", false),
                new Symbol("♦", false),
                new Symbol("♪", false)
        };

        MenuItem createSymbolItem(String symbol, boolean displayable) {
            int radius = 6;
            MenuItem item = new MenuItem(symbol, displayable ? new Circle(radius, Color.GREEN) : new Circle(radius, Color.RED));
            item.setStyle("-fx-font-family: \"Segoe UI Symbol\"");
            return item;
        }

        {
            for (Symbol symbol : symbols) getItems().add(createSymbolItem(symbol.symbol, symbol.isDisplayable));
            getItems().forEach(item -> item.setOnAction(event -> tTransText.insertText(tTransText.getCaretPosition(), ((MenuItem) event.getSource()).getText())));
        }
    };

    private final class AutoBack extends TimerTask {
        @Override
        public void run() {
            if (state.isChanged()) {
                Controller.this.silentBak();
            }
        }
    }
    private final Timer timer;
    private AutoBack task = null;


    @FXML private TextArea tTransText;

    @FXML private Button btnSwitchViewMode;
    @FXML private Button btnSwitchWorkMode;

    @FXML private SplitPane pMain;
    @FXML private SplitPane pRight;
    @FXML private AnchorPane pText;

    @FXML private TreeView<String> vTree;

    @FXML private CLabelSlider cSlider;
    @FXML private CComboBox<String> cPicBox;
    @FXML private CComboBox<String> cGroupBox;
    @FXML private CImagePane cImagePane;

    @FXML private Menu mmFile;
    @FXML private MenuItem mNew;
    @FXML private MenuItem mOpen;
    @FXML private MenuItem mOpenRecent;
    @FXML private MenuItem mSave;
    @FXML private MenuItem mSaveAs;
    @FXML private MenuItem mBakRecover;
    @FXML private MenuItem mClose;
    @FXML private Menu mmExport;
    @FXML private MenuItem mExportAsLp;
    @FXML private MenuItem mExportAsMeo;
    @FXML private MenuItem mExportAsMeoPack;
    @FXML private MenuItem mEditComment;
    @FXML private Menu mmAbout;
    @FXML private MenuItem mAbout;
    @FXML private MenuItem mHint;

    public Controller(State state) {
        this.state = state;

        this.fileFilter = new FileChooser.ExtensionFilter(I18N.FILE_TRANSLATION, "*" + State.EXTENSION_MEO, "*" + State.EXTENSION_LP);
        this.meoFilter = new FileChooser.ExtensionFilter(I18N.FILE_MEO_TRANSLATION, "*" + State.EXTENSION_MEO);
        this.lpFilter = new FileChooser.ExtensionFilter(I18N.FILE_LP_TRANSLATION, "*" + State.EXTENSION_LP);
        this.bakFilter = new FileChooser.ExtensionFilter(I18N.FILE_BACKUP, "*" + State.EXTENSION_BAK);
        this.packFilter = new FileChooser.ExtensionFilter(I18N.FILE_PIC_PACK, "*" + State.EXTENSION_PACK);

        this.fileChooser = new CFileChooser();
        this.bakChooser = new CFileChooser();
        this.exportChooser = new CFileChooser();
        this.exportPackChooser = new CFileChooser();

        this.meoPackager = new MeoPackager(state);

        this.loaderMeo = new TransFileLoader.MeoFileLoader(state);
        this.loaderLP = new TransFileLoader.LPFileLoader(state);
        this.exporterMeo = new TransFileExporter.MeoFileExporter(state);
        this.exporterLP = new TransFileExporter.LPFileExporter(state);

        this.menu = new CTreeMenu(state);
        this.timer = new Timer();

        state.setControllerAccessor(new State.ControllerAccessor() {
            @Override
            public void close() {
                Controller.this.close();
            }

            @Override
            public void reset() {
                Controller.this.reset();
            }

            @Override
            public void addLabelLayer() {
                Controller.this.cImagePane.addLabelLayer();
            }

            @Override
            public void updateLabelLayer(int index) {
                Controller.this.cImagePane.updateLabelLayer(index);
            }

            @Override
            public void removeLabelLayer(int index) {
                Controller.this.cImagePane.removeLabelLayer(index);
            }

            @Override
            public void updateLabelLayers() {
                Controller.this.cImagePane.updateLabelLayers();
            }

            @Override
            public void updateTree() {
                Controller.this.loadTransLabel();
            }

            @Override
            public void updateGroupList() {
                Controller.this.updateGroupList();
            }

            @Override
            public CTreeItem findLabelByIndex(int index) {
                return Controller.this.findLabelItemByIndex(index);
            }

            @Override
            public Object get(String fieldName) {
                try {
                    Class<? extends Controller> clazz = Controller.this.getClass();
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.get(Controller.this);
                } catch (Exception e) {
                    CDialog.showException(e);
                }
                return null;
            }
        });

        init();
    }

    private void init() {
        fileChooser.getExtensionFilters().add(fileFilter);
        fileChooser.getExtensionFilters().add(meoFilter);
        fileChooser.getExtensionFilters().add(lpFilter);

        bakChooser.setTitle(I18N.CHOOSER_BAK_FILE);
        bakChooser.getExtensionFilters().add(bakFilter);

        exportChooser.setTitle(I18N.CHOOSER_EXPORT_TRANSLATION);

        exportPackChooser.setTitle(I18N.CHOOSER_EXPORT_TRANS_PACK);
        exportPackChooser.getExtensionFilters().add(packFilter);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setText();
        setDisable(true);

        // Initialize
        pMain.setDividerPositions(Config.Instance.get(Config.MainDivider).asDouble());
        pRight.setDividerPositions(Config.Instance.get(Config.RightDivider).asDouble());
        menu.treeMenu.init(vTree);
        cImagePane.setConfig(state);
        cImagePane.setMinScale(cSlider.getMinScale());
        cImagePane.setMaxScale(cSlider.getMaxScale());
        cPicBox.setWrapped(true);

        // Accelerator
        if (CAccelerator.isMac) {
            mSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN));
            mSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN, KeyCombination.SHIFT_DOWN));
        } else {
            mSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
            mSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        }

        // Fix split ratio when resize
        ChangeListener<Number> geometryListener = (observable, oldValue, newValue) -> {
            Function<Double, Double> debounce = input -> ((double) ((int) (input * 100 + 0.2))) / 100;
            pMain.setDividerPositions(debounce.apply(pMain.getDividerPositions()[0]));
            pRight.setDividerPositions(debounce.apply(pRight.getDividerPositions()[0]));
        };
        state.stage.widthProperty().addListener(geometryListener);
        state.stage.heightProperty().addListener(geometryListener);

        // Update Config
        pMain.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
            Config.Instance.get(Config.MainDivider).set(newValue);
        });
        pRight.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
            Config.Instance.get(Config.RightDivider).set(newValue);
        });

        // Reload labels and Repaint pane when change pic
        cPicBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            state.setCurrentPicName(newValue);
            loadTransLabel();
            if (newValue != null) {
                cImagePane.update();
                cImagePane.relocate();
                if (state.getLabelsNow().size() > 0) {
                    CTreeItem item = findLabelItemByIndex(state.getLabelsNow().get(0).getIndex());
                    if (item != null) {
                        vTree.getSelectionModel().select(item);
                        textListener.retargetTo(item);
                        cImagePane.moveToLabel(item.meta.getIndex());
                    }
                }
            }
        });

        // Update text layer when change group
        cGroupBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            state.setCurrentGroupId(state.getGroupIdByName(newValue));
            cImagePane.updateTextLayer();
        });

        // Update tree menu state when requested in view mode INDEX_MODE
        vTree.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            if (state.getViewMode() == State.VIEW_MODE_INDEX) {
                menu.treeMenu.update();
            }
        });

        // Bind view scale and slider value
        cImagePane.scaleProperty().addListener((observable, oldValue, newValue) -> cSlider.setScale((Double) newValue));
        cSlider.scaleProperty().addListener((observableValue, oldValue, newValue) -> cImagePane.setScale((Double) newValue));

        // Bind selected group with Config & GroupBox
        vTree.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            TreeItem<String> item = vTree.getSelectionModel().getSelectedItem();
            if (item != null) {
                if (item.getParent() != null && item.getClass() != CTreeItem.class) {
                    textListener.retargetTo(null);
                    cGroupBox.moveTo(state.getGroupIdByName(item.getValue()));
                }
            }
        });

        // Bind Text and Tree
        tTransText.textProperty().addListener(textListener);
        vTree.addEventHandler(ScrollToEvent.ANY, event -> {
            TreeItem<String> item = vTree.getSelectionModel().getSelectedItem();
            if (item != null && item.getClass() == CTreeItem.class) {
                // Label
                textListener.retargetTo((CTreeItem) item);
            } else {
                textListener.retargetTo(null);
            }
        });
        vTree.addEventHandler(MouseEvent.MOUSE_CLICKED,  event -> {
            TreeItem<String> item = vTree.getSelectionModel().getSelectedItem();
            if (item != null && item.getClass() == CTreeItem.class) {
                // Label
                textListener.retargetTo((CTreeItem) item);
            } else {
                textListener.retargetTo(null);
            }
        });
        vTree.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().isArrowKey()) {

                // Shift edit
                int shift = 0;
                if (event.getCode() == KeyCode.UP) shift = -1;
                else if (event.getCode() == KeyCode.DOWN) shift = 1;

                TreeItem<String> item = vTree.getTreeItem(vTree.getSelectionModel().getSelectedIndex() + shift);

                if (item != null && item.getClass() == CTreeItem.class) {
                    // Label
                    textListener.retargetTo((CTreeItem) item);
                } else {
                    textListener.retargetTo(null);
                }
            }
        });

        // Bind Label Graphic and Tree
        cImagePane.selectedLabelProperty().addListener((observable, oldValue, newValue) -> {
            int index = (int) newValue;
            if (index != CImagePane.NOT_FOUND) {
                CTreeItem item = findLabelItemByIndex(index);
                vTree.getSelectionModel().clearSelection();
                vTree.getSelectionModel().select(item);
                vTree.scrollTo(vTree.getRow(item));
            }
        });
        vTree.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() < 2) return;

            TreeItem<String> item = vTree.getSelectionModel().getSelectedItem();
            if (item != null && item.getClass() == CTreeItem.class) {
                int index = ((CTreeItem) item).meta.getIndex();
                cImagePane.moveToLabel(index);
            }
        });
        vTree.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().isArrowKey()) {

                // Shift edit
                int shift = 0;
                if (event.getCode() == KeyCode.UP) shift = -1;
                else if (event.getCode() == KeyCode.DOWN) shift = 1;

                TreeItem<String> item = vTree.getTreeItem(vTree.getSelectionModel().getSelectedIndex() + shift);

                if (item != null && item.getClass() == CTreeItem.class) {
                    int index = ((CTreeItem) item).meta.getIndex();
                    cImagePane.moveToLabel(index);
                }
            }
        });

        // Bind number input with group selection
        cImagePane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().isDigitKey()) {
                cGroupBox.moveTo(Integer.parseInt(event.getText()) - 1);
            }
        });

        // Bind Tab with work mode switch
        cImagePane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                switchWorkMode();
                event.consume();
            }
        });

        // Bind Tab with view mode switch
        vTree.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                switchViewMode();
                event.consume();
            }
        });

        // Bind Arrow KeyEvent with Label change and Pic change
        tTransText.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyListener);
        cImagePane.addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyListener);

        // Bind Alt/Meta+A with special symbols (Render Menu in )
        tTransText.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if ((event.isAltDown() || event.isMetaDown()) && event.getCode() == KeyCode.A) {
                double x = state.stage.getX() + pText.getScene().getWidth() - pText.getWidth() * 0.9;
                double y = state.stage.getY() + pText.getScene().getHeight() - pText.getHeight() + 40;

                symbolMenu.show(tTransText, x, y);
            }
        });
    }

    private void setText() {
        mmFile.setText(I18N.MM_FILE);
        mNew.setText(I18N.M_NEW);
        mOpen.setText(I18N.M_OPEN);
        mOpenRecent.setText(I18N.M_OPEN_RECENT);
        mSave.setText(I18N.M_SAVE);
        mSaveAs.setText(I18N.M_SAVE_AS);
        mBakRecover.setText(I18N.M_BAK_RECOVERY);
        mClose.setText(I18N.M_CLOSE);
        mmExport.setText(I18N.MM_EXPORT);
        mExportAsLp.setText(I18N.M_E_LP);
        mExportAsMeo.setText(I18N.M_E_MEO);
        mExportAsMeoPack.setText(I18N.M_E_MEO_P);
        mEditComment.setText(I18N.M_E_COMMENT);
        mmAbout.setText(I18N.MM_ABOUT);
        mHint.setText(I18N.M_HINT);
        mAbout.setText(I18N.M_ABOUT);

        btnSwitchWorkMode.setText(I18N.WORK_CHECK);
        btnSwitchViewMode.setText(I18N.VIEW_INDEX);
    }

    private void setDisable(boolean isDisable) {
        if (isDisable) {
            disable();
        } else {
            enable();
        }
    }
    private void disable() {
        mSave.setDisable(true);
        mSaveAs.setDisable(true);
        mExportAsLp.setDisable(true);
        mExportAsMeo.setDisable(true);
        mExportAsMeoPack.setDisable(true);
        mEditComment.setDisable(true);
        btnSwitchViewMode.setDisable(true);
        btnSwitchWorkMode.setDisable(true);
        tTransText.setDisable(true);

        cPicBox.setDisable(true);
        cGroupBox.setDisable(true);
        cSlider.setDisable(true);
    }
    private void enable() {
        mSave.setDisable(false);
        mSaveAs.setDisable(false);
        mExportAsLp.setDisable(false);
        mExportAsMeo.setDisable(false);
        mExportAsMeoPack.setDisable(false);
        mEditComment.setDisable(false);
        btnSwitchViewMode.setDisable(false);
        btnSwitchWorkMode.setDisable(false);
        tTransText.setDisable(false);

        cPicBox.setDisable(false);
        cGroupBox.setDisable(false);
        cSlider.setDisable(false);
    }

    private void reset() {
        vTree.setRoot(null);
        textListener.retargetTo(null);

        cPicBox.reset();
        // cGroupBox.reset will be invoked by cGroupBox.setList
        // cSlider will reset with cImagePane through listener
        cImagePane.reset();
        btnSwitchWorkMode.setText(I18N.WORK_CHECK);
        btnSwitchViewMode.setText(I18N.VIEW_INDEX);
        setDisable(true);
    }
    private void updateGroupList() {
        cGroupBox.setList(state.getGroupNames());
    }

    private void silentBak() {
        File bak = new File(state.getBakFolder() + File.separator + new Date().getTime() + State.EXTENSION_BAK);
        exporterMeo.export(bak);
    }
    private void prepare() {
        state.stage.setTitle(I18N.WINDOW_TITLE + " - " + new File(state.getFilePath()).getName());
        cPicBox.setList(state.getSortedPicList());
        updateGroupList();
        setDisable(false);

        if (task != null) {
            task.cancel();
        }
        File bakDir = new File(state.getBakFolder());
        if ((bakDir.exists() && bakDir.isDirectory()) || bakDir.mkdir()) {
            task = new AutoBack();
            timer.schedule(task, State.AUTO_SAVE_DELAY , State.AUTO_SAVE_PERIOD);
        } else {
            CDialog.showAlert(I18N.ALERT_AUTO_SAVE_NOT_AVAILABLE);
        }
    }
    private boolean trySave() {
        if (!CString.isBlank(state.getFilePath()) && state.isChanged()) {
            Optional<ButtonType> result = CDialog.showAlert(I18N.ALERT, I18N.SAVE_QUES, I18N.SAVE, I18N.NOT_SAVE);
            if (result.isPresent()) {
                ButtonBar.ButtonData data = result.get().getButtonData();
                if (data == ButtonBar.ButtonData.YES) {
                    saveTranslation();
                    return true;
                }
            }
        }
        return false;
    }
    @FXML public void newTranslation() {
        fileChooser.setTitle(I18N.CHOOSER_NEW_TRANSLATION);
        File file = fileChooser.showSaveDialog(state.stage);
        if (file == null) return;

        if (!trySave()) return;

        state.initialize();

        MeoTransFile transFile = new MeoTransFile();
        transFile.setVersion(new int[]{1, 0});
        transFile.setComment(TransFile.DEFAULT_COMMENT);

        List<Group> groups = new ArrayList<>();
        List<String> nameList = Settings.Instance.get(Settings.DefaultGroupList).asList();
        List<String> colorList = Settings.Instance.get(Settings.DefaultColorList).asList();
        for (int i = 0; i < nameList.size(); i++) {
            groups.add(new Group(nameList.get(i), colorList.get(i)));
        }
        transFile.setGroupList(groups);

        Map<String, List<TransLabel>> transMap = new HashMap<>();
        ArrayList<String> potentialFiles = new ArrayList<>();
        File dir = file.getParentFile();
        if (dir.isDirectory() && dir.listFiles() != null) {
            File[] listFiles = dir.listFiles();
            if (listFiles != null) {
                for (File f : listFiles) {
                    if (f.isFile()) {
                        for (String extension : State.PIC_EXTENSIONS) {
                            if (f.getName().endsWith(extension)) {
                                potentialFiles.add(f.getName());
                            }
                        }
                    }
                }
            }
        }
        Optional<List<String>> listResult = CDialog.showListChoose(state.stage, I18N.CHOOSE_PICS_TITLE, potentialFiles);
        if (listResult.isPresent()) {
            List<String> picList = listResult.get();
            for (String pic : picList) {
                transMap.put(pic, new ArrayList<>());
            }
            transFile.setTransMap(transMap);

            state.setTransFile(transFile);
            state.setFilePath(file.getPath());

            TransFileExporter exporter;
            if (state.isMeoFile()) {
                exporter = exporterMeo;
            } else {
                exporter = exporterLP;
            }
            if (exporter.export()) {
                prepare();
            } else {
                state.initialize();
            }
        }
    }
    @FXML public void openTranslation() {
        fileChooser.setTitle(I18N.CHOOSER_OPEN_TRANSLATION);
        File file = fileChooser.showOpenDialog(state.stage);
        if (file == null) return;

        if (!trySave()) return;

        state.initialize();

        state.setFilePath(file.getPath());
        TransFileLoader loader;
        if (state.isMeoFile()) {
            loader = loaderMeo;
        } else {
            loader = loaderLP;
        }
        if (loader.load(file)) {
            prepare();

            // Show info if comment not in default list
            String comment = state.getComment().trim();
            boolean isModified = true;
            for (String defaultComment : TransFile.DEFAULT_COMMENT_LIST) {
                if (comment.equals(defaultComment)) {
                    isModified = false;
                    break;
                }
            }
            if (isModified) {
                CDialog.showConfirm(I18N.INFO, I18N.DIALOG_CONTENT_MODIFIED_COMMENT, comment);
            }
        } else {
            state.initialize();
        }
    }
    @FXML public void saveTranslation() {
        if (state.getFilePath() == null || CString.isBlank(state.getFilePath())) {
            // Actually this never happen
            saveAsTranslation();
            return;
        }

        TransFileExporter exporter;
        if (state.isMeoFile()) {
            exporter = exporterMeo;
        } else {
            exporter = exporterLP;
        }

        File ori = new File(state.getFilePath());
        File bak = new File(state.getFilePath() + State.EXTENSION_BAK);
        boolean backed = false;

        try (FileChannel input = new FileInputStream(ori).getChannel();
             FileChannel output = new FileOutputStream(bak).getChannel()
        ) {
            output.transferFrom(input, 0, input.size());
            backed = true;
        } catch (Exception e) {
            CDialog.showException(new Exception(I18N.ALERT_BAK_FAILED, e));
        }

        if (exporter.export()) {
            state.setChanged(false);
            if (backed && !bak.delete()) {
                CDialog.showAlert(I18N.ALERT_BAK_FILE_DELETE_FAILED);
            }
        } else {
            CDialog.showInfo(String.format(I18N.FORMAT_SAVE_FAILED_BAK_PATH, bak.getPath()));
        }
    }
    @FXML public void saveAsTranslation() {
        fileChooser.setTitle(I18N.CHOOSER_SAVE_TRANSLATION);
        File file = fileChooser.showSaveDialog(state.stage);
        if (file == null) return;
        if (!file.getParent().equals(state.getFileFolder())) {
            Optional<ButtonType> result = CDialog.showAlert(I18N.DIALOG_CONTENT_SAVE_AS_ALERT);
            if (!(result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.YES)) return;
        }

        state.setFilePath(file.getPath());
        TransFileExporter exporter;
        if (state.isMeoFile()) {
            exporter = exporterMeo;
        } else {
            exporter = exporterLP;
        }

        if (exporter.export(file)) {
            state.setFilePath(file.getPath());
            state.setChanged(false);
            CDialog.showInfo(I18N.INFO_SAVED_SUCCESSFULLY);
        } else {
            state.setFilePath(null);
            CDialog.showAlert(I18N.ALERT_SAVE_FAILED);
        }
    }
    @FXML public void bakRecovery() {
        File file = bakChooser.showOpenDialog(state.stage);
        if (file == null) return;

        if (!trySave()) return;

        state.initialize();

        if (file.getParentFile().getName().equals(State.FOLDER_NAME_BAK)) {
            String projectFolder = file.getParentFile().getParentFile().getAbsolutePath();
            File recovered = new File(projectFolder + File.separator + file.getName().replace(State.EXTENSION_BAK, State.EXTENSION_MEO));

            state.setFilePath(recovered.getPath());
            if (loaderMeo.load(file)) {
                if (exporterMeo.export()) {
                    prepare();
                } else {
                    state.initialize();
                }
            } else {
                state.initialize();
            }
        } else {
            fileChooser.setTitle(I18N.CHOOSER_RECOVERY);
            File recovered = fileChooser.showSaveDialog(state.stage);
            if (recovered == null) return;

            state.setFilePath(recovered.getPath());
            if (loaderMeo.load(file)) {
                TransFileExporter exporter;
                if (state.isMeoFile()) {
                    exporter = exporterMeo;
                } else {
                    exporter = exporterLP;
                }
                if (exporter.export()) {
                    prepare();
                } else {
                    state.initialize();
                }
            } else {
                state.initialize();
            }
        }
    }
    @FXML public void close() {
        if (!state.isChanged()) System.exit(0);

        Optional<ButtonType> result = CDialog.showAlert(I18N.EXIT, I18N.SAVE_QUES, I18N.SAVE, I18N.NOT_SAVE);

        if (result.isPresent()){
            ButtonBar.ButtonData data = result.get().getButtonData();
            if (data == ButtonBar.ButtonData.YES) {
                saveTranslation();
                System.exit(0);
            } else if (data == ButtonBar.ButtonData.NO) {
                System.exit(0);
            }
        }
    }

    @FXML public void exportTransFile(ActionEvent event) {
        exportChooser.getExtensionFilters().clear();
        TransFileExporter exporter;

        if (event.getSource() == mExportAsLp) {
            exportChooser.getExtensionFilters().add(lpFilter);
            exporter = exporterLP;
        } else {
            exportChooser.getExtensionFilters().add(meoFilter);
            exporter = exporterMeo;
        }

        File file = exportChooser.showSaveDialog(state.stage);
        if (file == null) return;
        if (exporter.export(file)) {
            CDialog.showInfo(I18N.INFO_EXPORTED_SUCCESSFULLY);
        } else {
            CDialog.showAlert(I18N.ALERT_EXPORT_FAILED);
        }
    }
    @FXML public void exportTransPack() {
        File file = exportPackChooser.showSaveDialog(state.stage);
        if (file == null) return;

        if (meoPackager.packMeo(file.getPath())) {
            CDialog.showInfo(I18N.INFO_EXPORTED_PACK_SUCCESSFULLY);
        } else {
            CDialog.showAlert(I18N.ALERT_EXPORT_FAILED);
        }
    }
    @FXML public void setComment() {
        Optional<String> result = CDialog.showInputArea(state.stage, I18N.DIALOG_TITLE_EDIT_COMMENT, state.getComment());
        result.ifPresent(state::setComment);
    }

    @FXML public void hint() {
        CDialog.showInfoWithLink(
                I18N.HINT,
                I18N.HINT_CONTENT,
                I18N.HINT_LINK,
                event -> state.application.getHostServices().showDocument(I18N.HINT_LINK_URL)
        );
    }
    @FXML public void about() {
        CDialog.showInfoWithLink(
                I18N.ABOUT,
                I18N.ABOUT_CONTENT,
                I18N.ABOUT_LINK,
                event -> state.application.getHostServices().showDocument(I18N.ABOUT_LINK_URL)
        );
    }

    // Also reload transLabels
    private void setViewMode(int viewMode) {
        state.setViewMode(viewMode);
        switch (viewMode) {
            case State.VIEW_MODE_GROUP:
                btnSwitchViewMode.setText(I18N.VIEW_GROUP);
                break;
            case State.VIEW_MODE_INDEX:
                btnSwitchViewMode.setText(I18N.VIEW_INDEX);
                break;
        }
        loadTransLabel();
    }
    @FXML public void switchViewMode() {
        int viewMode = (state.getViewMode() + 1) % 2;
        setViewMode(viewMode);
    }

    private void setWorkMode(int workMode) {
        state.setWorkMode(workMode);
        switch (workMode) {
            case State.WORK_MODE_CHECK:
                btnSwitchWorkMode.setText(I18N.WORK_CHECK);
                setViewMode(State.VIEW_MODE_INDEX);
                break;
            case State.WORK_MODE_LABEL:
                btnSwitchWorkMode.setText(I18N.WORK_LABEL);
                setViewMode(State.VIEW_MODE_GROUP);
                break;
            case State.WORK_MODE_INPUT:
                setViewMode(State.VIEW_MODE_INDEX);
                btnSwitchWorkMode.setText(I18N.WORK_INPUT);
                break;
        }
        int count = state.getGroupCount();
        for (int i = 0; i < count; i++) {
            cImagePane.updateLabelLayer(i);
        }
    }
    @FXML public void switchWorkMode() {
        int workMode = (state.getWorkMode() + 1) % 3;
        setWorkMode(workMode);
    }

    // Also retarget textListener to null
    private void loadTransLabel() {
        textListener.retargetTo(null);

        if (state.getTransFile() != null) {
            switch (state.getViewMode()) {
                case State.VIEW_MODE_GROUP:
                    vTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                    vTree.setCellFactory(view -> new CTreeCell(State.VIEW_MODE_GROUP, menu));
                    vTree.setContextMenu(null);
                    loadTransLabel_Group();
                    break;
                case State.VIEW_MODE_INDEX:
                    vTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                    vTree.setCellFactory(null);
                    vTree.setContextMenu(menu.treeMenu);
                    loadTransLabel_Index();
                    break;
            }
        }
        CTree.expandAll(vTree.getRoot());
    }
    private void loadTransLabel_Group() {
        List<TransLabel> labels = state.getLabelsNow();

        TreeItem<String> root = new TreeItem<>(state.getCurrentPicName());
        ArrayList<TreeItem<String>> groups = new ArrayList<>();
        for (TransFile.MeoTransFile.Group group : state.getGroupList()) {
            Node node = new Circle(8, Color.web(group.color));
            TreeItem<String> item = new TreeItem<>(group.name, node);
            groups.add(item);
            root.getChildren().add(item);
        }
        for (TransLabel label : labels) {
            TreeItem<String> group = groups.get(label.getGroupId());
            CTreeItem item = new CTreeItem(state, group.getValue(), label);

            group.getChildren().add(item);
        }
        root.setExpanded(true);

        vTree.setRoot(root);
    }
    private void loadTransLabel_Index() {
        List<TransLabel> labels = state.getLabelsNow();
        List<TransFile.MeoTransFile.Group> groups = state.getGroupList();

        TreeItem<String> root = new TreeItem<>(state.getCurrentPicName());
        for (TransLabel label : labels) {
            TransFile.MeoTransFile.Group group = groups.get(label.getGroupId());
            Node node = new Circle(8, Color.web(group.color));
            CTreeItem item = new CTreeItem(state, group.name, label, node);

            root.getChildren().add(item);
        }
        root.setExpanded(true);

        vTree.setRoot(root);
    }
    private CTreeItem findLabelItemByIndex(int index) {
        if (index != CImagePane.NOT_FOUND) {
            List<TransLabel> labels = state.getLabelsNow();
            Optional<TransLabel> labelResult = labels.stream().filter(e -> e.getIndex() == index).findFirst();
            if (labelResult.isPresent()) {
                TransLabel label = labelResult.get();
                TreeItem<String> whereToSearch;
                if (state.getViewMode() == State.VIEW_MODE_GROUP) {
                    whereToSearch = vTree.getRoot().getChildren().get(label.getGroupId());
                } else {
                    whereToSearch = vTree.getRoot();
                }
                Optional<TreeItem<String>> itemResult = whereToSearch.getChildren().stream().filter(e -> ((CTreeItem) e).meta == label).findFirst();
                if (itemResult.isPresent()) {
                    return (CTreeItem) itemResult.get();
                }
            }
        }
        return null;
    }

}
