package com.desperado.mediaforandroid.camera;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.desperado.mediaforandroid.R;

/**
 * Created by kamlin on 18-8-25.
 */
public class SurfaceViewPreview extends CameraPreview implements SurfaceHolder.Callback {

    final SurfaceView mSurfaceView;

    SurfaceViewPreview(Context context, ViewGroup parent) {
        View view = View.inflate(context, R.layout.preview_surface, parent);
        mSurfaceView = (SurfaceView) view.findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);
    }

    @Override
    Surface getSurface() {
        return getSurfaceHolder().getSurface();
    }

    @Override
    SurfaceHolder getSurfaceHolder() {
        return mSurfaceView.getHolder();
    }

    @Override
    Class<?> getOutputClass() {
        return SurfaceHolder.class;
    }

    @Override
    View getView() {
        return mSurfaceView;
    }

    @Override
    void setDisplayOrientation(int displayOrientation) {
    }

    @Override
    boolean isReady() {
        return getWidth() != 0 && getHeight() != 0;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        dispatchSurfaceCreate();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        setSize(width, height);
        if (!ViewCompat.isInLayout(mSurfaceView)) {
            dispatchSurfaceChanged();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        setSize(0, 0);
    }
}
