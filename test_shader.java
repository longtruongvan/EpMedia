import androidx.media3.effect.GlEffect;
import androidx.media3.effect.GlShaderProgram;
import androidx.media3.effect.BaseGlShaderProgram;
import androidx.media3.common.VideoFrameProcessingException;
import androidx.media3.common.util.GlUtil;
import android.content.Context;

class SharpenEffect implements GlEffect {
    @Override
    public GlShaderProgram toGlShaderProgram(Context context, boolean useHdr) throws VideoFrameProcessingException {
        return new BaseGlShaderProgram(useHdr, 1) {
            @Override
            public androidx.media3.common.util.Size configure(int inputWidth, int inputHeight) {
                return new androidx.media3.common.util.Size(inputWidth, inputHeight);
            }
            @Override
            public void drawFrame(int inputTexId, long presentationTimeUs) throws VideoFrameProcessingException {
                
            }
        };
    }
}
