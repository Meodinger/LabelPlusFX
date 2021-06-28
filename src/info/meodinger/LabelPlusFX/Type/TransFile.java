package info.meodinger.LabelPlusFX.Type;

import info.meodinger.LabelPlusFX.Util.CDialog;
import info.meodinger.LabelPlusFX.Util.CString;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.*;

/**
 * @author Meodinger
 * Date: 2021/5/18
 * Location: info.meodinger.LabelPlusFX.Type
 */
public abstract class TransFile<T> {

    public static final int[] DEFAULT_VERSION = new int[] {1, 0};
    public static final String DEFAULT_COMMENT = "Default Comment\nYou can edit me";
    public static final String[] DEFAULT_COMMENT_LIST = {
            DEFAULT_COMMENT, "由 MoeFlow.com 导出", "由MoeTra.com导出"
    };

    private int[] version;
    private String comment;
    @JsonAlias({"group", "groups"})
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
        String string = "";
        try {
            string = new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            CDialog.showException(e);
        }
        return string;
    }

    public static class MeoTransFile extends TransFile<MeoTransFile.Group> {

        @Override
        public MeoTransFile clone() {
            MeoTransFile translation = new MeoTransFile();

            translation.setVersion(this.getVersion().clone());
            translation.setGroupList(new ArrayList<>(this.getGroupList()));
            translation.setComment(this.getComment());
            translation.setTransMap(new HashMap<>(this.getTransMap()));

            return translation;
        }

        public static List<String> getSortedPicList(MeoTransFile file) {
            List<String> trimmed = CString.trimSame(new ArrayList<>(file.getTransMap().keySet()));
            if (trimmed != null && trimmed.size() > 2) {
                boolean canCastToNumberList = true;
                for (int i = 2; i < trimmed.size(); i++) {
                    if (!CString.isDigit(trimmed.get(i))) {
                        canCastToNumberList = false;
                        break;
                    }
                }
                if (canCastToNumberList) {
                    Map<Integer, Integer> map = new HashMap<>();
                    List<Integer> integerList = new ArrayList<>();
                    for (int i = 2; i < trimmed.size(); i++) {
                        int num = Integer.parseInt(trimmed.get(i));
                        integerList.add(num);
                        map.put(num, i);
                    }
                    integerList.sort(Comparator.naturalOrder());

                    int numberLength, complementLength;
                    ArrayList<String> list = new ArrayList<>();
                    for (Integer integer : integerList) {
                        numberLength = CString.lengthOf(integer);
                        complementLength = trimmed.get(map.get(integer)).length() - numberLength;
                        list.add(trimmed.get(0) + CString.repeat('0', complementLength) + integer + trimmed.get(1));
                    }
                    return list;
                }
            }

            Set<String> sorted = new TreeSet<>(Comparator.naturalOrder());
            sorted.addAll(file.getTransMap().keySet());
            return new ArrayList<>(sorted);
        }

        public static String toJsonString(MeoTransFile file) {
            TransFile.MeoTransFile cloned = file.clone();

            List<String> sorted = getSortedPicList(file);
            Map<String, List<TransLabel>> map = new LinkedHashMap<>();
            for (String key : sorted) {
                map.put(key, file.getTransMap().get(key));
            }
            cloned.setTransMap(map);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String content = "";
            try {
                content = mapper.writeValueAsString(cloned);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            return content;
        }

        public static class Group {

            public String name;
            public String color;

            // For jackson
            public Group() {}

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
