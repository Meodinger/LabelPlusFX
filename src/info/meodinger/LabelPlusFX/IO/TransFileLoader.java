package info.meodinger.LabelPlusFX.IO;

import info.meodinger.LabelPlusFX.State;
import info.meodinger.LabelPlusFX.I18N;
import info.meodinger.LabelPlusFX.Type.*;
import info.meodinger.LabelPlusFX.Type.TransFile.LPTransFile;
import info.meodinger.LabelPlusFX.Type.TransFile.MeoTransFile;
import info.meodinger.LabelPlusFX.Util.CDialog;
import info.meodinger.LabelPlusFX.Util.CString;

import com.alibaba.fastjson.JSON;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Author: Meodinger
 * Date: 2021/5/17
 * Location: info.meodinger.LabelPlusFX.Util
 */
public abstract class TransFileLoader {

    private final State state;

    private TransFileLoader(State state) {
        this.state = state;
    }

    public abstract boolean load(File file);
    public boolean load(String path) {
        return load(new File(path));
    }
    public void setTransFile(MeoTransFile transFile) {
        state.setTransFile(transFile);
    }


    public static class LPFileLoader extends TransFileLoader {

        private static final int AHEAD_LIMIT = 100;

        private LPTransFile transFile;
        private BufferedReader reader;
        private String line;

        public LPFileLoader(State state) {
            super(state);
            init();
        }

        public void init() {
            transFile = new LPTransFile();
            transFile.setTransMap(new HashMap<>());
            transFile.setGroupList(new ArrayList<>());
            reader = null;
        }

        private void readHead() throws IOException {
            // Version
            line = reader.readLine();
            String[] v = line.split(LPTransFile.SPLIT);
            int[] version = new int[2];
            version[0] = Integer.parseInt(v[0]);
            version[1] = Integer.parseInt(v[1]);
            transFile.setVersion(version);

            // Separator
            line = reader.readLine();
            if (!LPTransFile.SEPARATOR.equals(line)) throw new IOException(String.format(I18N.FORMAT_UNEXPECTED_STRING, line));

            // Group Info and Separator
            int count = 0;
            while (!(line = reader.readLine()).equals(LPTransFile.SEPARATOR) && count < 9) {
                if (transFile.getGroupList().contains(line)) throw new IOException(String.format(I18N.FORMAT_REPEATED_GROUP_NAME, line));
                transFile.getGroupList().add(line);
                count++;
            }
            if (!line.equals(LPTransFile.SEPARATOR)) throw new IOException(I18N.EXPORTER_TOO_MANY_GROUPS);
        }

        private void readComment() throws IOException{
            transFile.setComment(parseText(new StringBuilder(), LPTransFile.PIC_START));
        }

        private void readContent() throws IOException {

            // Skip Empty Line
            while (CString.isBlank(line = reader.readLine()));

            while (line != null && line.startsWith(LPTransFile.PIC_START)) {
                transFile.getTransMap().put(parsePicHead(), parsePicBody());
            }

        }

        public String parsePicHead() {
            return line.replace(LPTransFile.PIC_START, "").replace(LPTransFile.PIC_END, "");
        }

        public ArrayList<TransLabel> parsePicBody() throws IOException {
            ArrayList<TransLabel> translations = new ArrayList<>();

            while ((line = reader.readLine()) != null && line.startsWith(TransFile.LPTransFile.LABEL_START)) {
                TransLabel label = parseTransLabel();

                for (TransLabel l : translations)
                    if (l.getIndex() == label.getIndex())
                        throw new IOException(String.format(I18N.FORMAT_REPEATED_LABEL_INDEX, l.getIndex()));

                translations.add(label);
            }
            // Empty Pic
            if (CString.isBlank(line)) line = reader.readLine();

            return translations;
        }

        private TransLabel parseTransLabel() throws IOException {
            String[] s = line.split(LPTransFile.LABEL_END);
            String[] props = s[1].replace(LPTransFile.PROP_START, "").replace(LPTransFile.PROP_END, "").split(LPTransFile.SPLIT);

            int index = Integer.parseInt(s[0].replace(LPTransFile.LABEL_START, ""));
            double x = Double.parseDouble(props[0]);
            double y = Double.parseDouble(props[1]);
            int groupId = Integer.parseInt(props[2]) - 1;

            if (index < 0) throw new IOException(String.format(I18N.FORMAT_INVALID_LABEL_INDEX, index));

            return new TransLabel(index, x, y, groupId, parseText());
        }

        private String parseText() throws IOException {
            return parseText(new StringBuilder(), LPTransFile.PIC_START, LPTransFile.LABEL_START);
        }

        private String parseText(StringBuilder text, String... stopMarks) throws IOException {
            while (!CString.isBlank(line = reader.readLine())) {
                for (String mark : stopMarks) {
                    if (line.startsWith(mark)) {
                        reader.reset();
                        return text.toString();
                    }
                }
                text.append(line).append("\n");
                reader.mark(AHEAD_LIMIT);
            }

            // Mark on potential text end
            reader.mark(AHEAD_LIMIT);

            line = reader.readLine();
            while (line != null && CString.isBlank(line)) {
                reader.mark(AHEAD_LIMIT);
                line = reader.readLine();

            }

            boolean flag = false;
            if (line == null) {
                flag = true;
            } else {
                for (String mark : stopMarks) {
                    if (line.startsWith(mark)) {
                        flag = true;
                        break;
                    }
                }
            }

            if (flag) {
                // Actual end, reset to the mark
                reader.reset();
                if (text.length() > 0) text.deleteCharAt(text.length() - 1);
                return text.toString();
            }

            text.append("\n").append(line).append("\n");
            return parseText(text, stopMarks);
        }

        @Override
        public boolean load(File file) {
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

                // Remove BOM (EF BB BF)
                int bytes;
                byte[] bom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
                byte[] buf = new byte[3];
                FileInputStream in = new FileInputStream(file);
                bytes = in.read(buf, 0, 3);

                if (bytes != 3) throw new IOException("Unexpected EOF");
                if (Arrays.equals(buf, bom)) {
                    char[] chars = new char[3];
                    reader.read(chars, 0, 1);
                }

                readHead();
                readComment();
                readContent();

                setTransFile(Convertor.lp2meo(transFile));
                reader.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                CDialog.showException(e);
                return false;
            } finally {
                init();
            }
        }
    }

    public static class MeoFileLoader extends TransFileLoader{

        public MeoFileLoader(State state) {
            super(state);
        }

        @Override
        public boolean load(File file) {
            try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
                setTransFile(JSON.parseObject(is, MeoTransFile.class));
                return true;
            } catch (Exception e) {
                CDialog.showException(e);
                return false;
            }
        }
    }
}
