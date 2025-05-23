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

    private static AudioProcessor audioProcessor;
    private static final float PITCH_SHIFT_VALUE = 0.7f;

    static {
        System.loadLibrary("native_lib");
    }


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.android.phone") || lpparam.packageName.equals("com.qualcomm.qti.telephonyservice") || lpparam.packageName.equals("com.audio.providers.telephony")) {
            if (audioProcessor == null) {
                audioProcessor = new AudioProcessor();
            }

            XposedHelpers.findAndHookMethod(AudioRecord.class, "read", byte[].class, int.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (audioProcessor == null) {
                        return;
                    }

                    // AudioRecord recordInstance = (AudioRecord) param.thisObject; // Example of getting instance

                    byte[] buffer = (byte[]) param.args[0];
                    int bytesRead = (int) param.getResult();

                    if (bytesRead > 0 && buffer != null) {
                        byte[] originalAudioData = new byte[bytesRead];
                        System.arraycopy(buffer, 0, originalAudioData, 0, bytesRead);

                        byte[] manipulatedData = audioProcessor.processAudio(originalAudioData, bytesRead, PITCH_SHIFT_VALUE);

                        if (manipulatedData != null) {
                            int bytesToWrite = Math.min(manipulatedData.length, buffer.length);
                            System.arraycopy(manipulatedData, 0, buffer, 0, bytesToWrite);

                            if (bytesToWrite < bytesRead && bytesToWrite < buffer.length) {
                                for (int i = bytesToWrite; i < Math.min(bytesRead, buffer.length); i++) {
                                    buffer[i] = 0; // Silence
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
