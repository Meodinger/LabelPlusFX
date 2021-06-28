package info.meodinger.LabelPlusFX;

import info.meodinger.LabelPlusFX.Util.CDialog;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Author: Meodinger
 * Date: 2021/6/27
 * Location: info.meodinger.LabelPlusFX
 */
public final class Settings {

    public static final class Setting {
        public static final String LIST_SEPARATOR = "|";

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

    public static final Settings DefaultSettings = new Settings();
    static {
        DefaultSettings.settings.addAll(Arrays.asList(
                new Setting("DefaultColorList", "FF0000",
                        "0000FF", "008000", "1E90FF", "FFD700",
                        "FF00FF", "A0522D", "FF4500", "9400D3"
                ),
                new Setting("DefaultGroupList", "框外", "框内"),
                new Setting("MainDivider", 0.63),
                new Setting("RightDivider", 0.6)
        ));
    }

    private Settings() {}
    public static final Settings Instance = new Settings();

    private final List<Setting> settings = new ArrayList<>();

    public void load() {
        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(Options.settings.toFile()))) {
            settings.addAll(new ObjectMapper().readValue(is, Settings.class).settings);
        } catch (Exception e) {
            CDialog.showException(e);
            settings.addAll(DefaultSettings.settings);
        }
    }
    public void save() {
        try {
            OutputStream stream = Files.newOutputStream(Options.settings);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            String content = mapper.writeValueAsString(this);

            stream.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            CDialog.showException(e);
        }
    }

    public Setting get(String key) {
        for (Setting setting : settings) {
            if (setting.key.equals(key)) {
                return setting;
            }
        }
        return null;
    }
}
