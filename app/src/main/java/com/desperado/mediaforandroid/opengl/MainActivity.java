package com.desperado.mediaforandroid.opengl;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGLSurfaceView;
    private boolean isSupportGL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isSupportGL()) {
            mGLSurfaceView = new GLSurfaceView(this);
            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setRenderer(new GLRender());
            mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            isSupportGL = true;
        } else {
            isSupportGL = false;
            Toast.makeText(this, "not support gl", Toast.LENGTH_LONG).show();
        }
        setContentView(mGLSurfaceView);
    }

    private boolean isSupportGL() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = activityManager.getDeviceConfigurationInfo();
        return info.reqGlEsVersion >= 0x20000
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                && (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK build for x86")));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSupportGL) {
            mGLSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSupportGL) {
            mGLSurfaceView.onResume();
        }
    }

    class GLRender implements GLSurfaceView.Renderer {

        public static final String U_COLOR = "u_Color";
        public static final String A_POSITION = "a_Position";

        private int uColorLocation;
        private int aPositionLocation;

        private int programId;

        private float[] tableVerticesWithTriangles;
        public final FloatBuffer vertexData;

        public static final int BYTE_PER_FLOAT = 4;

        public GLRender() {
            tableVerticesWithTriangles = new float[]{
// Triangle 1
                    -0.5f, -0.5f,
                    0.5f, 0.5f,
                    -0.5f, 0.5f,
// Triangle 2
                    -0.5f, -0.5f,
                    0.5f, -0.5f,
                    0.5f, 0.5f,
// Line 1
                    -0.5f, 0f,
                    0.5f, 0f,
// Mallets
                    0f, -0.25f,
                    0f, 0.25f
            };
            vertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length * BYTE_PER_FLOAT)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            vertexData.put(tableVerticesWithTriangles);
            vertexData.position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(1.0f, 0, 0, 1.0f);
            String vertexShaderSource = ShaderLoader.loadShaderFromResource(MainActivity.this, R.raw.simple_vertex_shader);
            String fragmentShaderSource = ShaderLoader.loadShaderFromResource(MainActivity.this, R.raw.simple_fragment_shader);
            Log.d("CR7", "onSurfaceCreated: " + vertexShaderSource + "\n"
            + fragmentShaderSource);
            int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
            int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
            programId = ShaderHelper.linkProgram(vertexShader, fragmentShader);
            if (BuildConfig.DEBUG) {
                boolean isValid = ShaderHelper.validateProgram(programId);
                Log.d("CR7", "onSurfaceCreated: " + isValid);
            }
            GLES20.glUseProgram(programId);

            uColorLocation = GLES20.glGetUniformLocation(programId, U_COLOR);
            aPositionLocation = GLES20.glGetAttribLocation(programId, A_POSITION);

            GLES20.glEnableVertexAttribArray(aPositionLocation);
            GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 1, vertexData);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

            GLES20.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);

            GLES20.glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
            GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1);

            GLES20.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1);
        }
    }
}
