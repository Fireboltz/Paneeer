package org.amfoss.paneeer.editorCamera.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.Image;

public abstract class ReferenceCamera {

    protected CameraListener listener;

    public abstract int getCameraFacingFront();
    public abstract int getCameraFacingBack();

    public abstract  void setCameraTexture(int textureId, SurfaceTexture surfaceTexture);
    public abstract void setFacing(int CameraFacing);
    public abstract boolean isCameraFacingFront();
    public abstract int[] getPreviewSize();
    public abstract void startCamera();
    public abstract void stopCamera();
    public abstract void destroy();
    public abstract boolean changeCameraFacing();

    public interface CameraListener {
        void setConfig(int previewWidth,
                       int previewHeight,
                       float verticalFov,
                       float horizontalFov,
                       int orientation,
                       boolean isFrontFacing,
                       float fps);
        // region - for camera api 1
        void updateFaceRects(Camera.Face[] faces);
        void feedRawData(byte[] data);
        // endregion
        // region - for camera api 2
        void updateFaceRects(int numFaces, int[][] bbox);
        void feedRawData(Image data);
        // endregion
    }
}