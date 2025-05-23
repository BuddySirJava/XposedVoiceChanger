#include "native_bin.h"

#include <jni.h>
#include <android/log.h>
#include <cstring>

#define LOG_TAG "AudioManipulation"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ir_buddy_xposedvoicechanger_AudioProcessor_processAudio(JNIEnv *env, jobject thiz, jbyteArray inputBuffer, jint length, jfloat pitchShift) {
    if (inputBuffer == nullptr || length <= 0) {
        LOGI("Invalid input buffer or length");
        return nullptr;
    }

    jbyte *inputData = env->GetByteArrayElements(inputBuffer, nullptr);
    if (inputData == nullptr) {
        LOGI("Failed to get input buffer elements");
        return nullptr;
    }

    int newSize = static_cast<int>(length / pitchShift);
    jbyteArray outputBuffer = env->NewByteArray(newSize);
    jbyte *outputData = env->GetByteArrayElements(outputBuffer, nullptr);

    for (int i = 0; i < newSize; i++) {
        int index = static_cast<int>(i * pitchShift);
        if (index < length) {
            outputData[i] = inputData[index];
        } else {
            outputData[i] = 0;
        }
    }

    env->ReleaseByteArrayElements(inputBuffer, inputData, JNI_ABORT);
    env->ReleaseByteArrayElements(outputBuffer, outputData, 0);

    return outputBuffer;
}
