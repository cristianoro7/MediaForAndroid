package com.desperado.mediaforandroid.todo;

import android.util.Log;

import com.desperado.mediaforandroid.audio.AACEncoder;
import com.desperado.mediaforandroid.audio.AudioCapture;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by kamlin on 18-6-9.
 * 未完成
 */
public class AudioCaptureEngine extends Engine implements AudioCapture.OnAudioCaptureListener {

    private static final String TAG = "AudioCaptureEngine";

    private AudioCapture audioCapture = new AudioCapture();
    private AACEncoder aacEncoder = new AACEncoder();
    private DataOutputStream outputStream;

    private LinkedBlockingQueue<byte[]> bufferQueue = new LinkedBlockingQueue<>();

    public AudioCaptureEngine(String filePath) {
        if (filePath != null) {
            try {
                outputStream = new DataOutputStream(new FileOutputStream(filePath));
                audioCapture.setOnAudioCaptureListener(this);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "AudioCaptureEngine: cannot find the file");
            }
        }
    }
    @Override
    public boolean start() {
        return audioCapture.start();
    }

    @Override
    public boolean stop() {
        boolean isOK = audioCapture.stop();
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isOK;
    }

    @Override
    public void onAudioFrameCaptured(byte[] bytes) {
        if (outputStream != null) {
            Log.d(TAG, "onAudioFrameCaptured: writing data: " + Arrays.toString(bytes));
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "onAudioFrameCaptured: fail to write data!");
            }
        }
    }
}
