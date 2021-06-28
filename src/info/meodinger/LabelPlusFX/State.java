package info.meodinger.LabelPlusFX;

import info.meodinger.LabelPlusFX.Component.CTreeItem;
import info.meodinger.LabelPlusFX.Type.TransFile.MeoTransFile;
import info.meodinger.LabelPlusFX.Type.TransLabel;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

/**
 * @author Meodinger
 * Date: 2021/5/18
 * Location: info.meodinger.LabelPlusFX
 */
public final class State {

    public static final String[] PIC_EXTENSIONS = new String[] {
            ".png", ".jpg", ".jpeg"
    };
    public static final String EXTENSION_MEO = ".json";
    public static final String EXTENSION_LP = ".txt";
    public static final String EXTENSION_PACK = ".zip";
    public static final String EXTENSION_BAK = ".bak";
    public static final String FOLDER_NAME_BAK = "bak";

    public static final int WORK_MODE_CHECK = 0;
    public static final int WORK_MODE_LABEL = 1;
    public static final int WORK_MODE_INPUT = 2;
    public static final int WORK_MODE_DEFAULT = WORK_MODE_CHECK;

    public static final int VIEW_MODE_GROUP = 0;
    public static final int VIEW_MODE_INDEX = 1;
    public static final int VIEW_MODE_DEFAULT = VIEW_MODE_INDEX;

    public static final int AUTO_SAVE_DELAY = 5 * 60 * 1000;
    public static final int AUTO_SAVE_PERIOD = 3 * 60 * 1000;

    public final Application application;
    public final Stage stage;

    private MeoTransFile transFile;
    private String filePath;
    private boolean isChanged;

    private int workMode;
    private int viewMode;
    private int currentGroupId;
    private String currentPicName;

    public State(Application application, Stage stage) {
        this.application = application;
        this.stage = stage;

        this.workMode = WORK_MODE_DEFAULT;
        this.viewMode = VIEW_MODE_DEFAULT;
        this.isChanged = false;
    }

    public void initialize() {
        setTransFile(null);
        setFilePath(null);
        setChanged(false);

        stage.setTitle(I18N.WINDOW_TITLE);
        setWorkMode(WORK_MODE_DEFAULT);
        setViewMode(VIEW_MODE_DEFAULT);
        setCurrentGroupId(0);
        setCurrentPicName(null);
        controllerAccessor.reset();
    }

    @Override
    public String toString() {
        return "Config{" +
                "filePath='" + filePath + '\'' +
                ", isChanged=" + isChanged +
                ", workMode=" + workMode +
                ", viewMode=" + viewMode +
                ", currentGroupId=" + currentGroupId +
                ", currentPicName='" + currentPicName + '\'' +
                '}';
    }

    public void setTransFile(MeoTransFile transFile) {
        this.transFile = transFile;
    }

    /**
     * Get TransFile reference
     * @return Origin Reference
     */
    public MeoTransFile getTransFile() {
        return transFile;
    }

    /**
     * Get TransFile map of PicName - List&lt;TransLabel&rt;
     * @return Origin Reference
     */
    public Map<String, List<TransLabel>> getTransMap() {
        return transFile.getTransMap();
    }

    /**
     * Get Labels at <code>picName</code>
     * @param picName where labels at
     * @return Origin Reference
     */
    public List<TransLabel> getLabelsAt(String picName) {
        return getTransMap().get(picName);
    }

    /**
     * Get Labels at current pic
     * @return Origin Reference
     */
    public List<TransLabel> getLabelsNow() {
        return getLabelsAt(currentPicName);
    }

    /**
     * Get Label by index at current pic
     * @return Origin Reference
     */
    public TransLabel getLabelAt(int index) {
        return getLabelsNow().get(index - 1);
    }

    /**
     * Get groups
     * @return Origin Reference
     */
    public List<MeoTransFile.Group> getGroupList() {
        return transFile.getGroupList();
    }

    /**
     * Get Group which id is <code>groupId</code>
     * @param groupId of Group
     * @return Origin Reference
     */
    public MeoTransFile.Group getGroupAt(int groupId) {
        return getGroupList().get(groupId);
    }

