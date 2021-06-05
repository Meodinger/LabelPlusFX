package info.meodinger.LabelPlusFX;

import java.util.Locale;

/**
 * @author Meodinger
 * Date: 2021/5/28
 * Location: info.meodinger.LabelPlusFX
 */
public class I18N {
    public static String WINDOW_TITLE = "Label Plus FX";

    public static String NEW_TRANSLATION = "新建翻译文件";
    public static String OPEN_TRANSLATION = "打开翻译文件";
    public static String SAVE_TRANSLATION = "保存翻译文件";
    public static String EXPORT_TRANSLATION = "导出翻译文件";
    public static String EXPORT_TRANS_PACK = "导出翻译图包";

    public static String MEO_TRANS_FILE = "喵版翻译文件";
    public static String LP_TRANS_FILE = "原版翻译文件";
    public static String PACK_FILE = "图包文件";

    public static String EXIT = "退出";
    public static String SAVE_QUES = "是否保存？";
    public static String SAVE = "保存";
    public static String NOT_SAVE = "不保存";
    public static String SAVED_SUCCESSFULLY = "保存成功";
    public static String EXPORTED_SUCCESSFULLY = "导出成功";
    public static String EXPORTED_PACK_SUCCESSFULLY = "导出图包成功";
    public static String BAK_FILE_DELETED_FAILED = "备份文件清理失败";
    public static String AUTO_SAVE_NOT_AVAILABLE = "自动保存不可用";

    public static String HINT = "帮助";
    public static String HINT_CONTENT = "图片可以任意拖动\n" +
            "Alt/Ctrl + 滚轮 -> 图片缩放\n" +
            "标号模式：数字键 -> 更改分组\n" +
            "标号模式：鼠标左键 -> 放置标号\n" +
            "标号模式：鼠标右键 -> 移除标号\n" +
            "录入审查：点击标号 -> 快速修改";
    public static String ABOUT = "关于";
    public static String ABOUT_CONTENT = "Label Plus FX for version [1,0]\n" +
            "Author: Meodinger(meodinger@qq.com)\n" +
            "Version: 1.0.3 Bugfix 210603\n" +
            "Java Version: " + System.getProperty("java.version");

    public static String ERROR = "错误";
    public static String INFO = "信息";
    public static String ALERT = "警告";
    public static String CONFIRM = "确认";
    public static String OK = "好的";
    public static String YES = "是";
    public static String NO = "否";
    public static String SUBMIT = "提交";
    public static String CANCEL = "取消";

    public static String CHOOSE_PICS_TITLE = "添加图片";
    public static String PICS_POTENTIAL = "检测到的图片的文件";
    public static String PICS_SELECTED = "要导入的图片";
    public static String ADD_PIC = ">";
    public static String ADD_ALL_PIC = ">>";
    public static String REMOVE_PIC = "<";
    public static String REMOVE_ALL_PIC = "<<";

    public static String TOO_MANY_GROUPS = "分组太多了！";
    public static String GROUP_NAME_ALREADY_EXISTS = "已经存在同名分组";

    public static String FORMAT_TOO_MANY_GROUPS = "%d个分组对于原版LP来说太多了！";
    public static String FORMAT_UNEXPECTED_STRING = "出现了意料之外的文本：%s";
    public static String FORMAT_REPEATED_GROUP_NAME = "分组名字重复了：%s";
    public static String FORMAT_REPEATED_LABEL_INDEX = "Label的ID重复了：%d";
    public static String FORMAT_INVALID_LABEL_INDEX = "出现了意料之外的LabelID：%d";
    public static String FORMAT_NEW_GROUP_NAME = "新分组%d";
    public static String FORMAT_BAK_FILE_PATH = "备份文件位于: %s";

    public static String MENU_ITEM_ADD_GROUP = "添加分组";
    public static String MENU_ITEM_RENAME = "重命名";
    public static String MENU_ITEM_MOVE_TO = "移动到…";
    public static String MENU_ITEM_DELETE = "删除";

    public static String TITLE_ADD_GROUP = "添加分组";
    public static String TITLE_RENAME = "新名称";
    public static String TITLE_MOVE_TO = "选择分组";
    public static String TITLE_DELETE_LABEL = "删除Label";

    public static String CONTENT_ADD_GROUP = "请输入分组<名称>，并选择一个<颜色>";
    public static String CONTENT_RENAME = "请输入分组的新<名称>";
    public static String CONTENT_MOVE_TO = "选择这个Label的分组";
    public static String CONTENT_DELETE_LABEL = "确定要删除这个Label么？";

