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

    private final FileChooser chooser;
    private final ObjectProperty<File> lastDirectory;

    public CFileChooser() {
        this.chooser = new FileChooser();
        this.lastDirectory = new SimpleObjectProperty<>(null);

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

    private void recordLastDirectory(File file) {
        if (file != null) {
            if (file.isDirectory()) lastDirectory.set(file);
            else lastDirectory.set(file.getParentFile());
        }
    }

    public void setTitle(String title) {
        chooser.setTitle(title);
    }

    public List<FileChooser.ExtensionFilter> getExtensionFilters() {
        return chooser.getExtensionFilters();
    }
}