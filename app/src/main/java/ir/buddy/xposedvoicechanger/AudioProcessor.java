package ir.buddy.xposedvoicechanger;

public class AudioProcessor {
        static {
            System.loadLibrary("native_lib");
        }

        public native byte[] processAudio(byte[] inputBuffer, int length, float pitchShift);
    }
