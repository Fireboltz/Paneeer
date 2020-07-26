package org.amfoss.paneeer.editorCamera.ui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;

import androidx.annotation.NonNull;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private static final String TAG = GLView.class.getSimpleName();

    private int viewWidth;
    private int viewHeight;
    private GLViewListener listener;

    public GLView(@NonNull Context context, GLViewListener listener) {
        super(context);

        this.listener = listener;

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        getHolder().setFormat(PixelFormat.RGBA_8888);

        setRenderer(this);
        setZOrderOnTop(true);

        setRenderMode(RENDERMODE_CONTINUOUSLY);
        setPreserveEGLContextOnPause(false);
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }


    // region - GLSurfaceView
    @Override
    public void onResume() {
        super.onResume();
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onPause() {
        super.onPause();
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int glviewWidth = ((EditorCameraActivity)getContext()).getGLViewWidth();
        int glviewHeight = ((EditorCameraActivity)getContext()).getGLViewHeight();

        Log.d(TAG, "onMeasure " + glviewWidth + " " + glviewHeight + " " + width + " " + height);

        if(glviewWidth > 0 && glviewHeight > 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(glviewWidth, glviewHeight);
            ((EditorCameraActivity)getContext()).setMeasureSurfaceView(this);
        }else{
            setMeasuredDimension(width, height);
        }
    }
    // endregion


    // region - Renderer
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        listener.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        viewWidth = width;
        viewHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        listener.onDrawFrame(gl, viewWidth, viewHeight);
    }
    // endregion


    public interface GLViewListener {
        void onSurfaceCreated(GL10 gl, EGLConfig config);
        void onDrawFrame(GL10 gl, int width, int height);
    }
}
