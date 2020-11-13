import java.io.File;
import java.util.LinkedList;
import java.util.List;

public abstract class MediaBase {
    protected MediaContext context_ = null;

    public MediaBase(MediaContext context) {
        context_ = context;
    }

    abstract public void startConvert() throws Exception;

    protected String getInputPath() {
        return context_.BaseFolder + File.separator + context_.Tag;
    }

    protected String getOutputFileName() {
        String page = !context_.Page.isEmpty() ? "_" + context_.Page : "";
        String tmp = FileHelper.correctFileName(context_.Title + page);
        return context_.OutputFolder + File.separator + tmp + ".mp4";
    }

    protected String generateTempFilename(String inputFilename) {
        String filename = FileHelper.getFileName(inputFilename);
        return context_.TempFolder + File.separator + filename;
    }
}
