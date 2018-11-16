package com.desperado.mediaforandroid.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.desperado.mediaforandroid.R;

/**
 * Created by kamlin on 2018/11/16.
 */
public class TextureViewPreview extends CameraPreview implements TextureView.SurfaceTextureListener {

    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;

    private int mDisplayOrientation;

    TextureViewPreview(Context context, ViewGroup parent) {
        View view = View.inflate(context, R.layout.preview_texture, parent);
        mTextureView = (TextureView) view.findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    Surface getSurface() {
        if (mSurfaceTexture != null) {
            return new Surface(mSurfaceTexture);
        }
        return null;
    }

    @Override
    Class<?> getOutputClass() {
        return SurfaceTexture.class;
    }

    @Override
    View getView() {
        return mTextureView;
    }

    @Override
    Object getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;

    }

    @Override
    void setBufferSize(int width, int height) {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.setDefaultBufferSize(width, height);
        }
    }

    @Override
    boolean isReady() {
        return mSurfaceTexture != null;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceTexture = surface;
        setSize(width, height);
        dispatchSurfaceChanged();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mSurfaceTexture = surface;
        setSize(width, height);
        dispatchSurfaceChanged();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        setSize(0, 0);
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
}
