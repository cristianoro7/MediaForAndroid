package com.desperado.mediaforandroid.camera;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by kamlin on 18-11-17.
 */
public class VideoEncoder {

    private MediaCodec mEncoder;

    private LinkedBlockingQueue<byte[]> mQueue = new LinkedBlockingQueue<>();

    private int width;
    private int height;

    private int bitrate;
    private boolean isStop = true;

    private OutputStream fileWriter;

    public VideoEncoder(int width, int height, String filePath) {
        this.width = width;
        this.height = height;
        bitrate =  4 * 1000 * 1000;

        try {
            fileWriter = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void fillData(byte[] data) {
        if (data != null) {
            mQueue.offer(data);
        }
    }

    public void initEncoder() {
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);

        try {
            mEncoder = MediaCodec.createEncoderByType("video/avc");
            mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        mEncoder.start();
        isStop = false;
        new Thread(new EncoderRunnable()).start();
    }

    public void stop() {
        isStop = true;
    }

    private void _stop() {
        mEncoder.stop();
        mEncoder.release();
        mEncoder = null;

        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class EncoderRunnable implements Runnable {

        @Override
        public void run() {
            while (!isStop) {

                while (mQueue.peek() != null) {
                    byte[] data = mQueue.poll();
                    byte[] newData = new byte[width * height * 3 / 2];
                    NV21ToNV12(data, newData, width, height);

                    //start fill data to mediaCodec
                    ByteBuffer[] inputBuffers = mEncoder.getInputBuffers();
                    int inputIndex = mEncoder.dequeueInputBuffer(-1);
                    if (inputIndex >= 0) {
                        long pts = computePresentationTime(inputIndex);
                        ByteBuffer inputBuffer = inputBuffers[inputIndex];
                        inputBuffer.clear();
                        inputBuffer.put(newData);
                        mEncoder.queueInputBuffer(inputIndex, 0, newData.length, pts, 0);
                    }

                    //start query output data
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    int outputIndex = mEncoder.dequeueOutputBuffer(info, 1000);
                    ByteBuffer[] outputBuffers = mEncoder.getOutputBuffers();
                    while (outputIndex >= 0) {
                        ByteBuffer outputBuffer = outputBuffers[outputIndex];
                        byte[] outData = new byte[info.size];
                        outputBuffer.get(outData);

                        if (fileWriter != null) {
                            try {
                                fileWriter.write(outData);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        mEncoder.releaseOutputBuffer(outputIndex, false);
                        outputIndex = mEncoder.dequeueOutputBuffer(info, -1);
                    }
                }
            }
            _stop();
        }
    }

    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / 25;
    }
}
