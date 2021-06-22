package info.meodinger.LabelPlusFX;

import java.util.Locale;

/**
 * @author Meodinger
 * Date: 2021/5/28
 * Location: info.meodinger.LabelPlusFX
 */
public class I18N {
    public static String WINDOW_TITLE = "Label Plus FX";

    public static String CHOOSER_NEW_TRANSLATION = "新建翻译文件";
    public static String CHOOSER_OPEN_TRANSLATION = "打开翻译文件";
    public static String CHOOSER_SAVE_TRANSLATION = "保存翻译文件";
    public static String CHOOSER_BAK_FILE = "选择备份文件";
    public static String CHOOSER_RECOVERY = "请选择恢复位置";
    public static String CHOOSER_EXPORT_TRANSLATION = "导出翻译文件";
    public static String CHOOSER_EXPORT_TRANS_PACK = "导出翻译图包";

    public static String FILE_TRANSLATION = "翻译文件";
    public static String FILE_MEO_TRANSLATION = "喵版翻译文件";
    public static String FILE_LP_TRANSLATION = "原版翻译文件";
    public static String FILE_BACKUP = "备份文件";
    public static String FILE_PIC_PACK = "图包文件";

    public static String INFO_SAVED_SUCCESSFULLY = "保存成功";
    public static String INFO_EXPORTED_SUCCESSFULLY = "导出成功";
    public static String INFO_EXPORTED_PACK_SUCCESSFULLY = "导出图包成功";

    public static String ALERT_SAVE_FAILED = "保存失败";
    public static String ALERT_EXPORT_FAILED = "导出失败";
    public static String ALERT_BAK_FAILED = "备份失败";
    public static String ALERT_BAK_FILE_DELETE_FAILED = "备份文件清理失败";
    public static String ALERT_AUTO_SAVE_NOT_AVAILABLE = "自动保存不可用";

    public static String HINT = "帮助";
    public static String HINT_CONTENT = "图片可以任意拖动\n" +
            "Alt/Ctrl/Meta + 滚轮 -> 图片缩放\n" +
            "标号模式：数字按键 -> 更改分组\n" +
            "标号模式：鼠标左键 -> 放置标号\n" +
            "标号模式：鼠标右键 -> 移除标号\n" +
            "录入审查：点击标号 -> 快速修改";
    public static String HINT_LINK = "更多用法请点击此处";
    public static String HINT_LINK_URL = "https://www.kdocs.cn/l/cpRyDN2Perkb";

    public static String ABOUT = "关于";
    public static String ABOUT_CONTENT = "Label Plus FX for LabelPlus version [1,0]\n\n" +
            "Version: 1.1.1\n" +
            "Author: Meodinger\n" +
            "Email: meodinger@qq.com\n";
    public static String ABOUT_LINK = "Github Repository";
    public static String ABOUT_LINK_URL = "https://github.com/Meodinger/LabelPlusFX";

    public static String ERROR = "错误";
    public static String INFO = "信息";
    public static String ALERT = "警告";
    public static String CONFIRM = "确认";
    public static String OK = "好的";
    public static String YES = "是";
    public static String NO = "否";
    public static String SUBMIT = "提交";
    public static String CANCEL = "取消";
    public static String EXIT = "退出";
    public static String SAVE_QUES = "是否保存？";
    public static String SAVE = "保存";
    public static String NOT_SAVE = "不保存";

    public static String CHOOSE_PICS_TITLE = "添加图片";
    public static String PICS_POTENTIAL = "检测到的图片的文件";
    public static String PICS_SELECTED = "要导入的图片";
    public static String ADD_PIC = ">";
    public static String ADD_ALL_PIC = ">>";
    public static String REMOVE_PIC = "<";
    public static String REMOVE_ALL_PIC = "<<";

    public static String EXPORTER_TOO_MANY_GROUPS = "分组太多了！";
    public static String EXPORTER_SAME_GROUP_NAME = "存在同名分组";

    public static String FORMAT_TOO_MANY_GROUPS = "%d个分组对于原版LP来说太多了！";
    public static String FORMAT_UNEXPECTED_STRING = "出现了意料之外的文本：%s";
    public static String FORMAT_REPEATED_GROUP_NAME = "分组名字重复了：%s";
    public static String FORMAT_REPEATED_LABEL_INDEX = "Label的ID重复了：%d";
    public static String FORMAT_INVALID_LABEL_INDEX = "出现了意料之外的LabelID：%d";
    public static String FORMAT_NEW_GROUP_NAME = "新分组%d";
    public static String FORMAT_SAVE_FAILED_BAK_PATH = "保存失败！\n备份文件位于: %s";

    public static String MENU_ITEM_ADD_GROUP = "添加分组";
    public static String MENU_ITEM_RENAME = "重命名";
    public static String MENU_ITEM_MOVE_TO = "移动到…";
    public static String MENU_ITEM_DELETE = "删除";

    public static String DIALOG_TITLE_ADD_GROUP = "添加分组";
    public static String DIALOG_TITLE_RENAME = "新名称";
    public static String DIALOG_TITLE_MOVE_TO = "选择分组";
    public static String DIALOG_TITLE_DELETE_LABEL = "删除Label";
    public static String DIALOG_TITLE_EDIT_COMMENT = "编辑注释";

    public static String DIALOG_CONTENT_ADD_GROUP = "请输入分组<名称>，并选择一个<颜色>";
    public static String DIALOG_CONTENT_RENAME = "请输入分组的新<名称>";
    public static String DIALOG_CONTENT_MOVE_TO = "选择这个Label的分组";
    public static String DIALOG_CONTENT_DELETE_LABEL = "确定要删除这个Label么？";
    public static String DIALOG_CONTENT_DELETE_LABELS = "确定要删除这些Label么？";
    public static String DIALOG_CONTENT_SAVE_AS_ALERT = "你正在将翻译文件另存到非当前项目文件夹\n" +
            "此操作可能导致加载图片失败！\n" +
            "如果想换一种格式保存翻译文件，请使用“导出”。\n" +
            "选择“是”以继续";

