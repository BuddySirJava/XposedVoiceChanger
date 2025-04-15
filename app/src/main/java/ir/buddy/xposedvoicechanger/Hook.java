package ir.buddy.xposedvoicechanger;

import android.annotation.SuppressLint;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.telecom.Call;
import android.telecom.InCallService;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class Hook implements IXposedHookLoadPackage {

    private AudioTrack audioTrack;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.android.phone")) {
            XposedHelpers.findAndHookMethod(InCallService.class, "onCallAdded", Call.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    startAudioManipulation();
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    @SuppressWarnings("deprecation")
    private void startAudioManipulation() {
        int sampleRate = 44100;
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate, channelConfig, audioFormat, bufferSize);
        audioRecord.startRecording();

        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate, channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
        audioTrack.play();

        byte[] buffer = new byte[bufferSize];
        while (true) {
            int read = audioRecord.read(buffer, 0, buffer.length);
            if (read > 0) {
                byte[] manipulatedData = manipulateAudioData(buffer, read, 0.7f);
                audioTrack.write(manipulatedData, 0, manipulatedData.length);
            }
        }
    }

    private byte[] manipulateAudioData(byte[] buffer, int read, float pitchShift) {
        int newSize = (int) (read / pitchShift);
        byte[] outputBuffer = new byte[newSize];

        for (int i = 0; i < newSize; i++) {
            int index = (int) (i * pitchShift);
            if (index < read) {
                outputBuffer[i] = buffer[index];
            } else {
                outputBuffer[i] = 0;
            }
        }
        return outputBuffer;
    }
}
