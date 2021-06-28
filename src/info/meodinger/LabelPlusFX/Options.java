package info.meodinger.LabelPlusFX;

import info.meodinger.LabelPlusFX.Property.Config;
import info.meodinger.LabelPlusFX.Property.RecentFiles;
import info.meodinger.LabelPlusFX.Property.Settings;
import info.meodinger.LabelPlusFX.Util.CDialog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Author: Meodinger
 * Date: 2021/6/27
 * Location: info.meodinger.LabelPlusFX
 */
public final class Options {

    private static final String LPFX = ".lpfx";
    private static final String FileName_Config = "config";
    private static final String FileName_Settings = "settings";
    private static final String FileName_RecentFiles = "recent_files";

    public static Path lpfx = Paths.get(System.getProperty("user.home")).resolve(LPFX);
    public static Path config = lpfx.resolve(FileName_Config);
    public static Path settings = lpfx.resolve(FileName_Settings);
    public static Path recentFiles = lpfx.resolve(FileName_RecentFiles);

    public static void init() {
        try {
            // project data folder
            if (Files.notExists(lpfx)) Files.createDirectories(lpfx);

            // config
            initConfig();
            // settings
            initSettings();
            // recent_files
            initRecentFiles();
        } catch (IOException e) {
            CDialog.showException(e);
        }
    }

    private static void initConfig() throws IOException {
        if (Files.notExists(config)) {
            Files.createFile(config);
            Config.Instance.save();
        }
        Config.Instance.load();
    }
    private static void initSettings() throws IOException {
        if (Files.notExists(settings)) {
            Files.createFile(settings);
            Settings.Instance.save();
        }
        Settings.Instance.load();
    }
    private static void initRecentFiles() throws IOException {
        if (Files.notExists(recentFiles)) {
            Files.createFile(recentFiles);
        }
        RecentFiles.Instance.load();
        Runtime.getRuntime().addShutdownHook(new Thread(RecentFiles.Instance::save));
    }
}
