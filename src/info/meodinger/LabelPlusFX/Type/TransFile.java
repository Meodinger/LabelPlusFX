package info.meodinger.LabelPlusFX.Type;

import com.alibaba.fastjson.JSON;

import java.util.*;

/**
 * @author Meodinger
 * Date: 2021/5/18
 * Location: info.meodinger.LabelPlusFX.Type
 */
public abstract class TransFile<T> {

    public static final String DEFAULT_COMMENT = "Default Comment\nYou can edit me";
    public static final String[] DEFAULT_COMMENT_LIST = {
            DEFAULT_COMMENT, "由 MoeFlow.com 导出"
    };

    private int[] version;
    private String comment;
    private List<T> groupList;
    private Map<String, List<TransLabel>> transMap;

    public int[] getVersion() {
        return version;
    }
    public String getComment() {
        return comment;
    }
    public List<T> getGroupList() {
        return groupList;
    }
    public Map<String, List<TransLabel>> getTransMap() {
        return transMap;
    }

    public void setVersion(int[] version) {
        this.version = version;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public void setGroupList(List<T> groupList) {
        this.groupList = groupList;
    }
    public void setTransMap(Map<String, List<TransLabel>> transMap) {
        this.transMap = transMap;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static class MeoTransFile extends TransFile<MeoTransFile.Group> {

        public static String[] DEFAULT_COLOR_LIST = {
                "FF0000",
                "0000FF",
                "008000",
                "1E90FF",
                "FFD700",
                "FF00FF",
                "A0522D",
                "FF4500",
                "9400D3"
        };

        @Override
        public MeoTransFile clone() {
            MeoTransFile translation = new MeoTransFile();

            translation.setVersion(this.getVersion().clone());
            translation.setGroupList(new ArrayList<>(this.getGroupList()));
            translation.setComment(this.getComment());
            translation.setTransMap(new HashMap<>(this.getTransMap()));

            return translation;
        }

        public static class Group {

            public String name;
            public String color;

            public Group(String name, String color) {
                this.name = name;
                this.color = color;
            }
        }
    }

    public static class LPTransFile extends TransFile<String> {

        public final static String PIC_START = ">>>>>>>>[";
        public final static String PIC_END = "]<<<<<<<<";
        public final static String LABEL_START = "----------------[";
        public final static String LABEL_END = "]----------------";
        public final static String PROP_START = "[";
        public final static String PROP_END = "]";
        public final static String SPLIT = ",";
        public final static String SEPARATOR = "-";

    }

}
