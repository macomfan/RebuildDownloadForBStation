import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class main {
    // Hard code the parameter
    private static String inputFolder_ = "C:\\dev\\ffmpegBin\\files";
    private static String ffmpegBinFolder_ = "C:\\dev\\ffmpegBin\\bin";
    private static String outputFolder_ = "C:\\dev\\ffmpegBin\\output";
    private static String tempFolderName_ = "temp";
    private static String objectFileName_ = "entry.json";

    public static void main(String[] args) {
        String tempFolder = outputFolder_ + File.separator + tempFolderName_;
        try {
            FileHelper.checkOrCreateFolder(outputFolder_);
            FileHelper.checkOrCreateFolder(tempFolder);
        } catch (Exception e) {
            System.err.println("[ERR] Cannot create output folder: " + outputFolder_);
        }
        try {
            FileHelper.makeSureFileExisted(ffmpegBinFolder_);
        } catch (Exception e) {
            System.err.println("[ERR] Cannot find the ffmpeg bin folder: " + ffmpegBinFolder_);
        }
        FFmpeg.instance().setRuntimeFolder(ffmpegBinFolder_);
        List<String> objectFolder = findFolderIncludedFile(inputFolder_, objectFileName_);
        for (String path : objectFolder) {
            try {
                File objectFile = new File(path + File.separator + objectFileName_);
                JSONObject jsonObject = readJsonFile(objectFile.getAbsolutePath());
                MediaContext context = new MediaContext();
                context.Title = jsonObject.getString("title");
                context.Tag = jsonObject.getString("type_tag");
                context.BaseFolder = path;
                context.MediaType = jsonObject.getString("media_type");
                context.OutputFolder = outputFolder_;
                context.TempFolder = tempFolder;
                JSONObject page = jsonObject.getJSONObject("page_data");
                if (page != null) {
                    context.Page = page.getString("page");
                }
                if (context.MediaType.equals("1")) {
                    Media1 media1 = new Media1(context);
                    media1.startConvert();
                } else if (context.MediaType.equals("2")) {
                    Media2 media2 = new Media2(context);
                    media2.startConvert();
                } else {
                    System.err.println("[ERR] Unknown media type in: " + path);
                }
                System.out.println("[INFO] Convert done");
            } catch (Exception e) {
                System.err.println("[ERR] Convert failed: " + e.getMessage());
            }

        }
    }

    private static List<String> findFolderIncludedFile(String path, String specifiedFileName) {
        List<String> folders = new LinkedList<String>();
        File file = new File(path);
        File[] fileList = file.listFiles();
        for (File f : fileList) {
            if (f.isDirectory()) {
                folders.addAll(findFolderIncludedFile(f.getPath(), specifiedFileName));
            } else {
                if (f.getName().equals(specifiedFileName)) {
                    folders.add(path);
                }
            }
        }
        return folders;
    }

    private static JSONObject readJsonFile(String fileName) {
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            String jsonStr = sb.toString();
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            return jsonObject;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