    /**
     * Get Group of currentGrouptId
     * @return Origin Reference
     */
    public MeoTransFile.Group getGroupNow() {
        return getGroupAt(currentGroupId);
    }

    public String getComment() {
        return transFile.getComment();
    }

    public void setComment(String comment) {
        transFile.setComment(comment);
    }

    public int[] getVersion() {
        return transFile.getVersion();
    }

    public void setVersion(int[] version) {
        if (version.length != 2) throw new IllegalArgumentException(String.format(I18N.FORMAT_INVALID_VERSION, version.length));
        transFile.setVersion(version);
    }

    public List<String> getSortedPicList() {
        return MeoTransFile.getSortedPicList(transFile);
    }

    public int getGroupCount() {
        return transFile.getGroupList().size();
    }

    public List<String> getGroupNames() {
        ArrayList<String> list = new ArrayList<>();
        for (MeoTransFile.Group group : transFile.getGroupList()) {
            list.add(group.name);
        }
        return list;
    }
    public int getGroupIdByName(String name) {
        int size = getGroupCount();
        for (int i = 0; i < size; i++) {
            if (transFile.getGroupList().get(i).name.equals(name)) return i;
        }
        return -1;
    }
    public String getGroupNameById(int groupId) {
        if (groupId >= 0 && groupId < getGroupCount()) {
            return getGroupNames().get(groupId);
        }
        return null;
    }

    public List<String> getGroupColors() {
        ArrayList<String> list = new ArrayList<>();
        for (MeoTransFile.Group group : transFile.getGroupList()) {
            list.add(group.color);
        }
        return list;
    }
    public String getGroupColorByName(String name) {
        return getGroupColorById(getGroupIdByName(name));
    }
    public String getGroupColorById(int groupId) {
        if (groupId >= 0 && groupId < getGroupCount()) {
            return getGroupColors().get(groupId);
        }
        return null;
    }

    public String getFileFolder() {
        return new File(filePath).getParent();
    }
    public String getBakFolder() {
        return getFileFolder() + File.separator + FOLDER_NAME_BAK;
    }
    public String getPicPathOf(String picName) {
        return getFileFolder() + File.separator + picName;
    }
    public String getCurrentPicPath() {
        return getPicPathOf(currentPicName);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getFilePath() {
        return filePath;
    }

    public boolean isMeoFile() {
        return isMeoFile(getFilePath());
    }
    public boolean isLPFile() {
        return isLPFile(getFilePath());
    }
    public static boolean isMeoFile(String filePath) {
        return filePath.endsWith(EXTENSION_MEO);
    }
    public static boolean isLPFile(String filePath) {
        return !isMeoFile(filePath);
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }
    public boolean isChanged() {
        return isChanged;
    }

    public void setViewMode(int viewMode) {
        this.viewMode = viewMode;
    }
    public int getViewMode() {
        return viewMode;
    }

    public void setWorkMode(int workMode) {
        this.workMode = workMode;
    }
    public int getWorkMode() {
        return workMode;
    }

    public void setCurrentGroupId(int groupId) {
        this.currentGroupId = groupId;
    }
    public int getCurrentGroupId() {
        return currentGroupId;
    }
    public String getCurrentGroupName() {
        return getGroupNameById(currentGroupId);
    }

    public void setCurrentPicName(String currentPicName) {
        this.currentPicName = currentPicName;
    }
    public String getCurrentPicName() {
        return currentPicName;
    }

    private ControllerAccessor controllerAccessor;
    public void setControllerAccessor(final ControllerAccessor newAccessor) {
        if (controllerAccessor != null) throw new IllegalStateException("Accessor already set");
        controllerAccessor = newAccessor;
    }
    public ControllerAccessor getControllerAccessor() {
        return controllerAccessor;
    }
    public interface ControllerAccessor {
        void close();
        void reset();

        void addLabelLayer();
        void updateLabelLayer(int index);
        void removeLabelLayer(int index);
        void updateLabelLayers();

        void updateTree();

        void updateGroupList();

        CTreeItem findLabelByIndex(int index);

        Object get(String field);
    }
}
