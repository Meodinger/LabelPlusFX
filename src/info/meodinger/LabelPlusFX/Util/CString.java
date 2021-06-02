package info.meodinger.LabelPlusFX.Util;

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

    public static String strip(String str) {
        if (str == null) return null;
        char[] chars = str.toCharArray();

        int left = 0, right = 0;
        for (int i = 0; i < chars.length; i ++) {
            if (!isWhiteSpace(chars[i])) {
                left = i;
                break;
            }
        }
        for (int i = chars.length - 1; i > 0; i --) {
            if (!isWhiteSpace(chars[i])) {
                right = i;
                break;
            }
        }
        return str.substring(0, right + 1).substring(left);

    }
}
