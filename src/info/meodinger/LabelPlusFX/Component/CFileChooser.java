package info.meodinger.LabelPlusFX.Component;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.List;

/**
 * Author: Meodinger
 * Date: 2021/6/15
 * Location: info.meodinger.LabelPlusFX.Component
 */
public class CFileChooser {

    public static final ObjectProperty<File> lastDirectory = new SimpleObjectProperty<>(new File(System.getProperty("user.home")));
    public void setLastDirectory(File file) {
        recordLastDirectory(file);
    }
    private static void recordLastDirectory(File file) {
        if (file != null) {
            if (file.isDirectory()) lastDirectory.set(file);
            else lastDirectory.set(file.getParentFile());
        }
    }

    private final FileChooser chooser;

    public CFileChooser() {
        this.chooser = new FileChooser();

        this.chooser.initialDirectoryProperty().bind(lastDirectory);
    }

    public File showOpenDialog(Window owner) {
        File file = chooser.showOpenDialog(owner);
        recordLastDirectory(file);
        return file;
    }
    public File showSaveDialog(Window owner) {
        File file = chooser.showSaveDialog(owner);
        recordLastDirectory(file);
        return file;
    }

    public void setTitle(String title) {
        chooser.setTitle(title);
    }

    public List<FileChooser.ExtensionFilter> getExtensionFilters() {
        return chooser.getExtensionFilters();
    }
}