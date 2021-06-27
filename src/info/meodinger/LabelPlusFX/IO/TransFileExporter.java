package info.meodinger.LabelPlusFX.IO;

import info.meodinger.LabelPlusFX.State;
import info.meodinger.LabelPlusFX.I18N;
import info.meodinger.LabelPlusFX.Type.TransFile.LPTransFile;
import info.meodinger.LabelPlusFX.Type.TransFile.MeoTransFile;
import info.meodinger.LabelPlusFX.Type.TransLabel;
import info.meodinger.LabelPlusFX.Util.CDialog;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Meodinger
 * Date: 2021/5/19
 * Location: info.meodinger.LabelPlusFX.Util
 */
public abstract class TransFileExporter {

    private final State state;

    private TransFileExporter(State state) {
        this.state = state;
    }

    public abstract boolean export(File file);
    public boolean export(String path) {
        return export(new File(path));
    }
    public boolean export() {
        return export(state.getFilePath());
    }
    public State getState() {
        return state;
    }

    public static class LPFileExporter extends TransFileExporter {

        public LPFileExporter(State state) {
            super(state);
        }

        private boolean validate() {

            // Group count validate
            int groupCount = getState().getGroupCount();
            if (groupCount > 9) {
                CDialog.showInfo(String.format(I18N.FORMAT_TOO_MANY_GROUPS, groupCount));
                return false;
            }

            // Group name validate
            List<MeoTransFile.Group> groups = getState().getGroupList();
            Set<String> groupNames = new HashSet<>();
            for (MeoTransFile.Group group : groups) {
                groupNames.add(group.name);
            }
            if (groupNames.size() < groups.size()) {
                CDialog.showInfo(I18N.EXPORTER_SAME_GROUP_NAME);
                return false;
            }

            return true;
        }

        private String exportVersion() {
            int[] version = getState().getVersion();
            return version[0] + LPTransFile.SPLIT + version[1];
        }

        private String exportGroup() {
            List<MeoTransFile.Group> group = getState().getGroupList();
            StringBuilder gBuilder = new StringBuilder();
            for (MeoTransFile.Group g : group) {
                gBuilder.append(g.name).append("\n");
            }
            return gBuilder.deleteCharAt(gBuilder.length() - 1).toString();
        }

        private String exportTranslation() {
            StringBuilder tBuilder = new StringBuilder();

            for (String picName : getState().getSortedPicList()) {
                tBuilder.append(buildPic(picName));
            }
            return tBuilder.toString();
        }

        private String buildPic(String picName) {
            StringBuilder builder = new StringBuilder();

            builder.append(LPTransFile.PIC_START).append(picName).append(LPTransFile.PIC_END).append("\n");

            List<TransLabel> tList = getState().getTransMap().get(picName);
            for (TransLabel label : tList) {
                builder.append(buildLabel(label)).append("\n");
            }
            builder.append("\n");

            return builder.toString();
        }

        private String buildLabel(TransLabel translabel) {
            return LPTransFile.LABEL_START + translabel.getIndex() + LPTransFile.LABEL_END +
                    LPTransFile.PROP_START +
                    translabel.getX() + LPTransFile.SPLIT +
                    translabel.getY() + LPTransFile.SPLIT +
                    (translabel.getGroupId() + 1) +
                    LPTransFile.PROP_END + "\n" +
                    translabel.getText() + "\n";
        }

        @Override
        public boolean export(File file) {
            if (!validate()) return false;

            StringBuilder builder = new StringBuilder();

            String vString = exportVersion();
            String gString = exportGroup();
            String tString = exportTranslation();

            builder.append(vString).append("\n")
                    .append(LPTransFile.SEPARATOR).append("\n")
                    .append(gString).append("\n")
                    .append(LPTransFile.SEPARATOR).append("\n")
                    .append(getState().getComment()).append("\n")
                    .append("\n").append("\n")
                    .append(tString);

            try (FileOutputStream os = new FileOutputStream(file);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))
            ) {
                // Write BOM (EF BB BF)
                os.write(new byte[] {
                        (byte) 0xEF,
                        (byte) 0xBB,
                        (byte) 0xBF
                });

                writer.write(builder.toString());
                return true;
            } catch (Exception e) {
                CDialog.showException(e);
                return false;
            }
        }
    }

    public static class MeoFileExporter extends TransFileExporter {

        public MeoFileExporter(State state) {
            super(state);
        }

        @Override
        public boolean export(File file) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(MeoTransFile.toJsonString(getState().getTransFile()));

                return true;
            } catch (Exception e) {
                CDialog.showException(e);
                return false;
            }
        }
    }

}
