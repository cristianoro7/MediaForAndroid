package com.desperado.mediaforandroid.jni;

/**
 * Created by kamlin on 18-8-18.
 */
public class SLAudioApi {

    static {
        System.loadLibrary("native-audio");
    }

    public static native void initSLEngine(int sampleRate, int framesPerBuffer);

    public static native boolean createAudioRecorder(String filePath);

    public static native boolean start();

    public static native void stop();
}
