package info.meodinger.LabelPlusFX.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Meodinger
 * Date: 2021/5/29
 * Location: info.meodinger.LabelPlusFX.Util
 */
public class CString {

    public static char[] WHITE_SPACE_ARRAY = new char[] {
      ' ', '\0', '\b', '\f', '\n', '\r', '\t'
    };

    public static String repeat(String str, int n) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            builder.append(str);
        }
        return builder.toString();
    }

    public static String repeat(char c, int n) {
        return repeat(String.valueOf(c), n);
    }

    public static <T> int lengthOf(T t) {
        return String.valueOf(t).length();
    }

    public static boolean isWhiteSpace(char c) {
        for (char w : WHITE_SPACE_ARRAY) {
            if (c == w) return true;
        }
        return false;
    }

    public static boolean isBlank(String str) {
        if (str == null) return true;
        if (str.isEmpty()) return true;

        char[] chars = str.toCharArray();
        int whiteCount = 0;
        for (char c : chars) {
            if (isWhiteSpace(c)) whiteCount++;
        }
        return whiteCount == chars.length;
    }

    public static boolean isDigit(String str) {
        if (str == null) return false;

        char[] chars = str.toCharArray();
        for (char c : chars) {
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    public static List<String> trimSame(List<String> strings) {

        if (strings == null) return null;
        if (strings.isEmpty()) return Arrays.asList("", "");
        if (strings.size() == 1) return Arrays.asList("", "", strings.get(0));

        ArrayList<String> trimmed = new ArrayList<>();

        char[] example = strings.get(0).toCharArray();
        int head = example.length, tail = example.length;
        for (int i = 1; i < strings.size(); i++) {
            char[] chars = strings.get(i).toCharArray();
            int range = Math.min(example.length, chars.length);

            for (int j = 0; j < range; j++) {
                if (chars[j] != example[j]) {
                    head = Math.min(j, head);
                    break;
                }
            }
            for (int j = 0; j < range; j++) {
                if (chars[chars.length - j - 1] != example[example.length - j - 1]) {
                    tail = Math.min(j, tail);
                    break;
                }
            }
        }

        trimmed.add(strings.get(0).substring(0, head));
        trimmed.add(strings.get(0).substring(strings.get(0).length() - tail));
        for (String string : strings) trimmed.add(string.substring(head, string.length() - tail));

        return trimmed;
    }
}
