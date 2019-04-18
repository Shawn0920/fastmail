package com.kernal.smartvision.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**扫描框UI布局
 * Created by user on 2018/8/28.
 */

public class BarCodeView extends View {
    boolean mInitialised;
    Paint mPaintFade ;
    Paint mPaintBlack;
    Paint mPaintYellow;
    Paint mPaintYellowFade;
    Paint mPaintRed;
    Paint mPaintGreen;
    Paint mPaintBlue;
    Paint mPaintWhite;
    public Rect mScanRect ;

    public BarCodeView(Context context) {
        super(context);
        mPaintBlack = new Paint();
        mPaintBlack.setStyle(Paint.Style.FILL);
        mPaintBlack.setColor(Color.BLACK);
        mPaintBlack.setTextSize(25);

        mPaintFade = new Paint();
        mPaintFade.setStyle(Paint.Style.FILL);
        mPaintFade.setColor(Color.BLACK);
        mPaintFade.setTextSize(25);
        mPaintFade.setAlpha(100);


        mPaintYellow = new Paint();
        mPaintYellow.setStyle(Paint.Style.FILL);
        mPaintYellow.setColor(Color.YELLOW);
        mPaintYellow.setTextSize(25);

        mPaintYellowFade = new Paint();
        mPaintYellowFade.setStyle(Paint.Style.FILL);
        mPaintYellowFade.setColor(Color.YELLOW);
        mPaintYellowFade.setTextSize(25);
        mPaintYellowFade.setAlpha(150);


        mPaintRed = new Paint();
        mPaintRed.setStyle(Paint.Style.FILL);
        mPaintRed.setColor(Color.RED);
        mPaintRed.setTextSize(25);

        mPaintGreen = new Paint();
        mPaintGreen.setStyle(Paint.Style.FILL);
        mPaintGreen.setColor(Color.GREEN);
        mPaintGreen.setTextSize(25);

        mPaintBlue = new Paint();
        mPaintBlue.setStyle(Paint.Style.FILL);
        mPaintBlue.setColor(Color.BLUE);
        mPaintBlue.setTextSize(25);

        mPaintWhite = new Paint();
        mPaintWhite.setStyle(Paint.Style.FILL);
        mPaintWhite.setColor(Color.WHITE);
        mPaintWhite.setTextSize(25);
    }

    @Override
    protected void onDraw(Canvas canvas) {

            int left, top, right, bottom ;
            left = (mScanRect.left * canvas.getWidth()) / 100;
            right = (mScanRect.right * canvas.getWidth()) / 100;
            top = (mScanRect.top * canvas.getHeight()) / 100;
            bottom = (mScanRect.bottom * canvas.getHeight()) / 100;

            canvas.drawRect(0, 0, canvas.getWidth(), top, mPaintFade);
            canvas.drawRect(0, bottom, canvas.getWidth(), canvas.getHeight(), mPaintFade);
            canvas.drawRect(0, top, left, bottom, mPaintFade);
            canvas.drawRect(right, top, canvas.getWidth(), bottom, mPaintFade);
            //canvas.drawLine(left, (top + bottom) / 2, right, (top + bottom) / 2, mPaintRed);
            canvas.drawLine(left, top, right, top, mPaintWhite);
            canvas.drawLine(left, bottom, right, bottom, mPaintWhite);
            canvas.drawLine(left, top, left, bottom, mPaintWhite);
            canvas.drawLine(right, top, right, bottom, mPaintWhite);
        // end if statement

        super.onDraw(canvas);

    } // end onDraw method
}
