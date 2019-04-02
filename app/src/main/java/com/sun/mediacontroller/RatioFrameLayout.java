package com.sun.mediacontroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @author Sun
 * @date 2018/12/29 15:39
 * @desc
 */
public class RatioFrameLayout extends FrameLayout {
    public RatioFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public RatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    private float ratio;

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RatioFrameLayout);
            if (ta != null) {
                ratio = ta.getFloat(R.styleable.RatioFrameLayout_ratio, -1);

                ta.recycle();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeW = MeasureSpec.getSize(widthMeasureSpec);
        int modeW = MeasureSpec.getMode(widthMeasureSpec);

        int sizeH = MeasureSpec.getSize(heightMeasureSpec);
        int modeH = MeasureSpec.getMode(heightMeasureSpec);

        // 宽度确定，高度不确定，比例合法，重新计算
        if (modeW == MeasureSpec.EXACTLY && modeH != MeasureSpec.EXACTLY && ratio > 0) {
            int ratioW = sizeW - getPaddingLeft() - getPaddingRight();
            int ratioH = (int) (ratioW / ratio + 0.5f) + getPaddingTop() + getPaddingBottom();

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(ratioH, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
