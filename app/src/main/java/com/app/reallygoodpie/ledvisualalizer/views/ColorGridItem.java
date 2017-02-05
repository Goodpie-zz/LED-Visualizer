package com.app.reallygoodpie.ledvisualalizer.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Linear layout child with square layout
 */
public class ColorGridItem extends LinearLayout {


    public ColorGridItem(Context context) {
        super(context);
    }

    public ColorGridItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorGridItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Ensure we only get a square layout
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
