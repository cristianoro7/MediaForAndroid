package com.desperado.mediaforandroid.todo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by kamlin on 18-6-9.
 * 未完成
 */
public class AudioPlayer {

    private static final String TAG = "AudioPlayer";

    private static final int SIMPLE_RATE = 44100; //采样率
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT; //量化位宽
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO; //双通道
    private static final int PLAY_TYPE = AudioManager.STREAM_MUSIC; //播放模式
    private static final int PLAY_MODE = AudioTrack.MODE_STREAM;

    private AudioTrack audioTrack;

    private volatile boolean isStart = false;

    public boolean start() {
        return start(PLAY_TYPE, SIMPLE_RATE, CHANNEL_CONFIG, FORMAT);
    }

    public boolean start(int streamType, int sampleRate, int channelConfig, int format) {
        if (isStart) {
            return false;
        }

        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, format);
        if (minBufferSize == AudioTrack.ERROR_BAD_VALUE) {
            return false;
        }

        audioTrack = new AudioTrack(streamType, sampleRate, channelConfig, format,
                minBufferSize, PLAY_MODE);
        if (audioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
            return false;
        }
        isStart = true;
        return true;
    }

    public boolean play(byte[] audioData, int offset, int size) {
        if (!isStart) {
            return false;
        }
        Log.d(TAG, "play: start");

        if (audioTrack.write(audioData, offset, size) != size) {
            Log.d(TAG, "play: size != write size");
        }
        audioTrack.play();
        Log.d(TAG, "play: after play");
        return true;
    }

    public void stop() {
        if (!isStart) {
            return;
        }
        if (audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
        }
        audioTrack.release();
        audioTrack = null;
        isStart = false;
    }
}
