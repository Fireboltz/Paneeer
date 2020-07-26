package org.amfoss.paneeer.editorCamera.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SizeF;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.max;

public class ReferenceCamera2 extends ReferenceCamera {

    private final String TAG = ReferenceCamera2.class.getSimpleName();

    // 아래의 카메라 ID에 기종별 int 값을 정의하면 됩니다. (현재는 Camera API의 Camera ID 사용하고 있습니다.)
    private static final int CAMERA_FACING_BACK = CameraCharacteristics.LENS_FACING_BACK;
    private static final int CAMERA_FACING_FRONT = CameraCharacteristics.LENS_FACING_FRONT;

    private int mCameraID;

    private int mCameraTextureId;
    private SurfaceTexture mCameraTexture;

    private final AtomicBoolean mSwitchingCamera = new AtomicBoolean(false);
    private final AtomicBoolean mIsStarted;

    private final List<String> mFrontCameraIds = new ArrayList<>();
    private final List<String> mRearCameraIds = new ArrayList<>();

    private final int[] mCameraIdIndices = new int[2];
    private boolean mSupportedFlash = false;
    private boolean mFaceDetectSupported = false;
    private int mFaceDetectMode = 0;

    private int[] mPreviewSize = new int[2];
    private int[] mVideoSize = new int[2];

    private int mCameraOrientation = 0;

    private HandlerThread mHandlerThread;
    private Handler mHandler = null;

    private final int mDeviceRotation;

    private Context mContext;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private CameraCaptureSession mCaptureSession;

    private CameraDevice mCameraDevice;

    private ImageReader mImageReader;

    private CaptureRequest.Builder mPreviewRequestBuilder;

    private CaptureRequest mPreviewRequest;

    private Size mCameraSensorResolution;

