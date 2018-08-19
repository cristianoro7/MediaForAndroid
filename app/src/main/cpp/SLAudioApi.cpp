//
// Created by desperado on 18-8-18.
//
#include "CommonHelper.h"

static SLAudioEngine slAudioEngine;

extern "C"
JNIEXPORT void JNICALL
Java_com_desperado_mediaforandroid_jni_SLAudioApi_initSLEngine(JNIEnv *env, jclass type,
                                                               jint sampleRate,
                                                               jint framesPerBuffer) {
    SLresult sLresult;
    memset(&slAudioEngine, 0, sizeof(slAudioEngine));

    slAudioEngine.fastPathSampleRate = static_cast<SLmilliHertz>(sampleRate);
    slAudioEngine.fastPathFramesPerBuffer = static_cast<uint32_t>(framesPerBuffer);
    slAudioEngine.sampleChannels = 1;
    slAudioEngine.bitsPerSample = SL_PCMSAMPLEFORMAT_FIXED_16;

    sLresult = slCreateEngine(&slAudioEngine.slObjectItf, 0, NULL, 0, NULL, NULL);
    SLASSTER(sLresult);

    sLresult = (*slAudioEngine.slObjectItf)->Realize(slAudioEngine.slObjectItf, SL_BOOLEAN_FALSE);
    SLASSTER(sLresult);

    sLresult = (*slAudioEngine.slObjectItf)->GetInterface(slAudioEngine.slObjectItf, SL_IID_ENGINE,
                                                          &slAudioEngine.slEngineItf);
    SLASSTER(sLresult);

    uint32_t bufferSize = slAudioEngine.fastPathFramesPerBuffer * slAudioEngine.sampleChannels
                          * slAudioEngine.bitsPerSample;
    bufferSize = (bufferSize + 7) >> 3;
    slAudioEngine.recorderBuffer = new RecorderBuffer(bufferSize);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_desperado_mediaforandroid_jni_SLAudioApi_createAudioRecorder(JNIEnv *env, jclass type) {
    SampleFormat sampleFormat;
    memset(&sampleFormat, 0, sizeof(sampleFormat));
    sampleFormat.pcmFormat = slAudioEngine.bitsPerSample;
    sampleFormat.framesPerBuffer = slAudioEngine.fastPathFramesPerBuffer;
    sampleFormat.channels = slAudioEngine.sampleChannels;
    sampleFormat.sampleRate = slAudioEngine.fastPathSampleRate;

    slAudioEngine.slAudioRecorder = new SLAudioRecorder(sampleFormat, slAudioEngine.slEngineItf,
                                                        *slAudioEngine.recorderBuffer);
    if (!slAudioEngine.slAudioRecorder) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}