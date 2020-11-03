import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

public class FFmpeg {
    private static FFmpeg instance_ = new FFmpeg();
    private String runtimeFolder_ = "";
    private String bin_ = "ffmpeg";
    private static Logger log = Logger.getLogger("FFmpeg");

    private FFmpeg() {

    }

    public static FFmpeg instance() {
        return instance_;
    }

    public void setRuntimeFolder(String folder) {
        runtimeFolder_ = folder;
    }

    public void setOutputFolder(String folder) {

    }

    public String convertToTs(String inputFileName, String outputFileName) throws Exception {
        // ffmpeg -i input1.flv -c copy -bsf:v h264_mp4toannexb -f mpegts input1.ts
        String tsName = FileHelper.renameFileExt(outputFileName,".ts");
        Params params = Params.build()
                .add("-i")
                .addFilename(inputFileName)
                .add("-c copy")
                .add("-bsf:v h264_mp4toannexb -f mpegts")
                .addFilename(tsName);
        doShell(params);
        FileHelper.makeSureFileExisted(tsName);
        return tsName;
    }

    public void mergeTsToMP4(List<String> inputFileNames, String outputFileName) throws Exception {
        // ffmpeg -i "concat:input1.ts|input2.ts|input3.ts" -c copy -bsf:a aac_adtstoasc -movflags +faststart output.mp4
        Params params = Params.build().add("-i");
        String files = "";
        for (String filename : inputFileNames ) {
            if (!files.isEmpty()) {
                files += "|";
            }
            files += filename;
        }
        files = "\"concat:" + files + "\"";
        params.add(files).add("-c copy -bsf:a aac_adtstoasc -movflags +faststart").addFilename(outputFileName);
        doShell(params);
        FileHelper.makeSureFileExisted(outputFileName);
    }

    public void mergeM4S(String inputVideoFile, String inputSoundFile, String outputFileName) throws Exception {
        FileHelper.makeSureFileExisted(inputVideoFile);
        FileHelper.makeSureFileExisted(inputSoundFile);
        String finalOutputFileName = FileHelper.checkOrRenameOutputFileName(outputFileName);
        // ffmpeg -i video.mp4 -i audio.wav -c:v copy -c:a aac -strict experimental output.mp4
        Params params = Params.build()
                .add("-i")
                .addFilename(inputVideoFile)
                .add("-i")
                .addFilename(inputSoundFile)
                .add("-c:v copy -c:a aac -strict experimental")
                .addFilename(finalOutputFileName);
        doShell(params);
        checkOutputFileName(finalOutputFileName);
    }

    private void checkOutputFileName(String filename) throws Exception {
        if (!FileHelper.fileExist(filename)) {
            throw new Exception("Output file created filed: " + filename);
        }
    }

    private void doShell(Params params) {
        String shell = runtimeFolder_.isEmpty() ? bin_ : runtimeFolder_ + File.separator + bin_;
        BufferedReader br = null;
        try {
            String command = shell + " -y " + params.getResult();
            Process p = Runtime.getRuntime().exec(command);//调用控制台执行shell
            System.out.println("[INFO] Executing: " + command);
            br = new BufferedReader(new InputStreamReader(p.getErrorStream()));//获取执行后出现的错误；getInputStream是获取执行后的结果
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
                //System.out.println(sb);
            }
            // System.out.println(sb);//打印执行后的结果
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Params {
        private String result_ = "";

        public static Params build() {
            return new Params();
        }

        public Params addFilename(String entity) {
            result_ = result_ + " " + "\"" + entity + "\"";
            return this;
        }
        public Params add(String entity) {
            result_ = result_ + " " + entity;
            return this;
        }

        public String getResult() {
            return result_;
        }
    }
}
