package org.amfoss.paneeer.editorCamera.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatSeekBar;


public class CustomSeekBar extends AppCompatSeekBar {
    protected int minValue = 0;
    protected int maxValue = 0;
    protected OnSeekBarChangeListener listener;

    public CustomSeekBar(Context context){
        super(context);
        setUpInternalListener();
    }

    public CustomSeekBar(Context context, AttributeSet attrs){
        super(context, attrs);
        setUpInternalListener();
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        setUpInternalListener();
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMinValue(int min){
        this.minValue = min;
        super.setMax(maxValue - minValue);
    }

    public void setMaxValue(int max){
        this.maxValue = max;
        super.setMax(maxValue - minValue);
    }

    public void setProgress(int progress) {
        super.setProgress(progress - minValue);
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener){
        this.listener = listener;
    }

    private void setUpInternalListener() {
        super.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(listener != null){
                    listener.onProgressChanged(seekBar, minValue + i, b);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(listener != null)
                    listener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(listener != null)
                    listener.onStopTrackingTouch(seekBar);
            }
        });
    }
}
