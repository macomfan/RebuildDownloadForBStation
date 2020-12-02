import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class main {
    // Hard code the parameter
    private static String inputFolder_ = "C:\\dev\\ffmpegBin\\files";
    private static String ffmpegBinFolder_ = "C:\\dev\\ffmpegBin\\bin";
    private static String outputFolder_ = "C:\\dev\\ffmpegBin\\output";
    private static int createFolderIfPageGreater_ = 3;
    private static String tempFolderName_ = "temp";
    private static String objectFileName_ = "entry.json";
    private static boolean listOutputFileOnly_ = false;
    private static AtomicInteger fileNumber_ = new AtomicInteger(0);
    private static String tempFolder_;

    private static ExecutorService fixedThreadPool_ = Executors.newFixedThreadPool(6);

    public static void doConvert(String path, int numOfPages) {
        fileNumber_.incrementAndGet();
        fixedThreadPool_.execute(() -> {
            try {
                String outputFolder = outputFolder_;
                File objectFile = new File(path + File.separator + objectFileName_);
                JSONObject jsonObject = readJsonFile(objectFile.getAbsolutePath());
                MediaContext context = new MediaContext();
                context.Title = jsonObject.getString("title");
                context.Tag = jsonObject.getString("type_tag");
                context.BaseFolder = path;
                context.MediaType = jsonObject.getString("media_type");
                context.OutputFolder = outputFolder;
                context.TempFolder = tempFolder_;
                JSONObject page = jsonObject.getJSONObject("page_data");
                if (page != null) {
                    context.Page = page.getString("page");
                }
                if (numOfPages > createFolderIfPageGreater_) {
                    outputFolder = outputFolder_ + "\\" + FileHelper.correctFileName(jsonObject.getString("title"));
                    try {
                        FileHelper.checkOrCreateFolder(outputFolder);
                    } catch (Exception e) {
                        System.err.println("[ERR] Cannot create output folder: " + outputFolder_);
                    }
                    context.Title = page.getString("part");
                    ;
                    context.Page = "";
                    context.OutputFolder = outputFolder;
                }
                String outputFilename = "";
                MediaBase media = null;
                if (context.MediaType.equals("1")) {
                    media = new Media1(context);
                } else if (context.MediaType.equals("2")) {
                    media = new Media2(context);
                } else {
                    System.err.println("[ERR] Unknown media type in: " + path);
                }
                if (!listOutputFileOnly_) {
                    System.out.println("[INFO] Working on " + path);
                    media.startConvert();
                    outputFilename = media.outputFileName_;
                    System.out.println("[INFO] Convert done: " + outputFilename);
                } else {
                    System.out.println("[INFO] File: " + media.getOutputFileName());
                }

            } catch (Exception e) {
                System.err.println("[ERR] Convert failed: " + e.getMessage());
            }
            finally {
                fileNumber_.decrementAndGet();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        tempFolder_ = outputFolder_ + File.separator + tempFolderName_;
        try {
            FileHelper.checkOrCreateFolder(outputFolder_);
            FileHelper.checkOrCreateFolder(tempFolder_);
        } catch (Exception e) {
            System.err.println("[ERR] Cannot create output folder: " + outputFolder_);
        }
        try {
            FileHelper.makeSureFileExisted(ffmpegBinFolder_);
        } catch (Exception e) {
            System.err.println("[ERR] Cannot find the ffmpeg bin folder: " + ffmpegBinFolder_);
        }
        FFmpeg.instance().setRuntimeFolder(ffmpegBinFolder_);
        Map<String, List<String>> cacheFolders = findCacheFolders("", inputFolder_, objectFileName_);
        fileNumber_.set(0);
        cacheFolders.forEach((key, value) -> {
            for (String path : value) {
                doConvert(path, value.size());
            }
        });
        while (fileNumber_.intValue() != 0) {
            Thread.sleep(1000);
        }
        System.out.println("[INFO] Finished all");
        fixedThreadPool_.shutdown();
    }

    private static Map<String, List<String>> findCacheFolders(String parent, String path, String specifiedFileName) {
        Map<String, List<String>> cacheFolders = new HashMap<>();
        List<String> folders = new LinkedList<>();
        File file = new File(path);
        File[] fileList = file.listFiles();
        for (File f : fileList) {
            if (f.isDirectory()) {
                Map<String, List<String>> tmp = findCacheFolders(path, f.getPath(), specifiedFileName);
                if (!tmp.isEmpty()) {
                    tmp.forEach((key, value) -> {
                        if (!cacheFolders.containsKey(key)) {
                            cacheFolders.put(key, value);
                        } else {
                            cacheFolders.get(key).addAll(value);
                        }
                    });
                }
            } else {
                if (f.getName().equals(specifiedFileName)) {
                    folders.add(path);
                }
            }
        }
        if (!folders.isEmpty()) {
            if (!cacheFolders.containsKey(parent)) {
                cacheFolders.put(parent, folders);
            } else {
                cacheFolders.get(parent).addAll(folders);
            }
        }
        return cacheFolders;
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