    public static String MM_FILE = "文件";
    public static String M_NEW = "新建翻译";
    public static String M_OPEN = "打开";
    public static String M_SAVE = "保存";
    public static String M_SAVE_AS = "另存为";
    public static String M_BAK_RECOVERY = "从备份中恢复";
    public static String M_CLOSE = "关闭";
    public static String MM_EXPORT = "导出";
    public static String M_E_LP = "导出为LP格式文本";
    public static String M_E_MEO = "导出为Meo格式文本";
    public static String M_E_MEO_P = "导出包含图片的压缩包";
    public static String M_E_COMMENT = "编辑注释";
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

            CHOOSER_NEW_TRANSLATION = "New Translation File";
            CHOOSER_OPEN_TRANSLATION = "Open Translation File";
            CHOOSER_SAVE_TRANSLATION = "Save Translation File";
            CHOOSER_BAK_FILE = "Choose Backup file";
            CHOOSER_RECOVERY = "Choose Recovery Location";
            CHOOSER_EXPORT_TRANSLATION = "Export Translation Pack";
            CHOOSER_EXPORT_TRANS_PACK = "Export Trans File Pack";

            FILE_TRANSLATION = "Translation File";
            FILE_MEO_TRANSLATION = "MeoTransFile";
            FILE_LP_TRANSLATION = "LabelPlusFile";
            FILE_BACKUP = "Backup File";
            FILE_PIC_PACK = "PicPackFile";

            INFO_SAVED_SUCCESSFULLY = "Saved successfully";
            INFO_EXPORTED_SUCCESSFULLY = "Exported Successfully";
            INFO_EXPORTED_PACK_SUCCESSFULLY = "Exported PicPack Successfully";

            ALERT_SAVE_FAILED = "Save failed";
            ALERT_EXPORT_FAILED = "Export failed";
            ALERT_BAK_FAILED = "Backup failed";
            ALERT_BAK_FILE_DELETE_FAILED = "Bak file remove failed";
            ALERT_AUTO_SAVE_NOT_AVAILABLE = "Auto save not available";

            HINT = "Hint";
            HINT_CONTENT = "Pic can be dragged\n" +
                    "Alt/Ctrl/Meta + MouseScroll -> Scale pic\n" +
                    "(Label Mode) Number Key -> Change Group\n" +
                    "(Label Mode) Left Click -> Place Label\n" +
                    "(Label Mode) Right Click -> Remove Label\n" +
                    "(Check|Input) Click Label -> Quick Edit";
            HINT_LINK = "Click this for more information";
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
            EXIT = "Exit";
            SAVE_QUES = "Save?";
            SAVE = "Save";
            NOT_SAVE = "Not Save";

            CHOOSE_PICS_TITLE = "Add Pics";
            PICS_POTENTIAL = "Potential Pics";
            PICS_SELECTED = "Pics ready to load";

            EXPORTER_TOO_MANY_GROUPS = "Too Many Groups";
            EXPORTER_SAME_GROUP_NAME = "Groups with same name";

            FORMAT_TOO_MANY_GROUPS = "More than 9 groups (got %d) in this translation";
            FORMAT_UNEXPECTED_STRING = "Unexpected String: %s";
            FORMAT_REPEATED_GROUP_NAME = "Group name repeat: %s";
            FORMAT_REPEATED_LABEL_INDEX = "Label index repeat: %d";
            FORMAT_INVALID_LABEL_INDEX = "Label index invalid: %d";
            FORMAT_NEW_GROUP_NAME = "New_Group_%d";
            FORMAT_SAVE_FAILED_BAK_PATH = "Save Failed!\nBak file path is: %s";

            DIALOG_TITLE_ADD_GROUP = "Add Group";
            DIALOG_TITLE_RENAME = "New Name";
            DIALOG_TITLE_MOVE_TO = "Select Group";
            DIALOG_TITLE_DELETE_LABEL = "Delete Label";
            DIALOG_TITLE_EDIT_COMMENT = "Edit Comment";

            MENU_ITEM_ADD_GROUP = "Add Group";
            MENU_ITEM_RENAME = "Rename";
            MENU_ITEM_MOVE_TO = "Move to ...";
            MENU_ITEM_DELETE = "Delete";

            DIALOG_CONTENT_ADD_GROUP = "Enter the <name> and choose a <color>";
            DIALOG_CONTENT_RENAME = "Enter the new <name> for the Group";
            DIALOG_CONTENT_MOVE_TO = "Select Group of this Label";
            DIALOG_CONTENT_DELETE_LABEL = "Are your sue to delete this label?";
            DIALOG_CONTENT_DELETE_LABELS = "Are your sue to delete these labels?";
            DIALOG_CONTENT_SAVE_AS_ALERT = "You are saving translation to non-project folder\n" +
                    "This may cause load pic fail!\n" +
                    "If you want to save translation as the other format\n" +
                    "Please use `Export`\n" +
                    "Click YES to continue";

            MM_FILE = "File";
            M_NEW = "New File";
            M_OPEN = "Open";
            M_SAVE = "Save";
            M_SAVE_AS = "Save As";
            M_BAK_RECOVERY = "Recover form Backup";
            M_CLOSE = "Close";
            MM_EXPORT = "Export";
            M_E_LP = "As LP File";
            M_E_MEO = "AS Meo File";
            M_E_MEO_P = "As Meo Pack with pics";
            M_E_COMMENT = "Edit comment";
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
