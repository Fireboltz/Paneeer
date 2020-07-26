package org.amfoss.paneeer.editorCamera.data;

import com.seerslab.argear.session.ARGContents;

import org.amfoss.paneeer.R;
import org.amfoss.paneeer.editorCamera.AppConfig;

import java.util.ArrayList;


public class BeautyItemData {

    private ArrayList<BeautyItemInfo> mItemInfo = new ArrayList<>();
    private float [] mBeautyValues= new float[ARGContents.BEAUTY_TYPE_NUM];

    public class BeautyItemInfo {
        public ARGContents.BeautyType mBeautyType;
        public int mResource1;  // default
        public int mResource2;  // checked
        BeautyItemInfo(ARGContents.BeautyType type, int res1, int res2) {
            mBeautyType = type;
            mResource1 = res1;
            mResource2 = res2;
        }
    }

    public BeautyItemData() {
        initItemInfo();
        initBeautyValue();
    }

    public void initItemInfo() {
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.VLINE, R.drawable.beauty_vline_btn_default, R.drawable.beauty_vline_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.FACE_SLIM, R.drawable.beauty_face_slim_btn_default, R.drawable.beauty_face_slim_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.JAW, R.drawable.beauty_jaw_btn_default, R.drawable.beauty_jaw_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.CHIN, R.drawable.beauty_chin_btn_default, R.drawable.beauty_chin_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.EYE, R.drawable.beauty_eye_btn_default, R.drawable.beauty_eye_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.EYE_GAP, R.drawable.beauty_eyegap_btn_default, R.drawable.beauty_eyegap_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.NOSE_LINE, R.drawable.beauty_nose_line_btn_default, R.drawable.beauty_nose_line_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.NOSE_SIDE, R.drawable.beauty_nose_side_btn_default, R.drawable.beauty_nose_side_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.NOSE_LENGTH, R.drawable.beauty_nose_length_btn_default, R.drawable.beauty_nose_length_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.MOUTH_SIZE, R.drawable.beauty_mouth_size_btn_default, R.drawable.beauty_mouth_size_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.EYE_BACK, R.drawable.beauty_eyeback_btn_default, R.drawable.beauty_eyeback_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.EYE_CORNER, R.drawable.beauty_eyecorner_btn_default, R.drawable.beauty_eyecorner_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.LIP_SIZE, R.drawable.beauty_lip_size_btn_default, R.drawable.beauty_lip_size_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.SKIN_FACE, R.drawable.beauty_skin_btn_default, R.drawable.beauty_skin_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.SKIN_DARK_CIRCLE, R.drawable.beauty_dark_circle_btn_default, R.drawable.beauty_dark_circle_btn_checked));
        mItemInfo.add(new BeautyItemInfo(ARGContents.BeautyType.SKIN_MOUTH_WRINKLE, R.drawable.beauty_mouth_wrinkle_btn_default, R.drawable.beauty_mouth_wrinkle_btn_checked));
    }

    public ArrayList<BeautyItemInfo> getItemInfoData() {
        return mItemInfo;
    }

    public void initBeautyValue() {
        System.arraycopy(AppConfig.BEAUTY_TYPE_INIT_VALUE, 0, mBeautyValues, 0, mBeautyValues.length);
    }

    public void setBeautyValue(ARGContents.BeautyType beautyType, float progress) {
        mBeautyValues[beautyType.code] = progress;
    }

    public float getBeautyValue(ARGContents.BeautyType beautyType) {
        return mBeautyValues[beautyType.code];
    }

    public float [] getBeautyValues() {
        return mBeautyValues;
    }
}
