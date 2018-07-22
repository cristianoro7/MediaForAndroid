package com.desperado.mediaforandroid.audio;

import com.desperado.mediaforandroid.audio.wav.WaveWriter;

import java.io.FileNotFoundException;

/**
 * Created by kamlin on 18-7-21.
 */
public class WaveEncoder implements AudioCapture.OnAudioCaptureListener {

    private WaveWriter mWaveWriter;

    private AudioCapture mAudioCapture;

    public WaveEncoder() {
        mWaveWriter = new WaveWriter();
        mAudioCapture = new AudioCapture();
        mAudioCapture.setOnAudioCaptureListener(this);
    }

    public boolean prepare(String filePath, int sampleRate, short numChannels, short bitsPerSample) throws FileNotFoundException {
        return mWaveWriter.open(filePath, sampleRate, numChannels, bitsPerSample);
    }

    public boolean prepare(String filePath) throws FileNotFoundException {
        return prepare(filePath, AudioCapture.DEFAULT_SAMPLE_RATE, (short)1, (short)16);
    }

    public boolean start() {
        return mAudioCapture.start();
    }

    private boolean writeData(byte[] data, int off, int len) {
        return mWaveWriter.writeData(data, off, len);
    }

    public boolean stop() {
        boolean audioStop = mAudioCapture.stop();
        boolean waveWriterStop = mWaveWriter.closeFile();
        return waveWriterStop && audioStop;
    }

    @Override
    public void onAudioFrameCaptured(byte[] bytes) {
        writeData(bytes, 0, bytes.length);
    }
}
