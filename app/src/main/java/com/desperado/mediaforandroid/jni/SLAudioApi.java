package com.desperado.mediaforandroid.jni;

/**
 * Created by kamlin on 18-8-18.
 */
public class SLAudioApi {

    public static native void initSLEngine(int sampleRate, int framesPerBuffer);

    public static native boolean createAudioRecorder();
}
