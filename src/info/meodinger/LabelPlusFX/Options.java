package info.meodinger.LabelPlusFX;

import info.meodinger.LabelPlusFX.IO.RecentFiles;
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
    private static final String FileName_Settings = "settings.json";
    private static final String FileName_RecentFiles = "recent_files";

    public static Path lpfx = Paths.get(System.getProperty("user.home")).resolve(LPFX);
    public static Path settings = lpfx.resolve(FileName_Settings);
    public static Path recentFiles = lpfx.resolve(FileName_RecentFiles);

    public static void init() {
        try {
            if (Files.notExists(lpfx)) Files.createDirectories(lpfx);

            // settings.json
            if (Files.notExists(settings)) {
                Files.createFile(settings);
                Settings.DefaultSettings.save();
            }
            Settings.Instance.load();

            // recent_files
            if (Files.notExists(recentFiles)) {
                Files.createFile(recentFiles);
            }
            RecentFiles.Instance.load();
            Runtime.getRuntime().addShutdownHook(new Thread(RecentFiles.Instance::save));

        } catch (IOException e) {
            CDialog.showException(e);
        }
    }
}
