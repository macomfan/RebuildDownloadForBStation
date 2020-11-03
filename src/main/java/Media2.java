import java.io.File;

public class Media2 extends MediaBase {

    public Media2(MediaContext context) {
        super(context);
    }

    private String getAudioFileName() {
        return this.getInputPath() + File.separator + "audio.m4s";
    }

    private String getVideoFileName() {
        return this.getInputPath() + File.separator + "video.m4s";
    }

    @Override
    public void startConvert() throws Exception {
        FFmpeg fFmpeg = FFmpeg.instance();
        fFmpeg.mergeM4S(this.getVideoFileName(), this.getAudioFileName(), this.getOutputFileName());
    }
}
