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
        String invalid = "[ 。|<>?*/.:]";
        String title = context_.Title.replaceAll(invalid, "");
        String page = context_.Page.replaceAll(invalid, "");
        return context_.OutputFolder + File.separator + title + "_" + page + ".mp4";
    }

    protected String generateTempFilename(String inputFilename) {
        String filename = FileHelper.getFileName(inputFilename);
        return context_.TempFolder + File.separator + filename;
    }
}
