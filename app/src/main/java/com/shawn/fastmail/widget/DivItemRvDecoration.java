package com.shawn.fastmail.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.shawn.fastmail.R;
import com.shawn.fastmail.utils.CommonUtil;



/**
 * Created by shawn on 2016/4/10.
 */
public class DivItemRvDecoration extends RecyclerView.ItemDecoration {
    private int divHeight;
    private boolean hasHead;
    private Paint dividePaint;
    private int distance;

    public DivItemRvDecoration(Context context, boolean hasHead) {
        this.hasHead = hasHead;
        dividePaint = new Paint();
        dividePaint.setColor(context.getResources().getColor(R.color.ctc_backgrount));
        divHeight = (int) context.getResources().getDimension(R.dimen.h1);
        distance = CommonUtil.dip2px(12);
    }

    public DivItemRvDecoration(Context context, boolean hasHead, int lineHeight) {
        this.hasHead = hasHead;
        dividePaint = new Paint();
        dividePaint.setColor(context.getResources().getColor(R.color.transparent));
        divHeight = lineHeight;
        distance = CommonUtil.dip2px(12);
    }

    public DivItemRvDecoration(Context context, boolean hasHead, int lineHeight, int color) {
        this.hasHead = hasHead;
        dividePaint = new Paint();
        dividePaint.setColor(color);
        divHeight = lineHeight;
        distance = CommonUtil.dip2px(12);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        if (hasHead && position == 0) {
            return;
        }
        outRect.bottom = divHeight;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int childCount = parent.getChildCount();
        int left = parent.getPaddingLeft() + CommonUtil.dip2px(10);
        int right = parent.getWidth() - parent.getPaddingRight() - distance;
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            c.drawRect(left, view.getBottom(), right, view.getBottom() + divHeight, dividePaint);
        }

    }
}