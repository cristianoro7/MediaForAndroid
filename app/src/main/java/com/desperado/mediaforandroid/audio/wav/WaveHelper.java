package com.desperado.mediaforandroid.audio.wav;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by kamlin on 2018/7/18.
 */
public class WaveHelper {

    public static byte[] int2Bytes(int intValue) {
        return ByteBuffer.allocate(4).putInt(intValue).array();
    }

    public static byte[] short2Bytes(short shortValue) {
        return ByteBuffer.allocate(2).putShort(shortValue).array();
    }

    public static int bytes2Int(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static short bytes2Short(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getShort();
    }

    public static String bytes2String(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

    public static String bytes2String(byte[] bytes) {
        return bytes2String(bytes, Charset.forName("UTF-8"));
    }
}
