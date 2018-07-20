package com.desperado.mediaforandroid.audio;

import android.annotation.TargetApi;
import android.media.AudioFormat;
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

    public static final int DEFAULT_CHANNEL_COUNTS = AudioFormat.CHANNEL_OUT_STEREO;

    private MediaCodec mediaCodec;

    private OnBufferAvailableListener onBufferAvailableListener;

    public void setOnBufferAvailableListener(OnBufferAvailableListener onBufferAvailableListener) {
        this.onBufferAvailableListener = onBufferAvailableListener;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private MediaFormat createMediaFormat() {
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                DEFAULT_SIMPLE_RATE, DEFAULT_CHANNEL_COUNTS);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_BIT_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);
        return mediaFormat;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initAACEncoder() {
        if (isSupport(MediaFormat.MIMETYPE_AUDIO_AAC)) {
            MediaFormat mediaFormat = createMediaFormat();
            try {
                MediaCodec mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
                mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("cannot not support aac encoder");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean isSupport(String mime) {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[] mediaCodecInfo = mediaCodecList.getCodecInfos();
        for (MediaCodecInfo info : mediaCodecInfo) {
            if (!info.isEncoder()) {
                continue;
            }
            String[] name = info.getSupportedTypes();
            for (String n : name) {
                if (n.equalsIgnoreCase(mime)) {
                    return true;
                }
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void start() {
        mediaCodec.setCallback(new AsyEncodeCallback());
        mediaCodec.start();
    }

    class AACEncodeWorker implements Runnable {


        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run() {
        }
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

    public interface OnBufferAvailableListener {
        boolean onFillInputBuffer(ByteBuffer byteBuffer);
    }
}
