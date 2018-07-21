package com.desperado.mediaforandroid.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by kamlin on 2018/6/28.
 * 未完成
 */
public class AACEncoder {

    public static final int DEFAULT_BIT_RATE = 128 * 1024; //128kb

    public static final int DEFAULT_SIMPLE_RATE = 44100; //44100Hz

    public static final int DEFAULT_CHANNEL_COUNTS = 1;

    public static final int DEFAULT_MAX_INPUT_SIZE = 16384; //16k

    private MediaCodec mediaCodec;

    private MediaFormat mediaFormat;

    private OnBufferAvailableListener onBufferAvailableListener;

    private boolean isAsyMode = false;

    private Thread encoderThread;

    public void setOnBufferAvailableListener(OnBufferAvailableListener onBufferAvailableListener) {
        this.onBufferAvailableListener = onBufferAvailableListener;
    }

    private MediaFormat createMediaFormat() {
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                DEFAULT_SIMPLE_RATE, DEFAULT_CHANNEL_COUNTS);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_BIT_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, DEFAULT_MAX_INPUT_SIZE);
        return mediaFormat;
    }

    private void configure() {
        mediaCodec = createMediaCodec();
        if (mediaCodec == null) {
            throw new IllegalStateException("该设备不支持AAC编码器");
        }
        if (isAsyMode) {
            mediaCodec.setCallback(new AsyEncodeCallback());
        } else {
            encoderThread = new Thread(new SynchronousEncodeRunnable());
        }
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    private MediaCodec createMediaCodec() {
        mediaFormat = createMediaFormat();
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        String name = mediaCodecList.findEncoderForFormat(mediaFormat);
        if (name != null) {
            try {
                return MediaCodec.createByCodecName(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void start() {
        configure();
        mediaCodec.start();
        encoderThread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class AsyEncodeCallback extends MediaCodec.Callback {

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            ByteBuffer inputBuffer = codec.getInputBuffer(index);
            if (inputBuffer != null && onBufferAvailableListener != null) {
                inputBuffer.clear();
                boolean isStop = onBufferAvailableListener.onFillInputBuffer(inputBuffer);
                if (isStop) {

                }
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {

        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

        }
    }

    private class SynchronousEncodeRunnable implements Runnable {

        @Override
        public void run() {
            
        }
    }
    public interface OnBufferAvailableListener {
        boolean onFillInputBuffer(ByteBuffer byteBuffer);
    }
}