    public static String MM_FILE = "文件";
    public static String M_NEW = "新建翻译";
    public static String M_OPEN = "打开";
    public static String M_SAVE = "保存";
    public static String M_SAVE_AS = "另存为";
    public static String M_CLOSE = "关闭";
    public static String MM_EXPORT = "导出";
    public static String M_E_LP = "导出为LP格式文本";
    public static String M_E_MEO = "导出为Meo格式文本";
    public static String M_E_MEO_P = "导出为包含图片的压缩包";
    public static String MM_ABOUT = "关于";
    public static String M_HINT = "帮助";
    public static String M_ABOUT = "关于";

    public static String VIEW_GROUP = "分组浏览";
    public static String VIEW_INDEX = "顺序浏览";
    public static String WORK_CHECK = "审阅模式";
    public static String WORK_LABEL = "标号模式";
    public static String WORK_INPUT = "输入模式";

    public static void init() {
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage();
        if (!(lang.equals(Locale.SIMPLIFIED_CHINESE.getLanguage()) || lang.equals(Locale.TRADITIONAL_CHINESE.getLanguage()))) {

            NEW_TRANSLATION = "New Translation File";
            OPEN_TRANSLATION = "Open Translation File";
            SAVE_TRANSLATION = "Save Translation File";
            EXPORT_TRANSLATION = "Export Translation Pack";

            MEO_TRANS_FILE = "MeoTransFile";
            LP_TRANS_FILE = "LabelPlusFile";
            PACK_FILE = "PicPackFile";

            EXIT = "Exit";
            SAVE_QUES = "Save?";
            SAVE = "Save";
            NOT_SAVE = "Not Save";
            SAVED_SUCCESSFULLY = "Saved successfully";
            EXPORT_TRANSLATION = "Exported Successfully";
            EXPORTED_PACK_SUCCESSFULLY = "Exported PicPack Successfully";
            BAK_FILE_DELETED_FAILED = "Bak file remove failed";
            AUTO_SAVE_NOT_AVAILABLE = "Auto save not available";

            HINT = "Hint";
            HINT_CONTENT = "Pic can be dragged\n" +
                    "Alt/Ctrl + MouseScroll -> Scale pic\n" +
                    "(Label Mode) Number Key -> Change Group\n" +
                    "(Label Mode) Left Click -> Place Label\n" +
                    "(Label Mode) Right Click -> Remove Label\n" +
                    "(Check|Input) Click Label -> Quick Edit";
            ABOUT = "About";

            ERROR = "Error";
            INFO = "Info";
            ALERT = "Alert";
            CONFIRM = "Confirm";
            OK = "OK";
            YES = "Yes";
            NO = "No";
            SUBMIT = "Submit";
            CANCEL = "Cancel";

            CHOOSE_PICS_TITLE = "Add Pics";
            PICS_POTENTIAL = "Potential Pics";
            PICS_SELECTED = "Pics ready to load";

            TOO_MANY_GROUPS = "Too Many Groups";
            GROUP_NAME_ALREADY_EXISTS = "Group name already exists";

            FORMAT_TOO_MANY_GROUPS = "More than 9 groups (got %d) in this translation";
            FORMAT_UNEXPECTED_STRING = "Unexpected String: %s";
            FORMAT_REPEATED_GROUP_NAME = "Group name repeat: %s";
            FORMAT_REPEATED_LABEL_INDEX = "Label index repeat: %d";
            FORMAT_INVALID_LABEL_INDEX = "Label index invalid: %d";
            FORMAT_NEW_GROUP_NAME = "New_Group_%d";
            FORMAT_BAK_FILE_PATH = "Bak file path is: %s";

            TITLE_ADD_GROUP = "Add Group";
            TITLE_RENAME = "New Name";
            TITLE_MOVE_TO = "Select Group";
            TITLE_DELETE_LABEL = "Delete Label";

            MENU_ITEM_ADD_GROUP = "Add Group";
            MENU_ITEM_RENAME = "Rename";
            MENU_ITEM_MOVE_TO = "Move to ...";
            MENU_ITEM_DELETE = "Delete";

            CONTENT_ADD_GROUP = "Enter the <name> and choose a <color>";
            CONTENT_RENAME = "Enter the new <name> for the Group";
            CONTENT_MOVE_TO = "Select Group of this Label";
            CONTENT_DELETE_LABEL = "Are your sue to delete this label?";

            MM_FILE = "File";
            M_NEW = "New File";
            M_OPEN = "Open";
            M_SAVE = "Save";
            M_SAVE_AS = "Save As";
            M_CLOSE = "Close";
            MM_EXPORT = "Export";
            M_E_LP = "As LP File";
            M_E_MEO = "AS Meo File";
            M_E_MEO_P = "As Meo Pack with pics";
            MM_ABOUT = "About";
            M_HINT = "Hint";
            M_ABOUT = "About";

            VIEW_GROUP = "Group Mode";
            VIEW_INDEX = "Index Mode";
            WORK_CHECK = "Check Mode";
            WORK_LABEL = "Label Mode";
            WORK_INPUT = "Input Mode";

        }
    }

}
