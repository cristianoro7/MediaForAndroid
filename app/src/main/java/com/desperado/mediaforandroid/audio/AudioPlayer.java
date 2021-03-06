package com.desperado.mediaforandroid.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by kamlin on 18-6-9.
 * 未完成
 */
public class AudioPlayer implements AudioCapture.OnAudioCaptureListener {

    private static final String TAG = "AudioPlayer";

    private static final int SIMPLE_RATE = 44100; //采样率
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT; //量化位宽
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO; //单通道
    private static final int PLAY_TYPE = AudioManager.STREAM_MUSIC; //播放模式
    private static final int PLAY_MODE = AudioTrack.MODE_STREAM;

    private AudioTrack audioTrack;

    private AudioCapture mAudioCapture;

    private volatile boolean isStart = false;

    public AudioPlayer() {
        mAudioCapture = new AudioCapture();
        mAudioCapture.setOnAudioCaptureListener(this);
    }

    public boolean start() {
        boolean audioStart = mAudioCapture.start();
        boolean audioPlayerStart = start(PLAY_TYPE, SIMPLE_RATE, CHANNEL_CONFIG, FORMAT);
        return audioPlayerStart && audioStart;
    }

    public boolean start(int streamType, int sampleRate, int channelConfig, int format) {
        if (isStart) {
            return false;
        }
        audioTrack = createAudioTrack(streamType, sampleRate, channelConfig, format);
        if (audioTrack == null) {
            return false;
        }
        isStart = true;
        audioTrack.play();
        Log.d(TAG, "play: start");
        return true;
    }

    public boolean play(byte[] audioData, int offset, int size) {
        if (!isStart) {
            return false;
        }
        if (audioTrack.write(audioData, offset, size) != size) {
            Log.d(TAG, "play: size != write size");
        }
        return true;
    }

    public void stop() {
        if (!isStart) {
            return;
        }
        if (audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
        }
        isStart = false;
        mAudioCapture.stop();
        mAudioCapture = null;
        audioTrack.release();
        audioTrack = null;
    }

    @Override
    public void onAudioFrameCaptured(byte[] bytes) {
        play(bytes, 0, bytes.length);
    }

    private AudioTrack createAudioTrack(int streamType, int sampleRate, int channelConfig, int format) {
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, format);
        if (minBufferSize == AudioTrack.ERROR_BAD_VALUE) {
            return null;
        }
        int audioTrackBufferSize = minBufferSize * 4; //4帧音频帧的大小
        AudioTrack audioTrack = new AudioTrack(streamType, sampleRate, channelConfig, format,
                audioTrackBufferSize, PLAY_MODE);
        if (audioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
            return null;
        }
        return audioTrack;
    }
}
