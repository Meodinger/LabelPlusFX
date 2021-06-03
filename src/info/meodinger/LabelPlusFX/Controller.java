package info.meodinger.LabelPlusFX;

import info.meodinger.LabelPlusFX.Component.*;
import info.meodinger.LabelPlusFX.IO.MeoPackager;
import info.meodinger.LabelPlusFX.IO.TransFileExporter;
import info.meodinger.LabelPlusFX.IO.TransFileLoader;
import info.meodinger.LabelPlusFX.Type.TransFile;
import info.meodinger.LabelPlusFX.Type.TransFile.MeoTransFile;
import info.meodinger.LabelPlusFX.Type.TransFile.MeoTransFile.Group;
import info.meodinger.LabelPlusFX.Type.TransLabel;
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

public class Controller implements Initializable {

    private final Config config;
    private final FileChooser.ExtensionFilter meoFilter;
    private final FileChooser.ExtensionFilter lpFilter;
    private final FileChooser.ExtensionFilter packFilter;
    private final FileChooser newChooser;
    private final FileChooser openChooser;
    private final FileChooser saveChooser;
    private final FileChooser exportChooser;
    private final TransFileLoader.MeoFileLoader loaderMeo;
    private final TransFileLoader.LPFileLoader loaderLP;
    private final TransFileExporter.MeoFileExporter exporterMeo;
    private final TransFileExporter.LPFileExporter exporterLP;
    private final MeoPackager meoPackager;

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
                config.setChanged(true);
            }
        }
    }
    private final TransLabelTextListener textListener = new TransLabelTextListener();


    private abstract class TreeItemListener<T extends Event> implements EventHandler<T> {
        public TreeItem<String> getItem(T event) {
            int shift = 0;

            // Shift edit
            if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                KeyEvent keyEvent = (KeyEvent) event;
                if (keyEvent.getCode() == KeyCode.UP) shift = -1;
                else if (keyEvent.getCode() == KeyCode.DOWN) shift = 1;
                else shift = 0;
            }

            int index = vTree.getSelectionModel().getSelectedIndex() + shift;
            return vTree.getTreeItem(index);
        }
    }
    private class TreeItemListener4Text<T extends Event> extends TreeItemListener<T> {
        @Override
        public void handle(T event) {
            TreeItem<String> item = getItem(event);

            if (item != null) {
                if (item.getClass() == CTreeItem.class) {
                    // Label
                    textListener.retargetTo((CTreeItem) item);
                } else {
                    // Other
                    textListener.retargetTo(null);
                }
            } else {
                textListener.retargetTo(null);
            }
        }
    }
    private class TreeItemListener4Label<T extends Event> extends TreeItemListener<T> {
        @Override
        public void handle(T event) {
            TreeItem<String> item = getItem(event);
            if (item != null) {
                if (item.getClass() == CTreeItem.class) {
                    // Label
                    int index = ((CTreeItem) item).meta.getIndex();
                    cImagePane.moveToLabel(index);
                }
            }
        }
    }

    private final CTreeMenu menu;

    private final class AutoBack extends TimerTask {
        @Override
        public void run() {
            Controller.this.silentBak();
        }
    }
    private final Timer timer;
    private AutoBack task = null;


    @FXML private BorderPane main;

    // @FXML private Label lbInfo;

    @FXML private TextArea tTransText;

    @FXML private Button btnSwitchViewMode;
    @FXML private Button btnSwitchWorkMode;

    @FXML private SplitPane pRight;
    @FXML private AnchorPane pTree;
    @FXML private AnchorPane pText;

    @FXML private TreeView<String> vTree;

    @FXML private CLabelSlider cSlider;
    @FXML private CComboBox<String> cPicBox;
    @FXML private CComboBox<String> cGroupBox;
    @FXML private CImagePane cImagePane;

    @FXML private Menu mmFile;
    @FXML private MenuItem mNew;
    @FXML private MenuItem mOpen;
    @FXML private MenuItem mSave;
    @FXML private MenuItem mSaveAs;
    @FXML private MenuItem mClose;
    @FXML private Menu mmExport;
    @FXML private MenuItem mExportAsLp;
    @FXML private MenuItem mExportAsMeo;
    @FXML private MenuItem mExportAsMeoPack;
    @FXML private Menu mmAbout;
    @FXML private MenuItem mAbout;
    @FXML private MenuItem mHint;

    public Controller(Config config) {
        this.config = config;

        this.meoFilter = new FileChooser.ExtensionFilter(I18N.MEO_TRANS_FILE, "*" + Config.EXTENSION_MEO);
        this.lpFilter = new FileChooser.ExtensionFilter(I18N.LP_TRANS_FILE, "*" + Config.EXTENSION_LP);
        this.packFilter = new FileChooser.ExtensionFilter(I18N.PACK_FILE, "*" + Config.EXTENSION_PACK);

        this.newChooser = new FileChooser();
        this.openChooser = new FileChooser();
        this.saveChooser = new FileChooser();
        this.exportChooser = new FileChooser();

        this.meoPackager = new MeoPackager(config);

        this.loaderMeo = new TransFileLoader.MeoFileLoader(config);
        this.loaderLP = new TransFileLoader.LPFileLoader(config);
        this.exporterMeo = new TransFileExporter.MeoFileExporter(config);
        this.exporterLP = new TransFileExporter.LPFileExporter(config);

        this.menu = new CTreeMenu(config);
        this.timer = new Timer();

        config.setControllerAccessor(new Config.ControllerAccessor() {
            @Override
            public void close() {
                Controller.this.close();
            }

            @Override
            public void reset() {
                Controller.this.reset();
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
            public TreeItem<String> findGroupItemByName(String name) {
                return Controller.this.findGroupItemByName(name);
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
        newChooser.setTitle(I18N.NEW_TRANSLATION);
        newChooser.getExtensionFilters().addAll(meoFilter, lpFilter);

        openChooser.setTitle(I18N.OPEN_TRANSLATION);
        openChooser.getExtensionFilters().addAll(meoFilter, lpFilter);

        saveChooser.setTitle(I18N.SAVE_TRANSLATION);
        saveChooser.getExtensionFilters().addAll(meoFilter, lpFilter);

        exportChooser.setTitle(I18N.EXPORT_TRANSLATION);
        exportChooser.getExtensionFilters().add(packFilter);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setText();
        setDisable(true);

        // Initialize
        cImagePane.setConfig(config);
        cImagePane.setMinScale(cSlider.getMinScale());
        cImagePane.setMaxScale(cSlider.getMaxScale());
        vTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        vTree.setCellFactory(view -> new CTreeCell(config, menu));

        // Accelerator
        mSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        mSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));

        // Fix width ratio
        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> pRight.setPrefWidth(config.stage.getWidth() / 3);
        config.stage.widthProperty().addListener(stageSizeListener);
        config.stage.widthProperty().addListener(stageSizeListener);

        // Reload when change pic
        cPicBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            config.setCurrentPicName(newValue);
            loadTransLabel();
            if (newValue != null) cImagePane.update();
        });

        // Update text layer
        cGroupBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            config.setCurrentGroupId(config.getGroupIdByName(newValue));
            config.setCurrentGroupName(newValue);
            cImagePane.updateLabelLayer(CImagePane.TEXT_LAYER);
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
                    cGroupBox.moveTo(config.getGroupIdByName(item.getValue()));
                }
            }
        });

        // Bind Text and Tree
        tTransText.textProperty().addListener(textListener);
        vTree.addEventHandler(MouseEvent.MOUSE_CLICKED, new TreeItemListener4Text<>());
        vTree.addEventHandler(KeyEvent.KEY_PRESSED, new TreeItemListener4Text<>());
        vTree.addEventHandler(ScrollToEvent.ANY, new TreeItemListener4Text<>());

        // Bind Label Graphic and Tree
        cImagePane.selectedLabelProperty().addListener((observable, oldValue, newValue) -> {
            int index = (int) newValue;
            if (index != CImagePane.NOT_FOUND) {
                CTreeItem item = findLabelItemByIndex(index);
                vTree.getSelectionModel().select(item);
                vTree.scrollTo(vTree.getRow(item));
            }
        });
        vTree.addEventHandler(MouseEvent.MOUSE_CLICKED, new TreeItemListener4Label<>());
        vTree.addEventHandler(KeyEvent.KEY_PRESSED, new TreeItemListener4Label<>());

        // Bind number input with group selection
        cImagePane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().isDigitKey()) {
                cGroupBox.moveTo(Integer.parseInt(event.getText()) - 1);
            }
        });

        // Bind Arrow KeyEvent with Label change and Pic change
        tTransText.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode().isArrowKey()) {
                switch (event.getCode()) {
                    case UP:
                    case DOWN:
                        int shift, index;

                        // Shift edit
                        if (event.getCode() == KeyCode.UP) shift = -1;
                        else shift = 1;

                        index = vTree.getSelectionModel().getSelectedIndex() + shift;
                        TreeItem<String> item = vTree.getTreeItem(index);

                        if (item != null) {
                            if (item.getClass() == CTreeItem.class) {
                                // Label
                            } else if (item.getParent() != null) {
                                // Group
                                item.setExpanded(true);
                                index = index + shift;

                            } else {
                                // Root
                                CTree.expandAll(item);
                                if (config.getViewMode() == Config.VIEW_MODE_GROUP) index++;
                                while (vTree.getTreeItem(index).getChildren().size() == 0) {
                                    index++;
                                    if (vTree.getTreeItem(index) == null) break;
                                }
                                index = index + shift;
                            }
                            vTree.getSelectionModel().select(index);
                            vTree.fireEvent(event);
                        }
                        break;
                    case LEFT:
                        cPicBox.back();
                        break;
                    case RIGHT:
                        cPicBox.next();
                        break;
                }
            }

        });

    }

    private void setText() {
        mmFile.setText(I18N.MM_FILE);
        mNew.setText(I18N.M_NEW);
        mOpen.setText(I18N.M_OPEN);
        mSave.setText(I18N.M_SAVE);
        mSaveAs.setText(I18N.M_SAVE_AS);
        mClose.setText(I18N.M_CLOSE);
        mmExport.setText(I18N.MM_EXPORT);
        mExportAsLp.setText(I18N.M_E_LP);
        mExportAsMeo.setText(I18N.M_E_MEO);
        mExportAsMeoPack.setText(I18N.M_E_MEO_P);
        mmAbout.setText(I18N.MM_ABOUT);
        mHint.setText(I18N.M_HINT);
        mAbout.setText(I18N.M_ABOUT);

        btnSwitchWorkMode.setText(I18N.WORK_CHECK);
        btnSwitchViewMode.setText(I18N.VIEW_GROUP);
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
        setDisable(true);
    }
    private void updateGroupList() {
        cGroupBox.setList(config.getGroupNames());
    }

    private void silentBak() {
        File bak = new File(config.getBakFolder() + File.separator + new Date().getTime() + Config.EXTENSION_BAK);
        exporterMeo.export(bak);
    }
    private void prepare() {
        cPicBox.setList(new ArrayList<>(config.getSortedPicSet()));
        updateGroupList();
        setDisable(false);

        if (task != null) {
            task.cancel();
        }
        File bakDir = new File(config.getBakFolder());
        if ((bakDir.exists() && bakDir.isDirectory()) || bakDir.mkdir()) {
            task = new AutoBack();
            timer.schedule(task, Config.AUTO_SAVE_DELAY , Config.AUTO_SAVE_PERIOD);
        } else {
            CDialog.showAlert(I18N.AUTO_SAVE_NOT_AVAILABLE);
        }
    }
    private void trySave() {
        if (config.getFilePath() != null && CString.isBlank(config.getFilePath()) && config.isChanged()) {
            Optional<ButtonType> result = CDialog.showAlert(I18N.EXIT, I18N.SAVE_QUES, I18N.SAVE, I18N.NOT_SAVE);
            if (result.isPresent()) {
                ButtonBar.ButtonData data = result.get().getButtonData();
                if (data == ButtonBar.ButtonData.YES) {
                    saveTranslation();
                }
            }
        }
    }
    @FXML public void newTranslation() {
        File file = newChooser.showSaveDialog(config.stage);
        if (file == null) return;
        trySave();
        config.initialize();

        MeoTransFile transFile = new MeoTransFile();
        transFile.setVersion(new int[]{1, 0});
        transFile.setComment(TransFile.DEFAULT_COMMENT);

        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Group group = new Group(String.format(I18N.FORMAT_NEW_GROUP_NAME, i + 1), MeoTransFile.DEFAULT_COLOR_LIST[i]);
            groups.add(group);
        }
        transFile.setGroup(groups);

        Map<String, List<TransLabel>> transMap = new HashMap<>();
        ArrayList<String> potentialFiles = new ArrayList<>();
        File dir = file.getParentFile();
        if (dir.isDirectory() && dir.listFiles() != null) {
            File[] listFiles = dir.listFiles();
            if (listFiles != null) {
                List<File> files = new ArrayList<>(Arrays.asList(listFiles));
                for (File f : files) {
                    if (f.isFile()) {
                        for (String extension : Config.PIC_EXTENSIONS) {
                            if (f.getName().endsWith(extension)) {
                                potentialFiles.add(f.getName());
                            }
                        }
                    }
                }
            }
        }
        Optional<List<String>> listResult = CDialog.showListChoose(I18N.CHOOSE_PICS_TITLE, potentialFiles);
        if (listResult.isPresent()) {
            List<String> picList = listResult.get();
            for (String pic : picList) {
                transMap.put(pic, new ArrayList<>());
            }
            transFile.setTransMap(transMap);

            config.setTransFile(transFile);
            config.setFilePath(file.getPath());

            TransFileExporter exporter;
            if (config.isMeoFile()) {
                exporter = exporterMeo;
            } else {
                exporter = exporterLP;
            }
            if (exporter.export()) {
                prepare();
            } else {
                config.initialize();
            }
        }
    }
    @FXML public void openTranslation() {
        File file = openChooser.showOpenDialog(config.stage);
        if (file == null) return;
        trySave();
        config.initialize();

        config.setFilePath(file.getPath());
        TransFileLoader loader;
        if (config.isMeoFile()) {
            loader = loaderMeo;
        } else {
            loader = loaderLP;
        }
        if (loader.load(file)) {
            prepare();
        } else {
            config.initialize();
        }
    }
    @FXML public void saveTranslation() {
        if (config.getFilePath() == null || CString.isBlank(config.getFilePath())) {
            saveAsTranslation();
            return;
        }

        TransFileExporter exporter;
        if (config.isMeoFile()) {
            exporter = exporterMeo;
        } else {
            exporter = exporterLP;
        }

        File ori = new File(config.getFilePath());
        File bak = new File(config.getFilePath() + Config.EXTENSION_BAK);

        try (FileChannel input = new FileInputStream(ori).getChannel();
             FileChannel output = new FileOutputStream(bak).getChannel()
        ) {
            output.transferFrom(input, 0, input.size());
        } catch (Exception e) {
            CDialog.showException(e);
        }

        if (exporter.export()) {
            config.setChanged(false);
            CDialog.showInfo(I18N.SAVED_SUCCESSFULLY);
            if (!bak.delete()) {
                CDialog.showAlert(I18N.BAK_FILE_DELETED_FAILED);
            }
        } else {
            CDialog.showInfo(String.format(I18N.FORMAT_BAK_FILE_PATH, bak.getPath()));
        }
    }
    @FXML public void saveAsTranslation() {
        File file = saveChooser.showSaveDialog(config.stage);
        if (file == null) return;

        config.setFilePath(file.getPath());
        TransFileExporter exporter;
        if (config.isMeoFile()) {
            exporter = exporterMeo;
        } else {
            exporter = exporterLP;
        }

        if (exporter.export(file)) {
            config.setFilePath(file.getPath());
            config.setChanged(false);
            CDialog.showInfo(I18N.SAVED_SUCCESSFULLY);
        } else {
            config.setFilePath(null);
        }
    }
    @FXML public void close() {
        if (!config.isChanged()) System.exit(0);

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

        File file = exportChooser.showSaveDialog(config.stage);
        if (file == null) return;
        if (exporter.export(file)) {
            CDialog.showInfo(I18N.EXPORTED_SUCCESSFULLY);
        }
    }
    @FXML public void exportTransPack() {
        File file = exportChooser.showSaveDialog(config.stage);
        if (file == null) return;

        if (meoPackager.packMeo(file.getPath())) {
            CDialog.showInfo(I18N.EXPORTED_PACK_SUCCESSFULLY);
        }
    }

    @FXML public void hint() {
        CDialog.showInfo(I18N.HINT, I18N.HINT_CONTENT);
    }
    @FXML public void about() {
        CDialog.showInfo(I18N.ABOUT, I18N.ABOUT_CONTENT);
    }

    private void setViewMode(int viewMode) {
        config.setViewMode(viewMode);
        switch (viewMode) {
            case Config.VIEW_MODE_GROUP:
                btnSwitchViewMode.setText(I18N.VIEW_GROUP);
                break;
            case Config.VIEW_MODE_INDEX:
                btnSwitchViewMode.setText(I18N.VIEW_INDEX);
                break;
        }
        loadTransLabel();
    }
    @FXML public void switchViewMode() {
        int viewMode = (config.getViewMode() + 1) % 2;
        setViewMode(viewMode);
    }

    private void setWorkMode(int workMode) {
        config.setWorkMode(workMode);
        switch (workMode) {
            case Config.WORK_MODE_CHECK:
                btnSwitchWorkMode.setText(I18N.WORK_CHECK);
                setViewMode(Config.VIEW_MODE_INDEX);
                break;
            case Config.WORK_MODE_LABEL:
                btnSwitchWorkMode.setText(I18N.WORK_LABEL);
                setViewMode(Config.VIEW_MODE_GROUP);
                break;
            case Config.WORK_MODE_INPUT:
                setViewMode(Config.VIEW_MODE_INDEX);
                btnSwitchWorkMode.setText(I18N.WORK_INPUT);
                break;
        }
    }
    @FXML public void switchWorkMode() {
        int workMode = (config.getWorkMode() + 1) % 3;
        setWorkMode(workMode);
    }

    /**
     *  Reload all Labels will invoke CImagePane::update
     */
    private void loadTransLabel() {
        textListener.retargetTo(null);

        if (config.getTransFile() != null) {
            switch (config.getViewMode()) {
                case Config.VIEW_MODE_GROUP:
                    loadTransLabel_Group();
                    break;
                case Config.VIEW_MODE_INDEX:
                    loadTransLabel_Index();
                    break;
            }
        }
    }
    private void loadTransLabel_Group() {
        List<TransLabel> labels = config.getLabelsNow();

        TreeItem<String> root = new TreeItem<>(config.getCurrentPicName());
        ArrayList<TreeItem<String>> groups = new ArrayList<>();
        for (TransFile.MeoTransFile.Group group : config.getGroups()) {
            Node node = new Circle(8, Color.web(group.color));
            TreeItem<String> item = new TreeItem<>(group.name, node);
            groups.add(item);
            root.getChildren().add(item);
        }
        for (TransLabel label : labels) {
            TreeItem<String> group = groups.get(label.getGroupId());
            CTreeItem item = new CTreeItem(config, group.getValue(), label);

            group.getChildren().add(item);
        }
        root.setExpanded(true);

        vTree.setRoot(root);
    }
    private void loadTransLabel_Index() {
        List<TransLabel> labels = config.getLabelsNow();
        List<TransFile.MeoTransFile.Group> groups = config.getGroups();

        TreeItem<String> root = new TreeItem<>(config.getCurrentPicName());
        for (TransLabel label : labels) {
            TransFile.MeoTransFile.Group group = groups.get(label.getGroupId());
            Node node = new Circle(8, Color.web(group.color));
            CTreeItem item = new CTreeItem(config, group.name, label, node);

            root.getChildren().add(item);
        }
        root.setExpanded(true);

        vTree.setRoot(root);
    }
    private CTreeItem findLabelItemByIndex(int index) {
        if (index != CImagePane.NOT_FOUND) {
            List<TransLabel> labels = config.getLabelsNow();
            Optional<TransLabel> labelResult = labels.stream().filter(e -> e.getIndex() == index).findFirst();
            if (labelResult.isPresent()) {
                TransLabel label = labelResult.get();
                TreeItem<String> whereToSearch;
                if (config.getViewMode() == Config.VIEW_MODE_GROUP) {
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
    private TreeItem<String> findGroupItemByName(String name) {
        if (name != null && config.getViewMode() == Config.VIEW_MODE_GROUP) {
            TreeItem<String> root = vTree.getRoot();
            Optional<TreeItem<String>> itemResult = root.getChildren().stream().filter(e -> e.getValue().equals(name)).findFirst();
            if (itemResult.isPresent()) {
                return itemResult.get();
            }
        }
        return null;
    }

}
