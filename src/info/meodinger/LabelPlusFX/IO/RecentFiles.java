package info.meodinger.LabelPlusFX.IO;

import info.meodinger.LabelPlusFX.Options;
import info.meodinger.LabelPlusFX.Util.CDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Author: Meodinger
 * Date: 2021/6/27
 * Location: info.meodinger.LabelPlusFX.IO
 */
public final class RecentFiles {

    private static final int MAX_SIZE = 10;

    private final List<String> list;

    private RecentFiles() {
        this.list = new LinkedList<>();
    }
    public static final RecentFiles Instance = new RecentFiles();

    public void load() {
        try {
            Path p = Options.recentFiles;
            if (Files.notExists(p)) return;

            try (BufferedReader reader = Files.newBufferedReader(p)) {
                reader.lines().forEach(this::add);
            }

        } catch (Exception e) {
            CDialog.showException(e);
        }
    }
    public void save() {
        try {
            Path p = Options.recentFiles;
            if (Files.notExists(p)) Files.createFile(p);

            try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(p))) {
                for (String recentFile : list) {
                    pw.println(recentFile);
                }
            }
        } catch (IOException e) {
            CDialog.showException(e);
        }
    }

    public List<String> getAll() {
        return list;
    }
    public String getLastOpenFile() {
        return list.get(0);
    }

    public void add(String path) {
        list.remove(path);
        list.add(0, path);

        if (list.size() > MAX_SIZE) {
            list.remove(MAX_SIZE);
        }
    }
    public void add(File file) {
        add(file.getPath());
    }

    public void remove(String path) {
        list.remove(path);
    }
}
