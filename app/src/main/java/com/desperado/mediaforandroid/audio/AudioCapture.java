package com.desperado.mediaforandroid.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by kamlin on 18-6-9.
 */
public class AudioCapture {
    private static final String TAG = AudioCapture.class.getSimpleName();

    private AudioRecord mAudioRecord;

    private static final int DEFAULT_SAMPLE_RATE = 44100; //默认采样率
    private static final int DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_MONO; //单通道
    private static final int SIMPLE_FORMAT = AudioFormat.ENCODING_PCM_16BIT; //16位 量化
    private static final int DEFAULT_SOURCE_MIC = MediaRecorder.AudioSource.MIC; //声音从麦克风采集而来

    private volatile boolean isExit = true;
    private volatile boolean isStart = false;

    private Thread captureThread;

    private OnAudioCaptureListener listener;

    public void setOnAudioCaptureListener(OnAudioCaptureListener listener) {
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
        mAudioRecord = createAudioRecord(audioSource, simpleRate, channels, audioFormat); //初始化AudioRecord
        if (mAudioRecord == null) {
            return false;
        }
        mAudioRecord.startRecording(); //开始

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

        if (mAudioRecord.getState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop(); // 不是必须调用的, 因为release内部也会调用
        }
        mAudioRecord.release(); //
        mAudioRecord = null;
        isStart = false;
        Log.d(TAG, "stop: stop successfully");
        return true;
    }

    private class AudioCaptureRunnable implements Runnable {

        @Override
        public void run() {
            while (!isExit) {
                byte[] buffer = new byte[1024 * 2]; //每次拿2k
                int result = mAudioRecord.read(buffer, 0, buffer.length);
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

    private AudioRecord createAudioRecord(int audioSource, int simpleRate, int channels, int audioFormat) {
        int minBufferSize = AudioRecord.getMinBufferSize(simpleRate, channels, audioFormat); //获取一帧音频帧的大小
        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.d(TAG, "获取音频帧大小失败!");
            return null;
        }
        int audioRecordBufferSize = minBufferSize * 4; //AudioRecord内部缓冲设置为4帧音频帧的大小句
        AudioRecord audioRecord = new AudioRecord(audioSource, simpleRate, channels, audioFormat, audioRecordBufferSize);
        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.d(TAG, "初始化AudioRecord失败!");
            return null;
        }
        return audioRecord;
    }
}
