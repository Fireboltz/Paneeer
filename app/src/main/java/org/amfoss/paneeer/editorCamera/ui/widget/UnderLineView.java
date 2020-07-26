package org.amfoss.paneeer.editorCamera.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.amfoss.paneeer.R;

public class UnderLineView extends FrameLayout {

    private TextView mTextView;
    private View mUnderLine;

    private boolean mViewSelected;

    public UnderLineView(Context context) {
        super(context);
        init();
    }

    public UnderLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        setAttrs(attrs, 0);
    }

    public UnderLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        setAttrs(attrs, defStyleAttr);
    }

    private void setAttrs(AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.UnderLineView, defStyleAttr, 0);

        String textString = ta.getString(R.styleable.UnderLineView_text);
        setText(textString);

        setViewSelected(ta.getBoolean(R.styleable.UnderLineView_select, false));

        ta.recycle();
    }

    private void init() {
        mTextView = new TextView(getContext());
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        mTextView.setGravity(Gravity.CENTER);
        addView(mTextView);

        mUnderLine = new View(getContext());
        addView(mUnderLine);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mUnderLine.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.0f,
                getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.BOTTOM;

        setUnderLineColor(Color.parseColor("#3063E6"));
    }

    public void setText(CharSequence text) {
        if (mTextView != null) {
            mTextView.setText(text);
        }
    }

    public void setTextColor(int color) {
        if (mTextView != null) {
            mTextView.setTextColor(color);
        }
    }

    public void setUnderLineColor(int color) {
        if (mUnderLine != null) {
            mUnderLine.setBackgroundColor(color);
        }
    }

    public boolean getViewSelected() {
        return mViewSelected;
    }

    public void setViewSelected(boolean isSelected) {
        mViewSelected = isSelected;
        if (mViewSelected) {
            setTextColor(Color.parseColor("#3063E6"));
            setBottomLineVisibility(View.VISIBLE);

            if (getParent() != null) {
                if (getTag().equals("0")) {
                    ((View)getParent()).setPadding(getWidth(), 0, 0, 0);
                } else if (getTag().equals("1")) {
                    ((View)getParent()).setPadding(0, 0, (int) (getWidth() + (getWidth() * 0.5f)), 0);
                }
            }
        } else {
            setTextColor(Color.parseColor("#bdbdbd"));
            setBottomLineVisibility(View.GONE);
        }
    }

    private void setBottomLineVisibility(int visibility) {
        if (mUnderLine != null) {
            mUnderLine.setVisibility(visibility);
        }
    }
}
