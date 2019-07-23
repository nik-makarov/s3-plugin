package info.makarov.s3.core.utils;

import info.makarov.s3.core.exception.S3PluginException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static String generateTempFilename(String bucket, String filename) {
        String tmpdir = System.getProperty("java.io.tmpdir");
        String folder = "s3-plugin";
        Path path = Paths.get(tmpdir, folder, bucket, filename);
        File file = new File(path.toUri());
        file.mkdirs();
        if (file.exists()) {
            boolean deleteResult = file.delete();
            if (!deleteResult) {
                throw new S3PluginException("Cannot to delete " + path);
            }
        }
        return file.getAbsolutePath();
    }

}
