package com.desperado.mediaforandroid.opengl;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by kamlin on 18-11-4.
 */
public class ShaderLoader {

    public static String loadShaderFromResource(Context context, int resourceId) {
        StringBuilder builder = new StringBuilder();
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
