package com.desperado.mediaforandroid.audio.wav;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by kamlin on 2018/7/18.
 */
public class WaveWriter {

    private DataOutputStream dataOutputStream;

    private int dataLength = 0;

    private String filePath;

    public boolean open(String filePath, int sampleRate, short numChannels, short bitsPerSample) throws FileNotFoundException {
        if (filePath == null) {
            return false;
        }

        this.filePath = filePath;
        dataOutputStream = new DataOutputStream(new FileOutputStream(filePath));
        return writeWavHeader(sampleRate, numChannels, bitsPerSample);
    }

    private boolean writeWavHeader(int sampleRate, short numChannels, short bitsPerSample) {
        if (dataOutputStream == null) {
            return false;
        }
        WaveHeader waveHeader = new WaveHeader(sampleRate, numChannels, bitsPerSample);
        try {
            return waveHeader.writeHeader(dataOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean writeData(byte[] pcm, int off, int len) {
        if (pcm != null && dataOutputStream != null) {
            try {
                dataOutputStream.write(pcm, off, len);
                dataLength += len;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean closeFile() {
        if (dataOutputStream != null) {
            try {
                return writeDataSize();
            } catch (IOException e) {
                e.printStackTrace();
            }
            dataOutputStream = null;
            return true;
        }
        return false;
    }

    private boolean writeDataSize() throws IOException {
        if (filePath != null) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
            randomAccessFile.seek(WaveHeader.RIFF_CHUNK_SIZE_OFFSET);
            randomAccessFile.write(WaveHelper.int2Bytes(WaveHeader.CHUNKSIZE_EXCLUDE_DATA + dataLength), 0, 4);
            randomAccessFile.seek(WaveHeader.DATA_CHUNK_SIZE_OFFSET);
            randomAccessFile.write(WaveHelper.int2Bytes(dataLength), 0, 4);
            randomAccessFile.close();
            return true;
        }
        return false;
    }

}
