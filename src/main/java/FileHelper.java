import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FileHelper {
    public static boolean fileExist(String filename) {
        File file = new File(filename);
        return file.exists();
    }

    public static void makeSureFileExisted(String filename) throws Exception {
        if (!fileExist(filename)) {
            throw new Exception("File is not exist: " + filename);
        }
    }

    public static String checkOrRenameOutputFileName(String filename) {
        if (fileExist(filename)) {
            String newFilename = filename;
            // TODO
            System.out.println("[WARN] Rename the file to: " + newFilename);
            return newFilename;
        } else {
            return filename;
        }
    }

    public static void checkOrCreateFolder(String path) throws Exception {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.mkdir();
        makeSureFileExisted(path);
    }

    public static List<String> findFiles(String objectPath, String fileExt) throws Exception {
        List<String> files = new LinkedList<>();
        File path = new File(objectPath);
        if (!path.exists()) {
            throw new Exception("Cannot find folder by Tag: " + objectPath);
        }
        File[] fileList = path.listFiles();
        for (File file : fileList) {
            if (file.isDirectory()) {
                continue;
            }
            String fileName = file.getName();
            String ext = fileName.substring(fileName.lastIndexOf("."));
            if (ext.equals(fileExt)) {
                files.add(file.getAbsolutePath());
            }
        }
        return files;
    }

    public static String getFileName(String filename) {
        File file = new File(filename);
        return file.getName();
    }

    public static String renameFileExt(String filename, String ext) {
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            filename += ".unknown";
        }
        String tmp = filename.substring(index == -1 ? 0 : index);
        if (!tmp.equals(ext)) {
            String newFilename = filename.substring(0, filename.lastIndexOf(".")) + ext;
            return newFilename;
        }
        return filename;
    }

    public static void deleteFiles(List<String> files) {
        for (String filename : files) {
            File file = new File(filename);
            file.delete();
        }
    }
}