    private float HorizontalViewAngle = -1.0f;
    private float VerticalViewAngle = -1.0f;


    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }


    public ReferenceCamera2(Context context, CameraListener listener, int deviceRotation) {

        super.listener = listener;

        this.mDeviceRotation = deviceRotation;
        mIsStarted = new AtomicBoolean(false);

        this.mContext = context;

        initializeCameraIds();

        startHandlerThread();

        if (getCameraFacingFront() != -1) {
            setFacing(getCameraFacingFront());
        } else if (getCameraFacingBack() != -1) {
            setFacing(getCameraFacingBack());
        }
    }

    @Override
    public int getCameraFacingFront() {
        if (mFrontCameraIds.size() == 0) {
            return -1;
        }
        return CAMERA_FACING_FRONT;
    }

    @Override
    public int getCameraFacingBack() {
        if (mRearCameraIds.size() == 0) {
            return -1;
        }
        return CAMERA_FACING_BACK;
    }

    @Override
    public void setCameraTexture(int textureId, SurfaceTexture surfaceTexture) {
        if (mCameraDevice != null || mImageReader == null) return;

        if (mCameraTexture == null && surfaceTexture != null) {
            mCameraTextureId = textureId;
            mCameraTexture = surfaceTexture;
            String cameraId = getCameraId(mCameraID, mCameraIdIndices[mCameraID]);
            openCamera(cameraId, mCameraID);
        }
    }

    @Override
    public void setFacing(int CameraFacing) {
        mCameraID = CameraFacing;
    }

    @Override
    public void startCamera() {
        startCamera(mCameraID, mCameraIdIndices[mCameraID]);
    }

    @Override
    public void stopCamera() {
        Log.d(TAG , "stopCamera");

        synchronized (mIsStarted) {
            if (mIsStarted.compareAndSet(true, false)) {
                try {
                    closeCamera();
                } catch (NullPointerException e) {
                    Log.e(TAG, "Error Stopping camera - NullPointerException: ", e);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Error Stopping camera - RuntimeException: ", e);
                }
            }
        }
    }

    @Override
    public void destroy() {
        stopHandlerThread();
    }

    @Override
    public boolean isCameraFacingFront(){
        return mCameraID == CAMERA_FACING_FRONT;
    }

    @Override
    public boolean changeCameraFacing() {
        if (mFrontCameraIds.size() == 0 || mRearCameraIds.size() == 0) {
            return false;
        }

        if (mSwitchingCamera.compareAndSet(false, true)) {
            if (mCameraID == CAMERA_FACING_BACK) {
                mCameraID = CAMERA_FACING_FRONT;
            } else if (mCameraID == CAMERA_FACING_FRONT) {
                mCameraID = CAMERA_FACING_BACK;
            }
            changeCamera(mCameraID, mCameraIdIndices[mCameraID]);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int[] getPreviewSize(){
        return mPreviewSize;
    }

    public boolean isRunning(){
        return mIsStarted.get();
    }

    public int[] getVideoSize(){
        return mVideoSize;
    }

    public int getOrientation(){
        return mCameraOrientation;
    }

    private void startHandlerThread() {
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    private void stopHandlerThread() {
        if (mHandlerThread == null) {
            return;
        }

        mHandlerThread.quitSafely();
        try {
            mHandlerThread.join();
            mHandlerThread = null;
            mHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getCameraId(int facing, int cameraIdIndex) {
        String cameraId = null;
        if (facing == CAMERA_FACING_FRONT
                && mFrontCameraIds.size() > cameraIdIndex) {
            cameraId = mFrontCameraIds.get(cameraIdIndex);
        } else if (facing == CAMERA_FACING_BACK
                && mRearCameraIds.size() > cameraIdIndex) {
            cameraId = mRearCameraIds.get(cameraIdIndex);
        }
        return cameraId;
    }

    private void initializeCameraIds() {
        try {
            CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);
                int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == CAMERA_FACING_FRONT) {
                    mFrontCameraIds.add(cameraId);
                }else if (facing == CAMERA_FACING_BACK) {
                    mRearCameraIds.add(cameraId);
                }
            }
            mCameraIdIndices[0] = 0;
            mCameraIdIndices[1] = 0;
        }
        catch (CameraAccessException e) {
            Log.e(TAG, "Getting camera IDs failed.", e);
            mFrontCameraIds.clear();
            mRearCameraIds.clear();
            mCameraIdIndices[0] = 0;
            mCameraIdIndices[1] = 0;
        }
    }

    private void initCameraParameter(String cameraId, int facing) {
        Point realSize = new Point();
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        display.getRealSize(realSize);

        int width = realSize.x;
        int height = realSize.y;

        Size previewSize;
        Size videoSize;

        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            printSizes(streamConfigurationMap.getOutputSizes(SurfaceTexture.class), "preview");
            printSizes(streamConfigurationMap.getOutputSizes(MediaRecorder.class), "video");

            previewSize = getFittedPreviewSize(streamConfigurationMap.getOutputSizes(SurfaceTexture.class), 0.75f);
            videoSize = getOptimalSize(streamConfigurationMap.getOutputSizes(MediaRecorder.class), previewSize.getHeight(), previewSize.getWidth(), "video");
//            if(mCameraRatio == CAMERA_RATIO_FULL) {
//                previewSize = getOptimalSize(streamConfigurationMap.getOutputSizes(SurfaceTexture.class), width, height, "preview");
//                videoSize = getOptimalSize(streamConfigurationMap.getOutputSizes(MediaRecorder.class), previewSize.getHeight(), previewSize.getWidth(), "video");
//            } else {
//                previewSize = getFittedPreviewSize(streamConfigurationMap.getOutputSizes(SurfaceTexture.class), 0.75f);
//                videoSize = getOptimalSize(streamConfigurationMap.getOutputSizes(MediaRecorder.class), previewSize.getHeight(), previewSize.getWidth(), "video");
//            }

            Log.d(TAG , "displayMetrics w = " + width + "  h = " + height);
            Log.d(TAG , "previewSize w = " + previewSize.getWidth() + "  h = " + previewSize.getHeight());
            Log.d(TAG , "videoSize w = " + videoSize.getWidth() + "  h = " + videoSize.getHeight());

            mPreviewSize[0] = previewSize.getWidth();
            mPreviewSize[1] = previewSize.getHeight();

            mVideoSize[0] = videoSize.getWidth();
            mVideoSize[1] = videoSize.getHeight();

            mCameraSensorResolution = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);

            mImageReader = ImageReader.newInstance(mPreviewSize[0], mPreviewSize[1],
                    ImageFormat.YUV_420_888, /*maxImages*/2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mHandler);

            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            mCameraOrientation = getJpegOrientation(facing, mDeviceRotation, sensorOrientation);
            //mCameraOrientation = getCameraDisplayOrientation(facing, mDeviceRotation, sensorOrientation);

            // Check if the flash is supported.
            Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            mSupportedFlash = available == null ? false : available;

            // Check if the face detection is supported.
            int[] fdmode = characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
            int maxFdCount = characteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);
            if (fdmode.length>0) {
                List<Integer> fdList = new ArrayList<>();
                for (int mode : fdmode) {
                    fdList.add(mode);
                }

                if (maxFdCount > 0) {
                    mFaceDetectSupported = true;
                    mFaceDetectMode = Collections.max(fdList);
                }
            }

            // calculateFOV
            float[] maxFocus = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            SizeF size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            float w = size.getWidth();
            float h = size.getHeight();

            // FOV (rectilinear) =  2 * arctan (frame size/(focal length * 2))
            HorizontalViewAngle = (float) (2* Math.atan(w/(maxFocus[0]*2)));
            VerticalViewAngle = (float) (2* Math.atan(h/(maxFocus[0]*2)));

            Log.i(TAG, String.format("HorizontalViewAngle:%.2f", HorizontalViewAngle));
            Log.i(TAG, String.format("VerticalViewAngle:%.2f", VerticalViewAngle));
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void openCamera(String cameraId, int facing) {

        if(mCameraDevice != null)
            return;

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (cameraId != null) {
                manager.openCamera(cameraId, mStateCallback, mHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
            mCameraTexture = null;
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void startCamera(final int facing, final int cameraIdIndex) {
        Log.d(TAG , "startCamera " + mIsStarted + " " + mCameraTexture);

        synchronized (mIsStarted) {
            if (mIsStarted.compareAndSet(false, true)) {
                if (mFrontCameraIds.size() == 0 && mRearCameraIds.size() == 0) {
                    mIsStarted.set(false);
                    return;
                }

                String cameraId = getCameraId(facing, cameraIdIndex);
                if (cameraId == null) {
                    mIsStarted.set(false);
                    return;
                }

                initCameraParameter(cameraId, facing);
            }
        }
    }

    private void changeCamera(final int facing, final int cameraIdIndex) {
        if (mIsStarted.compareAndSet(true, false)) {
            try {
                closeCamera();
            } catch (NullPointerException e) {
                Log.e(TAG, "Error Stopping camera - NullPointerException: ", e);
            } catch (RuntimeException e) {
                Log.e(TAG, "Error Stopping camera - RuntimeException: ", e);
            }
        }

        String cameraId = getCameraId(facing, cameraIdIndex);
        if (cameraId == null) {
            mIsStarted.set(false);
            return;
        }

        if (mIsStarted.compareAndSet(false, true)) {
            initCameraParameter(cameraId, facing);
        }
        mSwitchingCamera.set(false);
    }

    private void setCameraFlash(boolean flag) {
        try {
            if (mCameraID == CAMERA_FACING_BACK) {
                if (mSupportedFlash) {
                    if (flag) {
                        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
                    } else {
                        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private int getCameraDisplayOrientation(int facing, int deviceRotation, int sensorOrientation) {

        Log.d(TAG, "setCameraDisplayOrientation " + deviceRotation + " " + sensorOrientation);

        int degrees = 0;
        switch (deviceRotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (facing == CAMERA_FACING_FRONT) {
            result = (sensorOrientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (sensorOrientation - degrees + 360) % 360;
        }

        return result;
    }

    private int getJpegOrientation(int facing, int deviceOrientation, int sensorOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return 0;

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        if (facing == CAMERA_FACING_FRONT) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;

        return jpegOrientation;
    }

    private Size getOptimalSize(Size[] sizes, int w, int h, String where) {

        Log.d(TAG, "getOptimalSize " + w + " " + h + " " + where + " " +  sizes);

        if (sizes == null) {
            return null;
        }

        double targetRatio = (double) h / w;
        Size optimalSize = null;
        double ASPECT_TOLERANCE = 0.0;

        // 1. find exactly matched
        for (Size size : sizes) {
            double ratio = (double) size.getWidth() / size.getHeight();
            int maxSize = Math.max(size.getWidth() , size.getHeight());
            Log.d(TAG, "optimal size (exactly) " + size.getWidth() + " " + size.getHeight() + " " +
                    ratio + " " + targetRatio + " " + Math.abs(ratio - targetRatio));

            boolean isCorrectkSize;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                isCorrectkSize = (size.getHeight() <= w);
            } else {
                isCorrectkSize = (maxSize <= 1920 && size.getHeight() <= w);
            }

            if (Math.abs(ratio - targetRatio) == ASPECT_TOLERANCE && isCorrectkSize) {
                optimalSize = size;
                break;
            }
        }

        if(optimalSize == null) {
            // 2. find matched
            double minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                double ratio = (double) size.getWidth() / size.getHeight();
                int maxSize = Math.max(size.getWidth() , size.getHeight());
                Log.d(TAG, "optimal size (step 2) " + size.getWidth() + " " + size.getHeight() + " "
                        + targetRatio + " " + minDiff);

                boolean isCorrectkSize;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    isCorrectkSize = (size.getHeight() <= w);
                } else {
                    isCorrectkSize = (maxSize <= 1920 && size.getHeight() <= w);
                }

                if (Math.abs(targetRatio - ratio) < minDiff && isCorrectkSize) {
                    optimalSize = size;
                    minDiff = Math.abs(targetRatio - ratio);
                }
            }
        }

        if (optimalSize != null) {
            Log.d(TAG, "result size " + targetRatio + " " + ((double)optimalSize.getWidth() / (double)optimalSize.getHeight())
                    + " " + optimalSize.getWidth() + " " + optimalSize.getHeight());
        } else {
            Log.d(TAG, "could not find optimal size " + targetRatio);
        }

        return optimalSize;
    }

    private Size getFittedPreviewSize(Size[] supportedPreviewSize, float previewRatio) {
        Size retSize = null;
        for (Size size : supportedPreviewSize) {
            float ratio = size.getHeight() / (float) size.getWidth();
            float EPSILON = (float) 1e-4;
            if (Math.abs(ratio - previewRatio) < EPSILON && max(size.getWidth(), size.getHeight()) <= 1920 ) {
                if (retSize == null || retSize.getWidth() < size.getWidth()) {
                    retSize = size;
                }
            }
        }
        return retSize;
    }

    private void printSizes(Size[] sizes, String title){
        Log.d(TAG, "==== print sizes for " + title);
        if(sizes != null) {
            for (Size size : sizes) {
                double ratio = (double) size.getWidth() / size.getHeight();
                Log.d(TAG, size.getWidth() + " " + size.getHeight() + " " + ratio);

            }
        }
        Log.d(TAG, "==== print sizes end " + title);
    }


    private void createCameraPreviewSession() {
        try {
            Surface surface = null;

            mCameraTexture.setDefaultBufferSize(mPreviewSize[0], mPreviewSize[1]);
            surface = new Surface(mCameraTexture);

            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            try {
                                // When the session is ready, we start displaying the preview.
                                mCaptureSession = cameraCaptureSession;

                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                // Enable face detection if supported
                                if (mFaceDetectSupported) {
                                    mPreviewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, mFaceDetectMode);
                                }

                                //Range<Integer> fpsRange = Range.create(30, 30);
                                //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange);

                                // Flash is automatically enabled when necessary.
                                if (mSupportedFlash) {
                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                                }

                                // Set orientation
                                mPreviewRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mCameraOrientation);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mHandler);


                                Range<Integer> fpsRange = mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE);

                                listener.setConfig(mPreviewSize[0],
                                        mPreviewSize[1],
                                        VerticalViewAngle,
                                        HorizontalViewAngle,
                                        mCameraOrientation,
                                        isCameraFacingFront(),
                                        fpsRange != null ? fpsRange.getUpper() : 30);


                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed (
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(final ImageReader reader) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final Image image = reader.acquireLatestImage();
                    if (image != null) {
                        listener.feedRawData(image);
                        image.close();
                    }
                }
            });
        }
    };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Log.e(TAG, "CameraDevice.StateCallback onError() " + error);
        }
    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            Integer mode = result.get(CaptureResult.STATISTICS_FACE_DETECT_MODE);
            Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
            if(faces != null && mode != null) {
                if (faces.length > 0) {
                    //Log.e(TAG, "face detected = " + faces.length);
                    int[][] bbox = new int[faces.length][4];
                    Rect rect;
                    for (int i = 0; i < faces.length; ++i) {
                        rect = faces[i].getBounds();
                        bbox[i][0] = rect.left*mPreviewSize[0]/mCameraSensorResolution.getWidth();
                        bbox[i][1] = rect.top*mPreviewSize[1]/mCameraSensorResolution.getHeight();
                        bbox[i][2] = rect.right*mPreviewSize[0]/mCameraSensorResolution.getWidth();
                        bbox[i][3] = rect.bottom*mPreviewSize[1]/mCameraSensorResolution.getHeight();
                    }

                    listener.updateFaceRects(faces.length, bbox);
                }
            }
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session,
                                        CaptureRequest request,
                                        CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }
    };

}
