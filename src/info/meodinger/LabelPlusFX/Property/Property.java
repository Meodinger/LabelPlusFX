package info.meodinger.LabelPlusFX.Property;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Meodinger
 * Date: 2021/6/28
 * Location: info.meodinger.LabelPlusFX.Property
 */
public class Property {

    public static final String LIST_SEPARATOR = "|";
    public static final String KEY_VALUE_SEPARATOR = "=";
    public static final String COMMENT_HEAD = "#";

    public final String key;
    private String value;

    Property(String key, String value) {
        this.key = key;
        this.value = value;
    }
    <T extends Number> Property(String key, T value) {
        this.key = key;
        this.value = String.valueOf(value);
    }
    Property(String key, String... list) {
            this(key, Arrays.asList(list));
    }
    Property(String key, List<String> list) {
        this.key = key;
        this.value = parseList(list);
    }

    public String get() {
        return value;
    }

    public String asString() {
        return get();
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

    final static class Key {

        final String key;
        Key(String key) {
            this.key = key;
        }
    }
}
