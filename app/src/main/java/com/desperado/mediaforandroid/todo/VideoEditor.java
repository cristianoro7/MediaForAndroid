package com.desperado.mediaforandroid.todo;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by kamlin on 18-6-30.
 */
public class VideoEditor {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void spiltTrack(String srcPath, String outPath, boolean isVideo) {
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(srcPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaMuxer mediaMuxer = null;
        try {
            mediaMuxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int trackIndex = -1;
        String trackName = isVideo ? "video/" : "audio/";
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            if (mediaFormat.getString(MediaFormat.KEY_MIME).equals(trackName)) {
                trackIndex = mediaMuxer.addTrack(mediaFormat);
                break;
            }
        }
        mediaMuxer.start();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1000);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        long videoSimpleTime = getSimpleTime(mediaExtractor, byteBuffer);
        mediaExtractor.unselectTrack(trackIndex);
        mediaExtractor.selectTrack(trackIndex);
        while (true) {
            int size = mediaExtractor.readSampleData(byteBuffer, 0);
            if (size < 0) {
                break;
            }
            bufferInfo.size = size;
            bufferInfo.offset = 0;
            bufferInfo.flags = mediaExtractor.getSampleFlags();
            bufferInfo.presentationTimeUs += videoSimpleTime;
            mediaMuxer.writeSampleData(trackIndex, byteBuffer, bufferInfo);
        }

        mediaExtractor.release();
        mediaMuxer.release();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void muxVideo(String audioPath, String videoPath, String outPath) {
        MediaExtractor videoExtractor = new MediaExtractor();
        MediaExtractor audioExtractor = new MediaExtractor();
        try {
            InputStream inputStream = new FileInputStream(new File(videoPath));
            videoExtractor.setDataSource(((FileInputStream) inputStream).getFD());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
////            audioExtractor.setDataSource(audioPath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        MediaFormat videoFormat = null;
        int videoTrack = 0;
        for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
             videoFormat = videoExtractor.getTrackFormat(i);
            if (videoFormat.getString(MediaFormat.KEY_MIME).equals("video/")) {
                videoTrack = i;
                break;
            }
        }

//        MediaFormat audioFormat = null;
//        int audioTrack = 0;
//        for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
//            audioFormat = audioExtractor.getTrackFormat(i);
//            if (audioFormat.getString(MediaFormat.KEY_MIME).equals("audio/")) {
//                audioTrack = i;
//                break;
//            }
//        }

        videoExtractor.selectTrack(videoTrack);
//        audioExtractor.selectTrack(audioTrack);

        MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
        MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

        try {
            MediaMuxer mediaMuxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeVideoIndex = mediaMuxer.addTrack(videoFormat);
//            int writeAudioIndex = mediaMuxer.addTrack(audioFormat);
            mediaMuxer.start();

            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1000);
            long videoTs = getSimpleTime(videoExtractor, byteBuffer);
            videoExtractor.unselectTrack(videoTrack);
            videoExtractor.selectTrack(videoTrack);

            while (true) {
                int size = videoExtractor.readSampleData(byteBuffer, 0);
                if (size < 0) {
                    break;
                }
                videoBufferInfo.size = size;
                videoBufferInfo.offset = 0;
                videoBufferInfo.presentationTimeUs += videoTs;
                videoBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeVideoIndex, byteBuffer, videoBufferInfo);
                videoExtractor.advance();
            }

            long audioTs = getSimpleTime(audioExtractor, byteBuffer);
//            audioExtractor.unselectTrack(audioTrack);
//            audioExtractor.selectTrack(audioTrack);

            while (true) {
                int size = audioExtractor.readSampleData(byteBuffer, 0);
                if (size < 0) {
                    break;
                }
                audioBufferInfo.size = size;
                audioBufferInfo.offset = 0;
                audioBufferInfo.presentationTimeUs += audioTs;
                audioBufferInfo.flags = audioExtractor.getSampleFlags();
//                mediaMuxer.writeSampleData(writeAudioIndex, byteBuffer, audioBufferInfo);
            }

            mediaMuxer.release();
            audioExtractor.release();
            videoExtractor.release();
            Log.d("CR7", "muxVideo: ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private static long getSimpleTime(MediaExtractor extractor, ByteBuffer byteBuffer) {
        extractor.readSampleData(byteBuffer, 0);
        long firstPTS = extractor.getSampleTime();
        extractor.advance();

        extractor.readSampleData(byteBuffer, 0);
        long secondPTS = extractor.getSampleTime();
        extractor.advance();
        return Math.abs(secondPTS - firstPTS);
    }
}
