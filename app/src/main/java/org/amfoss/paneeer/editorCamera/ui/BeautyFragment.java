package org.amfoss.paneeer.editorCamera.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.seerslab.argear.session.ARGContents;
import com.seerslab.argear.session.ARGFrame;

import org.amfoss.paneeer.R;
import org.amfoss.paneeer.editorCamera.AppConfig;
import org.amfoss.paneeer.editorCamera.data.BeautyItemData;
import org.amfoss.paneeer.editorCamera.ui.adapter.BeautyListAdapter;
import org.amfoss.paneeer.editorCamera.ui.widget.CustomSeekBar;

import java.util.Locale;

public class BeautyFragment
        extends Fragment
        implements View.OnClickListener, BeautyListAdapter.Listener {

    private static final String TAG = BeautyFragment.class.getSimpleName();

    public static final String BEAUTY_PARAM1 = "bearuty_param1";

    private BeautyListAdapter mBeautyListAdapter;
    private TextView mBeautyLevelInfo;
    private CustomSeekBar mBeautySeekBar;

    private BeautyItemData mBeautyItemData;
    private ARGContents.BeautyType mCurrentBeautyType = ARGContents.BeautyType.VLINE;

    private ARGFrame.Ratio mScreenRatio;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScreenRatio = (ARGFrame.Ratio)getArguments().getSerializable(BEAUTY_PARAM1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_beauty, container, false);

        mBeautyLevelInfo = rootView.findViewById(R.id.beauty_level_info);
        mBeautySeekBar = rootView.findViewById(R.id.beauty_seekbar);

        rootView.findViewById(R.id.beauty_init_button).setOnClickListener(this);
        rootView.findViewById(R.id.beauty_close_button).setOnClickListener(this);

        RecyclerView recyclerViewBeauty = rootView.findViewById(R.id.beauty_items_layout);
        recyclerViewBeauty.setHasFixedSize(true);
        LinearLayoutManager beautyLayoutManager = new LinearLayoutManager(getContext());
        beautyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewBeauty.setLayoutManager(beautyLayoutManager);

        mBeautyListAdapter = new BeautyListAdapter(this);
        recyclerViewBeauty.setAdapter(mBeautyListAdapter);

        mBeautyLevelInfo = rootView.findViewById(R.id.beauty_level_info);
        mBeautySeekBar = rootView.findViewById(R.id.beauty_seekbar);
        mBeautySeekBar.setOnSeekBarChangeListener(BeautySeekBarListener);

        Button comparisonButton = rootView.findViewById(R.id.beauty_comparison_button);
        comparisonButton.setOnTouchListener(BeautyComparisonBtnTouchListener);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBeautyItemData = ((EditorCameraActivity)getActivity()).getBeautyItemData();
        mBeautyListAdapter.setData(mBeautyItemData.getItemInfoData());
        mBeautyListAdapter.selectItem(ARGContents.BeautyType.VLINE);

        updateUIStyle(mScreenRatio);

        onBeautyItemSelected(ARGContents.BeautyType.VLINE);

        reloadBeauty();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.beauty_init_button:
                ((EditorCameraActivity) getActivity()).setBeauty(AppConfig.BEAUTY_TYPE_INIT_VALUE);
                mBeautyItemData.initBeautyValue();
                mBeautySeekBar.setProgress((int) mBeautyItemData.getBeautyValue(mCurrentBeautyType));
                break;
            case R.id.beauty_close_button:
                getActivity().onBackPressed();
                break;
        }
    }

    @Override
    public void onBeautyItemSelected(ARGContents.BeautyType beautyType) {
        int[] values = ARGContents.BEAUTY_RANGE.get(beautyType);
        if (values == null) {
            return;
        }
        mCurrentBeautyType = beautyType;
        mBeautySeekBar.setMinValue(values[0]);
        mBeautySeekBar.setMaxValue(values[1]);
        mBeautySeekBar.setProgress((int) mBeautyItemData.getBeautyValue(beautyType));
    }

    private void updateBeautyInfoPosition(TextView view, int progress) {
        if (view != null) {
            int max = mBeautySeekBar.getMaxValue() - mBeautySeekBar.getMinValue();
            view.setText(String.format(Locale.getDefault(), "%d", progress));

            int paddingLeft = 0;
            int paddingRight = 0;
            int offset = -5;
            int viewWidth = view.getWidth();
            int x = (int) ((float) (mBeautySeekBar.getRight() - mBeautySeekBar.getLeft() - paddingLeft - paddingRight - viewWidth - 2 * offset)
                    * (progress - mBeautySeekBar.getMinValue()) / max)
                    + mBeautySeekBar.getLeft() + paddingLeft + offset;
            view.setX(x);
        }
    }

    @Override
    public ARGFrame.Ratio getViewRatio() {
        return mScreenRatio;
    }

    private void zeroBeautyParam() {
        ((EditorCameraActivity) getActivity()).setBeauty(new float[ARGContents.BEAUTY_TYPE_NUM]);
    }

    private void reloadBeauty() {
        ((EditorCameraActivity) getActivity()).setBeauty(mBeautyItemData.getBeautyValues());
    }

    public void updateUIStyle(ARGFrame.Ratio ratio) {
        mScreenRatio = ratio;
        if (ratio == ARGFrame.Ratio.RATIO_FULL) {
            mBeautySeekBar.setActivated(false);
            mBeautyLevelInfo.setActivated(false);
            mBeautyLevelInfo.setTextColor(Color.BLACK);
        } else {
            mBeautySeekBar.setActivated(true);
            mBeautyLevelInfo.setActivated(true);
            mBeautyLevelInfo.setTextColor(Color.WHITE);
        }

        mBeautyListAdapter.notifyDataSetChanged();
    }

    private SeekBar.OnSeekBarChangeListener BeautySeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                updateBeautyInfoPosition(mBeautyLevelInfo, progress);
                mBeautyItemData.setBeautyValue(mCurrentBeautyType, progress);
                ((EditorCameraActivity) getActivity()).setBeauty(mBeautyItemData.getBeautyValues());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mBeautyLevelInfo.setVisibility(View.VISIBLE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mBeautyLevelInfo.setVisibility(View.GONE);
        }
    };

    private View.OnTouchListener BeautyComparisonBtnTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if(MotionEvent.ACTION_DOWN == event.getAction()) {
                zeroBeautyParam();
            } else if(MotionEvent.ACTION_UP == event.getAction()) {
                reloadBeauty();
            }
            return true;
        }
    };
}
