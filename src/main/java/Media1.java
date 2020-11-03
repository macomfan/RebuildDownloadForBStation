import java.util.LinkedList;
import java.util.List;

public class Media1 extends MediaBase{

    public Media1(MediaContext context) {
        super(context);
    }

    @Override
    public void startConvert() throws Exception {
        List<String> files = FileHelper.findFiles(getInputPath(), ".blv");
        List<String> tsFiles = new LinkedList<>();
        FFmpeg ffmpeg = FFmpeg.instance();
        for (String file : files) {
            tsFiles.add(ffmpeg.convertToTs(file, generateTempFilename(file)));
        }
        ffmpeg.mergeTsToMP4(tsFiles, this.getOutputFileName());
        FileHelper.deleteFiles(tsFiles);
    }
}
