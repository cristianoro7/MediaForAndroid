package com.desperado.mediaforandroid.audio;

import com.desperado.mediaforandroid.audio.wav.WaveWriter;

import java.io.FileNotFoundException;

/**
 * Created by kamlin on 18-7-21.
 */
public class WaveEncoder {

    private WaveWriter mWaveWriter;

    public WaveEncoder() {
        mWaveWriter = new WaveWriter();
    }

    public boolean open(String filePath, int sampleRate, short numChannels, short bitsPerSample) throws FileNotFoundException {
        return mWaveWriter.open(filePath, sampleRate, numChannels, bitsPerSample);
    }

    public boolean writeData(byte[] data, int off, int len) {
        return mWaveWriter.writeData(data, off, len);
    }

    public boolean closeFile() {
        return mWaveWriter.closeFile();
    }
}
