package info.meodinger.LabelPlusFX.Property;

import info.meodinger.LabelPlusFX.Options;
import info.meodinger.LabelPlusFX.Util.CDialog;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Author: Meodinger
 * Date: 2021/6/27
 * Location: info.meodinger.LabelPlusFX
 */
public final class Settings {

    private final List<Setting> settings;

    private Settings() {
        this.settings = new ArrayList<>(Arrays.asList(
                new Setting("DefaultColorList", "FF0000",
                        "0000FF", "008000", "1E90FF", "FFD700",
                        "FF00FF", "A0522D", "FF4500", "9400D3"
                ),
                new Setting("DefaultGroupList", "框外", "框内"),
                new Setting("MainDivider", 0.63),
                new Setting("RightDivider", 0.6)
        ));
    }
    public static final Settings Instance = new Settings();

    public void load() {
        try (BufferedReader reader = Files.newBufferedReader(Options.settings)) {
            List<Setting> all = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (line.trim().startsWith(Setting.COMMENT_HEAD)) continue;

                String[] props = line.split(Setting.KEY_VALUE_SEPARATOR, 2);
                all.add(new Setting(props[0], props[1]));
            }

            settings.clear();
            settings.addAll(all);
        } catch (Exception e) {
            CDialog.showException(e);
        }
    }
    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(Options.settings)) {
            for (Setting setting : settings) {
                writer.write(setting.key + Setting.KEY_VALUE_SEPARATOR + setting.value);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            CDialog.showException(e);
        }
    }

    public Setting get(Key key) {
        for (Setting setting : settings) {
            if (setting.key.equals(key.key)) {
                return setting;
            }
        }
        throw new IllegalStateException("Setting not found");
    }

    public static final class Setting {
        public static final String LIST_SEPARATOR = "|";
        public static final String KEY_VALUE_SEPARATOR = "=";
        public static final String COMMENT_HEAD = "#";

        public final String key;
        private String value;

        Setting(String key, String value) {
            this.key = key;
            this.value = value;
        }
        <T extends Number> Setting(String key, T value) {
            this.key = key;
            this.value = String.valueOf(value);
        }
        Setting(String key, String... list) {
            this(key, Arrays.asList(list));
        }
        Setting(String key, List<String> list) {
            this.key = key;
            this.value = parseList(list);
        }

        public String get() {
            return value;
        }
        public int asInteger() {
            return Integer.parseInt(value);
        }
        public double asDouble() {
            return Double.parseDouble(value);
        }
        public List<String> asList() {
            return Arrays.asList(value.split("\\|"));
        }

        public void set(String value) {
            this.value = value;
        }
        public <T extends Number> void set(T value) {
            set(String.valueOf(value));
        }
        public void set(String... list) {
            set(parseList(list));
        }
        public void set(List<String> list) {
            set(parseList(list));
        }

        public static String parseList(String... values) {
            return parseList(Arrays.asList(values));
        }
        public static String parseList(List<String> values) {
            StringBuilder builder = new StringBuilder();
            for (String value : values) {
                builder.append(value).append(LIST_SEPARATOR);
            }
            builder.deleteCharAt(builder.length() - 1);
            return builder.toString();
        }
    }

    public enum Key {
        DefaultColorList("DefaultColorList"),
        DefaultGroupList("DefaultGroupList"),
        MainDivider("MainDivider"),
        RightDivider("RightDivider");

        public final String key;
        Key(String key) {
            this.key = key;
        }
    }
}
