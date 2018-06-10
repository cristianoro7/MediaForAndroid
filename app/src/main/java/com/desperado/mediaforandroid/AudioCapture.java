package com.desperado.mediaforandroid;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by kamlin on 18-6-9.
 */
public class AudioCapture {
    private static final String TAG = AudioCapture.class.getSimpleName();

    private AudioRecord audioRecord;

    private static final int DEFAULT_SAMPLE_RATE = 44100; //默认采样率
    private static final int DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_STEREO; //双通道
    private static final int SIMPLE_FORMAT = AudioFormat.ENCODING_PCM_16BIT; //16位 量化
    private static final int DEFAULT_SOURCE_MIC = MediaRecorder.AudioSource.MIC; //声音从麦克风采集而来

    private volatile boolean isExit = true;
    private volatile boolean isStart = false;

    private Thread captureThread;

    private OnAudioCaptureListener listener;

    public void setListener(OnAudioCaptureListener listener) {
        this.listener = listener;
    }

    public boolean start() {
        return start(DEFAULT_SOURCE_MIC, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL, SIMPLE_FORMAT);
    }

    public boolean start(int audioSource, int simpleRate, int channels, int audioFormat) {
        if (isStart) {
            Log.d(TAG, "start: it is already start capturing");
            return false;
        }

        int minBufferSize = AudioRecord.getMinBufferSize(simpleRate, channels, audioFormat);
        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.d(TAG, "start: invalid parameters");
            return false;
        }

        audioRecord = new AudioRecord(audioSource, simpleRate, channels, audioFormat, minBufferSize * 4);
        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.d(TAG, "start: audio init fail");
            return false;
        }

        audioRecord.startRecording();

        isExit = false;
        captureThread = new Thread(new AudioCaptureRunnable());
        captureThread.start();
        isStart = true;
        Log.d(TAG, "start: start successfully");
        return true;
    }

    public boolean stop() {
        if (!isStart) {
            return false;
        }

        isExit = true;

        try {
            captureThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (audioRecord.getState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
        }
        audioRecord.release();
        audioRecord = null;
        isStart = false;
        Log.d(TAG, "stop: stop successfully");
        return true;
    }

    private class AudioCaptureRunnable implements Runnable {

        @Override
        public void run() {
            while (!isExit) {
                byte[] buffer = new byte[1024 * 2]; //每帧2K
                int result = audioRecord.read(buffer, 0, buffer.length);
                if (result == AudioRecord.ERROR_BAD_VALUE) {
                    Log.d(TAG, "run: ERROR_BAD_VALUE");
                } else if (result == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.d(TAG, "run: ERROR_INVALID_OPERATION");
                } else {
                    if (listener != null) {
                        Log.d(TAG, "run: capture buffer length is " + result);
                        listener.onAudioFrameCaptured(buffer);
                    }
                }
            }
        }
    }

    public interface OnAudioCaptureListener {
        void onAudioFrameCaptured(byte[] bytes);
    }
}
