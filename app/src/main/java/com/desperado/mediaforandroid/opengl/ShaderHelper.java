package com.desperado.mediaforandroid.opengl;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by kamlin on 18-11-4.
 */
public class ShaderHelper {

    private static final String TAG = "ShaderHelper";

    public static final int INVALID_ID = 0;

    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {
        final int shaderObjectId = GLES20.glCreateShader(type);
        if (shaderObjectId == INVALID_ID) {
            return INVALID_ID;
        }
        GLES20.glShaderSource(shaderObjectId, shaderCode);
        GLES20.glCompileShader(shaderObjectId);
        final int compileStatus[] = new int[1];
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == INVALID_ID) {
            GLES20.glDeleteShader(shaderObjectId);

            Log.d(TAG, "compileShader: compile fail");
            return INVALID_ID;
        }
        return shaderObjectId;
    }

    public static int linkProgram(int vertexShader, int fragmentShader) {
        int programObjectId = GLES20.glCreateProgram();
        if (programObjectId == INVALID_ID) {
            Log.d(TAG, "linkProgram: cannt create opengl program!");
            return INVALID_ID;
        }
        GLES20.glAttachShader(programObjectId, vertexShader);
        GLES20.glAttachShader(programObjectId, fragmentShader);
        GLES20.glLinkProgram(programObjectId);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == INVALID_ID) {
            GLES20.glDeleteProgram(programObjectId);
            Log.d(TAG, "linkProgram: link program error");
            return 0;
        }
        return programObjectId;
    }

    public static boolean validateProgram(int programId) {
        GLES20.glValidateProgram(programId);
        int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        return validateStatus[0] != INVALID_ID;
    }

    public void loadTexture() {
//        GLES20.glGenTextures();
    }

}
