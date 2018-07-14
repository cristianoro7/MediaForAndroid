package com.desperado.mediaforandroid.todo;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by kamlin on 18-6-9.
 * 未完成
 */
public class AudioPlayEngine extends Engine {

    private static final String TAG = "AudioPlayEngine";

    private AudioPlayer player = new AudioPlayer();

    private DataInputStream dataInputStream;

    private Thread playThread;

    private volatile boolean isStart = false;

    public AudioPlayEngine(String filePath) {
        if (filePath != null) {
            try {
                dataInputStream = new DataInputStream(new FileInputStream(filePath));
                playThread = new Thread(new PlayRunnable());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean start() {
        isStart = true;
        boolean isOk = player.start();
        if (isOk) {
            playThread.start();
        }
        return isOk;
    }

    @Override
    public boolean stop() {
        isStart = false;
        player.stop();
        return true;
    }

    private class PlayRunnable implements Runnable {

        @Override
        public void run() {
            byte[] buffers;
            int ret = 0;
            do {
                buffers = new byte[1024 * 2];
                try {
                    ret = dataInputStream.read(buffers);
                    if (ret != -1) {
                        player.play(buffers, 0, buffers.length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (ret != -1 && isStart);
            if (isStart) {
                player.stop();
            }
        }
    }
}
