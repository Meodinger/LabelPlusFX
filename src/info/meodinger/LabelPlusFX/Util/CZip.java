package info.meodinger.LabelPlusFX.Util;

import java.io.*;
import java.util.zip.*;


/**
 * Author: Meodinger
 * Date: 2021/5/31
 * Location: info.meodinger.LabelPlusFX.Util
 */
public class CZip {

    private final ZipOutputStream zip;

    public CZip(String zipFilePath) {
        ZipOutputStream zipOutputStream;
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(zipFilePath));
            zipOutputStream = new ZipOutputStream(bufferedOutputStream);
        } catch (IOException e) {
            CDialog.showException(e);
            throw new RuntimeException("Abort");
        }
        this.zip = zipOutputStream;
    }

    public void zip(File file, String path) throws IOException{
        if (file.isDirectory()) {
            path = path + "/";
            zip.putNextEntry(new ZipEntry(path));
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    zip(f, path + f);
                }
            }
        } else {
            zip(new FileInputStream(file), path);
        }
    }

    public void zip(InputStream inputStream, String path) throws IOException {
        zip.putNextEntry(new ZipEntry(path));

        BufferedInputStream input = new BufferedInputStream(inputStream);

        byte[] chunk = new byte[1024 * 4];
        int len;
        while ((len = input.read(chunk)) != -1) {
            zip.write(chunk, 0, len);
        }
        input.close();
        zip.closeEntry();
    }

    public void close() throws IOException {
        zip.close();
    }
}
