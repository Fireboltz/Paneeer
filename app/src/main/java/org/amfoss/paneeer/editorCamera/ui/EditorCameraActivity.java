package org.amfoss.paneeer.editorCamera.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.seerslab.argear.exceptions.InvalidContentsException;
import com.seerslab.argear.exceptions.NetworkException;
import com.seerslab.argear.exceptions.SignedUrlGenerationException;
import com.seerslab.argear.session.ARGAuth;
import com.seerslab.argear.session.ARGContents;
import com.seerslab.argear.session.ARGFrame;
import com.seerslab.argear.session.ARGMedia;
import com.seerslab.argear.session.ARGSession;
import com.seerslab.argear.session.config.ARGCameraConfig;
import com.seerslab.argear.session.config.ARGConfig;
import com.seerslab.argear.session.config.ARGInferenceConfig;

import org.amfoss.paneeer.R;
import org.amfoss.paneeer.databinding.ActivityCameraBinding;
import org.amfoss.paneeer.editorCamera.AppConfig;
import org.amfoss.paneeer.editorCamera.api.ContentsResponse;
import org.amfoss.paneeer.editorCamera.camera.ReferenceCamera;
import org.amfoss.paneeer.editorCamera.camera.ReferenceCamera1;
import org.amfoss.paneeer.editorCamera.camera.ReferenceCamera2;
import org.amfoss.paneeer.editorCamera.common.PermissionHelper;
import org.amfoss.paneeer.editorCamera.data.BeautyItemData;
import org.amfoss.paneeer.editorCamera.model.ItemModel;
import org.amfoss.paneeer.editorCamera.network.DownloadAsyncResponse;
import org.amfoss.paneeer.editorCamera.network.DownloadAsyncTask;
import org.amfoss.paneeer.editorCamera.rendering.CameraTexture;
import org.amfoss.paneeer.editorCamera.rendering.ScreenRenderer;
import org.amfoss.paneeer.editorCamera.util.FileDeleteAsyncTask;
import org.amfoss.paneeer.editorCamera.util.PreferenceUtil;
import org.amfoss.paneeer.editorCamera.viewmodel.ContentsViewModel;
import org.amfoss.paneeer.gallery.activities.GalleryMainActivity;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class EditorCameraActivity extends AppCompatActivity {

    private static final String TAG = EditorCameraActivity.class.getSimpleName();

    private ReferenceCamera mCamera;
    private GLView mGlView;
    private ScreenRenderer mScreenRenderer;
    private CameraTexture mCameraTexture;
    private ARGFrame.Ratio mScreenRatio = ARGFrame.Ratio.RATIO_FULL;

    private String mItemDownloadPath;
    private String mMediaPath;
    private String mVideoFilePath;
    private boolean mIsShooting = false;

    private boolean mFilterVignette = false;
    private boolean mFilterBlur = false;
    private int mFilterLevel = 100;
    private ItemModel mCurrentStickeritem = null;
    private boolean mHasTrigger = false;

    private int mDeviceWidth = 0;
    private int mDeviceHeight = 0;
    private int mGLViewWidth = 0;
    private int mGLViewHeight = 0;

    private FragmentManager mFragmentManager;
    private FilterFragment mFilterFragment;
    private StickerFragment mStickerFragment;
    private BeautyFragment mBeautyFragment;
    private BulgeFragment mBulgeFragment;

    private ContentsViewModel mContentsViewModel;
    private BeautyItemData mBeautyItemData;

    private ActivityCameraBinding mDataBinding;
    private Toast mTriggerToast = null;

    private ARGSession mARGSession;
    private ARGMedia mARGMedia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        Point realSize = new Point();
        Display display= ((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getRealSize(realSize);
        mDeviceWidth = realSize.x;
        mDeviceHeight = realSize.y;
        mGLViewWidth = realSize.x;
        mGLViewHeight = realSize.y;

        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
        mBeautyItemData = new BeautyItemData();
        mContentsViewModel = new ViewModelProvider(this).get(ContentsViewModel.class);
        mContentsViewModel.getContents().observe(this, new Observer<ContentsResponse>() {
            @Override
            public void onChanged(ContentsResponse contentsResponse) {
                if (contentsResponse == null) return;
                setLastUpdateAt(EditorCameraActivity.this, contentsResponse.lastUpdatedAt);
            }
        });

        mFragmentManager = getSupportFragmentManager();
        mFilterFragment = new FilterFragment();
        mStickerFragment = new StickerFragment();
        mBeautyFragment = new BeautyFragment();
        mBulgeFragment = new BulgeFragment();

        mItemDownloadPath = getFilesDir().getAbsolutePath();
        mMediaPath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/ARGEAR";
        File dir = new File(mMediaPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        initRatioUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mARGSession == null) {

            if (!PermissionHelper.hasPermission(this)) {
                if (PermissionHelper.shouldShowRequestPermissionRationale(this)) {
                    mDataBinding.getRoot().setVisibility(View.GONE);
                    Toast.makeText(this, "Please check your permissions!", Toast.LENGTH_SHORT).show();
                    return;
                }
                PermissionHelper.requestPermission(this);
                return;
            }

            ARGConfig config
                    = new ARGConfig(AppConfig.API_URL1, AppConfig.API_KEY1, AppConfig.SECRET_KEY1, AppConfig.AUTH_KEY1);
            Set<ARGInferenceConfig.Feature> inferenceConfig
                    = EnumSet.of(ARGInferenceConfig.Feature.FACE_HIGH_TRACKING);

            mARGSession = new ARGSession(this, config, inferenceConfig);
            mARGMedia = new ARGMedia(mARGSession);

            mScreenRenderer = new ScreenRenderer();
            mCameraTexture = new CameraTexture();

            setBeauty(mBeautyItemData.getBeautyValues());

            initGLView();
            initCamera();
        }

        mCamera.startCamera();
        mARGSession.resume();

        setGLViewSize(mCamera.getPreviewSize());
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mARGSession != null) {
            mCamera.stopCamera();
            mARGSession.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mARGSession != null) {
            mCamera.destroy();
            mARGSession.destroy();
        }
    }

    @Override
    public void onBackPressed() {

        if ((mFilterFragment != null && mFilterFragment.isAdded())
                || (mStickerFragment != null && mStickerFragment.isAdded())
                || (mBeautyFragment != null && mBeautyFragment.isAdded())
                || (mBulgeFragment != null && mBulgeFragment.isAdded())
        ) {
            mDataBinding.functionsLayout.setVisibility(View.VISIBLE);
            mDataBinding.shutterLayout.setVisibility(View.VISIBLE);
        }

        if (mBeautyFragment != null && mBeautyFragment.isAdded()) {
            closeBeauty();
        }

        Intent homeIntent = new Intent(this, GalleryMainActivity.class);
        startActivity(homeIntent);
        finish();
    }

    private void initRatioUI() {
        if (mScreenRatio == ARGFrame.Ratio.RATIO_FULL) {
            // full
            mDataBinding.topRatioView.setVisibility(View.INVISIBLE);
            mDataBinding.bottomRatioView.setVisibility(View.INVISIBLE);
        } else if (mScreenRatio == ARGFrame.Ratio.RATIO_4_3) {
            // 3 : 4
            mDataBinding.bottomRatioView.setY((mDeviceWidth * 4) / 3);
            mDataBinding.bottomRatioView.getLayoutParams().height = mDeviceHeight - ((mDeviceWidth * 4) / 3);
            mDataBinding.topRatioView.setVisibility(View.INVISIBLE);
            mDataBinding.bottomRatioView.setVisibility(View.VISIBLE);
        } else {
            // 1 : 1
            int viewTopRation_H = (((mDeviceWidth * 4) / 3) - mDeviceWidth) / 2;
            mDataBinding.topRatioView.getLayoutParams().height = viewTopRation_H;
            mDataBinding.bottomRatioView.setY(viewTopRation_H + mDeviceWidth);
            mDataBinding.bottomRatioView.getLayoutParams().height = mDeviceHeight - viewTopRation_H + mDeviceWidth;
            mDataBinding.topRatioView.setVisibility(View.VISIBLE);
            mDataBinding.bottomRatioView.setVisibility(View.VISIBLE);
        }

        if (mDataBinding.topRatioView.getVisibility() == View.VISIBLE) {
            mDataBinding.topRatioView.requestLayout();
        }

        if (mDataBinding.bottomRatioView.getVisibility() == View.VISIBLE) {
            mDataBinding.bottomRatioView.requestLayout();
        }

        if (mBeautyFragment != null && mBeautyFragment.isAdded()) {
            mBeautyFragment.updateUIStyle(mScreenRatio);
        }
    }

    private void initGLView() {
        final FrameLayout cameraLayout = findViewById(R.id.camera_layout);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mGlView = new GLView(this, glViewListener);
        mGlView.setZOrderMediaOverlay(true);

        cameraLayout.addView(mGlView, params);
    }

    private void initCamera() {
        if (AppConfig.USE_CAMERA_API == 1) {
            mCamera = new ReferenceCamera1(this, cameraListener);
        } else {
            mCamera = new ReferenceCamera2(this, cameraListener, getWindowManager().getDefaultDisplay().getRotation());
        }
    }

    public void onClickButtons(View v) {
        switch (v.getId()) {
            case R.id.sticker_button:
                showStickers();
                break;
            case R.id.filter_button:
                showFilters();
                break;
            case R.id.beauty_button:
                showBeauty();
                break;
            case R.id.bulge_button:
                showBulge();
                break;
            case R.id.shutter_button: {
                if (mDataBinding.shutterPhotoButton.getViewSelected()) {
                    mIsShooting = true;
                    mDataBinding.shutterButton.setChecked(false);
                } else {
                    if (!mDataBinding.shutterButton.isChecked()) {
                        stopRecording();
                    } else {
                        startRecording();
                    }
                }
                break;
            }
            case R.id.shutter_photo_button: {
                mDataBinding.shutterPhotoButton.setViewSelected(true);
                mDataBinding.shutterVideoButton.setViewSelected(false);
                mDataBinding.shutterButton.setBackgroundResource(R.drawable.btn_shutter_photo_blue);
                break;
            }
            case R.id.shutter_video_button: {
                mDataBinding.shutterPhotoButton.setViewSelected(false);
                mDataBinding.shutterVideoButton.setViewSelected(true);
                mDataBinding.shutterButton.setBackgroundResource(R.drawable.btn_shutter_video_blue);
                break;
            }
            case R.id.camera_switch_button:
                mARGSession.pause();
                mCamera.changeCameraFacing();
                mARGSession.resume();
                break;
        }
    }

    private void setGLViewSize(int [] cameraPreviewSize) {
        int previewWidth = cameraPreviewSize[1];
        int previewHeight = cameraPreviewSize[0];

        if (mScreenRatio == ARGFrame.Ratio.RATIO_FULL) {
            mGLViewHeight = mDeviceHeight;
            mGLViewWidth = (int) ((float) mDeviceHeight * previewWidth / previewHeight );
        } else {
            mGLViewWidth = mDeviceWidth;
            mGLViewHeight = (int) ((float) mDeviceWidth * previewHeight / previewWidth);
        }

        if (mGlView != null
                && (mGLViewWidth != mGlView.getViewWidth() || mGLViewHeight != mGlView.getHeight())) {
            mDataBinding.cameraLayout.removeView(mGlView);
            mGlView.getHolder().setFixedSize(mGLViewWidth, mGLViewHeight);
            mDataBinding.cameraLayout.addView(mGlView);
        }
    }

    public void setMeasureSurfaceView(View view) {
        if (view.getParent() instanceof FrameLayout) {
            view.setLayoutParams(new FrameLayout.LayoutParams(mGLViewWidth, mGLViewHeight));
        }else if(view.getParent() instanceof RelativeLayout) {
            view.setLayoutParams(new RelativeLayout.LayoutParams(mGLViewWidth, mGLViewHeight));
        }

        /* to align center */
        if ((mScreenRatio == ARGFrame.Ratio.RATIO_FULL) && (mGLViewWidth > mDeviceWidth)) {
            view.setX((mDeviceWidth - mGLViewWidth) / 2);
        } else {
            view.setX(0);
        }
    }

    public int getGLViewWidth() {
        return mGLViewWidth;
    }

    public int getGLViewHeight() {
        return mGLViewHeight;
    }

    public BeautyItemData getBeautyItemData() {
        return mBeautyItemData;
    }

    public void updateTriggerStatus(final int triggerstatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TRIGGER_MOUTH_FLAG       = (1 << 0)
                // TRIGGER_HEAD_FLAG        = (1 << 1)
                // TRIGGER_DELAY_FLAG       = (1 << 2)
                // TRIGGER_BLINK_EYES_FLAG  = (1 << 3)
                if (mCurrentStickeritem != null && mHasTrigger) {
                    String strTrigger = null;
                    if ((triggerstatus & 1) != 0) {
                        strTrigger = "Open your mouth.";
                    } else if ((triggerstatus & 2) != 0) {
                        strTrigger = "Move your head side to side.";
                    } else if ((triggerstatus & 8) != 0) {
                        strTrigger = "Blink your eyes.";
                    } else {
                        if (mTriggerToast != null) {
                            mTriggerToast.cancel();
                            mTriggerToast = null;
                        }
                    }

                    if (strTrigger != null) {
                        mTriggerToast = Toast.makeText(EditorCameraActivity.this, strTrigger, Toast.LENGTH_SHORT);
                        mTriggerToast.setGravity(Gravity.CENTER, 0, 0);
                        mTriggerToast.show();
                        mHasTrigger = false;
                    }
                }
            }
        });
    }

    private void showSlot(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.slot_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void showStickers(){
        showSlot(mStickerFragment);
        clearBulge();
        mDataBinding.functionsLayout.setVisibility(View.GONE);
        mDataBinding.shutterLayout.setVisibility(View.GONE);
    }

    private void showFilters(){
        showSlot(mFilterFragment);
        mDataBinding.functionsLayout.setVisibility(View.GONE);
        mDataBinding.shutterLayout.setVisibility(View.GONE);
    }

    private void showBeauty() {
        mDataBinding.functionsLayout.setVisibility(View.GONE);
        mDataBinding.shutterLayout.setVisibility(View.GONE);

        clearStickers();
        clearBulge();

        Bundle args = new Bundle();
        args.putSerializable(BeautyFragment.BEAUTY_PARAM1, mScreenRatio);
        mBeautyFragment.setArguments(args);
        showSlot(mBeautyFragment);
    }

    private void closeBeauty() {

    }

    private void showBulge() {
        mDataBinding.functionsLayout.setVisibility(View.GONE);
        mDataBinding.shutterLayout.setVisibility(View.GONE);

        clearStickers();

        showSlot(mBulgeFragment);
    }

    public void clearBulge() {
        mARGSession.contents().clear(ARGContents.Type.Bulge);
    }

    public void setItem(ARGContents.Type type, String path, ItemModel itemModel) {

        mCurrentStickeritem = null;
        mHasTrigger = false;

        mARGSession.contents().setItem(type, path, itemModel.uuid, new ARGContents.Callback() {
            @Override
            public void onSuccess() {
                if (type == ARGContents.Type.ARGItem) {
                    mCurrentStickeritem = itemModel;
                    mHasTrigger = itemModel.hasTrigger;
                }
            }

            @Override
            public void onError(Throwable e) {
                mCurrentStickeritem = null;
                mHasTrigger = false;
                if (e instanceof InvalidContentsException) {
                    Log.e(TAG, "InvalidContentsException");
                }
            }
        });
    }

    public void setSticker(ItemModel item) {
        String filePath = mItemDownloadPath + "/" + item.uuid;
        if (getLastUpdateAt(EditorCameraActivity.this) > getStickerUpdateAt(EditorCameraActivity.this, item.uuid)) {
            new FileDeleteAsyncTask(new File(filePath), new FileDeleteAsyncTask.OnAsyncFileDeleteListener() {
                @Override
                public void processFinish(Object result) {
                    Log.d(TAG, "file delete success!");

                    setStickerUpdateAt(EditorCameraActivity.this, item.uuid, getLastUpdateAt(EditorCameraActivity.this));
                    requestSignedUrl(item, filePath, true);
                }
            }).execute();
        } else {
            if (new File(filePath).exists()) {
                setItem(ARGContents.Type.ARGItem, filePath, item);
            } else {
                requestSignedUrl(item, filePath, true);
            }
        }
    }

    public void clearStickers() {
        mCurrentStickeritem = null;
        mHasTrigger = false;

        mARGSession.contents().clear(ARGContents.Type.ARGItem);
    }

    public void setFilter(ItemModel item) {

        String filePath = mItemDownloadPath + "/" + item.uuid;
        if (getLastUpdateAt(EditorCameraActivity.this) > getFilterUpdateAt(EditorCameraActivity.this, item.uuid)) {
            new FileDeleteAsyncTask(new File(filePath), new FileDeleteAsyncTask.OnAsyncFileDeleteListener() {
                @Override
                public void processFinish(Object result) {
                    Log.d(TAG, "file delete success!");

                    setFilterUpdateAt(EditorCameraActivity.this, item.uuid, getLastUpdateAt(EditorCameraActivity.this));
                    requestSignedUrl(item, filePath, false);
                }
            }).execute();
        } else {
            if (new File(filePath).exists()) {
                setItem(ARGContents.Type.FilterItem, filePath, item);
            } else {
                requestSignedUrl(item, filePath, false);
            }
        }
    }

    public void clearFilter() {
        mARGSession.contents().clear(ARGContents.Type.FilterItem);
    }

    public void setFilterStrength(int strength) {
        if ((mFilterLevel + strength) < 100 && (mFilterLevel + strength) > 0) {
            mFilterLevel += strength;
        }
        mARGSession.contents().setFilterLevel(mFilterLevel);
    }

    public void setVignette() {
        mFilterVignette = !mFilterVignette;
        mARGSession.contents().setFilterOption(ARGContents.FilterOption.VIGNETTING, mFilterVignette);
    }

    public void setBlurVignette() {
        mFilterBlur = !mFilterBlur;
        mARGSession.contents().setFilterOption(ARGContents.FilterOption.BLUR, mFilterBlur);
    }

    public void setBeauty(float[] params) {
        mARGSession.contents().setBeauty(params);
    }

    public void setBulgeFunType(int type) {
        ARGContents.BulgeType bulgeType = ARGContents.BulgeType.NONE;
        switch (type) {
            case 1:
                bulgeType = ARGContents.BulgeType.FUN1;
                break;
            case 2:
                bulgeType = ARGContents.BulgeType.FUN2;
                break;
            case 3:
                bulgeType = ARGContents.BulgeType.FUN3;
                break;
            case 4:
                bulgeType = ARGContents.BulgeType.FUN4;
                break;
            case 5:
                bulgeType = ARGContents.BulgeType.FUN5;
                break;
            case 6:
                bulgeType = ARGContents.BulgeType.FUN6;
                break;
        }
        mARGSession.contents().setBulge(bulgeType);
    }

    private void setDrawLandmark(boolean landmark, boolean faceRect) {

        EnumSet<ARGInferenceConfig.Debug> set = EnumSet.of(ARGInferenceConfig.Debug.NONE);

        if(landmark){
            set.add(ARGInferenceConfig.Debug.FACE_LANDMARK);
        }

        if(faceRect) {
            set.add(ARGInferenceConfig.Debug.FACE_RECT_HW);
            set.add(ARGInferenceConfig.Debug.FACE_RECT_SW);
            set.add(ARGInferenceConfig.Debug.FACE_AXIES);
        }

        mARGSession.setDebugInference(set);
    }

    private void takePictureOnGlThread(int textureId) {
        mIsShooting = false;

        ARGMedia.Ratio ratio;
        if (mScreenRatio == ARGFrame.Ratio.RATIO_FULL) {
            ratio = ARGMedia.Ratio.RATIO_16_9;
        } else if (mScreenRatio == ARGFrame.Ratio.RATIO_4_3) {
            ratio = ARGMedia.Ratio.RATIO_4_3;
        } else {
            ratio = ARGMedia.Ratio.RATIO_1_1;
        }

        String path = mMediaPath + "/" + System.currentTimeMillis() + ".jpg";
        mARGMedia.takePicture(textureId, path, ratio);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+path)));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EditorCameraActivity.this, "The file has been saved to your Gallery.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(EditorCameraActivity.this, ImageViewerActivity.class);
                Bundle b = new Bundle();
                b.putString(ImageViewerActivity.INTENT_IMAGE_URI, path);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    private void startRecording() {
        if (mCamera == null) {
            return;
        }

        int bitrate = 10 * 1000 * 1000; // 10M

        ARGMedia.Ratio ratio;
        if (mScreenRatio == ARGFrame.Ratio.RATIO_FULL) {
            ratio = ARGMedia.Ratio.RATIO_16_9;
        } else if (mScreenRatio == ARGFrame.Ratio.RATIO_4_3) {
            ratio = ARGMedia.Ratio.RATIO_4_3;
        } else {
            ratio = ARGMedia.Ratio.RATIO_1_1;
        }

        int [] previewSize = mCamera.getPreviewSize();
        mVideoFilePath = mMediaPath + "/" + System.currentTimeMillis() + ".mp4";

        mARGMedia.initRecorder(mVideoFilePath, previewSize[0], previewSize[1], bitrate,
                false, false, false, ratio);
        mARGMedia.startRecording();

        Toast.makeText(this, "start recording.", Toast.LENGTH_SHORT).show();
    }

    private void stopRecording() {
        mDataBinding.shutterButton.setEnabled(false);
        mARGMedia.stopRecording();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+mVideoFilePath)));

                Toast.makeText(EditorCameraActivity.this, "The file has been saved to your Gallery.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(EditorCameraActivity.this, PlayerActivity.class);
                Bundle b = new Bundle();
                b.putString(PlayerActivity.INTENT_URI, mVideoFilePath);
                intent.putExtras(b);
                startActivity(intent);

                mDataBinding.shutterButton.setEnabled(true);
            }
        }, 500);
    }

    private void setLastUpdateAt(Context context, long updateAt) {
        PreferenceUtil.putLongValue(context, AppConfig.USER_PREF_NAME, "ContentLastUpdateAt", updateAt);
    }

    private long getLastUpdateAt(Context context) {
        return PreferenceUtil.getLongValue(context, AppConfig.USER_PREF_NAME, "ContentLastUpdateAt");
    }

    private void setFilterUpdateAt(Context context, String itemId, long updateAt) {
        PreferenceUtil.putLongValue(context, AppConfig.USER_PREF_NAME_FILTER, itemId, updateAt);
    }

    private long getFilterUpdateAt(Context context, String itemId) {
        return PreferenceUtil.getLongValue(context, AppConfig.USER_PREF_NAME_FILTER, itemId);
    }

    private void setStickerUpdateAt(Context context, String itemId, long updateAt) {
        PreferenceUtil.putLongValue(context, AppConfig.USER_PREF_NAME_STICKER, itemId, updateAt);
    }

    private long getStickerUpdateAt(Context context, String itemId) {
        return PreferenceUtil.getLongValue(context, AppConfig.USER_PREF_NAME_STICKER, itemId);
    }


    // region - network
    private void requestSignedUrl(ItemModel item, String path, final boolean isArItem) {
        mDataBinding.progressBar.setVisibility(View.VISIBLE);
        mARGSession.auth().requestSignedUrl(item.zipFileUrl, item.title, item.type, new ARGAuth.Callback() {
            @Override
            public void onSuccess(String url) {
                requestDownload(path, url, item, isArItem);
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof SignedUrlGenerationException) {
                    Log.e(TAG, "SignedUrlGenerationException !! ");
                } else if (e instanceof NetworkException) {
                    Log.e(TAG, "NetworkException !!");
                }

                mDataBinding.progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void requestDownload(String targetPath, String url, ItemModel item, boolean isSticker) {
        new DownloadAsyncTask(targetPath, url, new DownloadAsyncResponse() {
            @Override
            public void processFinish(boolean result) {
                mDataBinding.progressBar.setVisibility(View.INVISIBLE);
                if (result) {
                    if (isSticker) {
                        setItem(ARGContents.Type.ARGItem, targetPath, item);
                    } else {
                        setItem(ARGContents.Type.FilterItem, targetPath, item);
                    }
                    Log.d(TAG, "download success!");
                } else {
                    Log.d(TAG, "download failed!");
                }
            }
        }).execute();
    }
    // endregion


    // region - GLViewListener
    GLView.GLViewListener glViewListener = new GLView.GLViewListener() {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mScreenRenderer.create(gl, config);
            mCameraTexture.createCameraTexture();
        }

        @Override
        public void onDrawFrame(GL10 gl, int width, int height) {
            if (mCameraTexture.getSurfaceTexture() == null) {
                return;
            }

            if (mCamera != null) {
                mCamera.setCameraTexture(mCameraTexture.getTextureId(), mCameraTexture.getSurfaceTexture());
            }

            ARGFrame frame = mARGSession.drawFrame(gl, mScreenRatio, width, height);
            mScreenRenderer.draw(frame, width, height);

            if (mHasTrigger) updateTriggerStatus(frame.getItemTriggerFlag());

            if (mARGMedia != null) {
                if (mARGMedia.isRecording()) mARGMedia.updateFrame(frame.getTextureId());
                if (mIsShooting) takePictureOnGlThread(frame.getTextureId());
            }

            // getRawData
            // ByteBuffer bf = frame.getRawData(0, false, false);
            // if (bf == null) return;
        }
    };
    // endregion


    // region - CameraListener
    ReferenceCamera.CameraListener cameraListener = new ReferenceCamera.CameraListener() {
        @Override
        public void setConfig(int previewWidth, int previewHeight, float verticalFov, float horizontalFov, int orientation, boolean isFrontFacing, float fps) {
            mARGSession.setCameraConfig(new ARGCameraConfig(previewWidth,
                    previewHeight,
                    verticalFov,
                    horizontalFov,
                    orientation,
                    isFrontFacing,
                    fps));
        }

        // region - for camera api 1
        @Override
        public void updateFaceRects(Camera.Face[] faces) {
            mARGSession.updateFaceRects(faces);
        }

        @Override
        public void feedRawData(byte[] data) {
            mARGSession.feedRawData(data);
        }
        // endregion

        // region - for camera api 2
        @Override
        public void updateFaceRects(int numFaces, int[][] bbox) {
            mARGSession.updateFaceRects(numFaces, bbox);
        }

        @Override
        public void feedRawData(Image data) {
            mARGSession.feedRawData(data);
        }
        // endregion
    };
    // endregion
}
