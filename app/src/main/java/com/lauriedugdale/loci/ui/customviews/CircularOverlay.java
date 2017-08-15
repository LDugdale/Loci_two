package com.lauriedugdale.loci.ui.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Used to display a circular map in the UploadPageTwoFragment
 *
 * @author Laurie Dugdale
 */
public class CircularOverlay extends LinearLayout {

    private Bitmap mWindowFrame;
    private float mRadius = 0f;
    private Point mPoint;

    public CircularOverlay(Context context) {
        super(context);
    }
    public CircularOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        createView();
        canvas.drawBitmap(mWindowFrame, 0, 0, null);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    /**
     * draws the circle around the specified point with the desired radius
     *
     * @param point the point to draw around
     * @param radius
     */
    public void draw(Point point, float radius) {
        mPoint = point;
        mRadius = radius;
        invalidate();
    }

    /**
     * Create the circular overlay
     */
    protected void createView() {
        mWindowFrame = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas osCanvas = new Canvas(mWindowFrame);

        RectF rect = new RectF(0, 0, getWidth(), getHeight());
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        osCanvas.drawRect(rect, paint);

        // if radius and point are greater than zero and is not null
        if (mRadius > 0 && mPoint != null) {
            paint.setColor(Color.TRANSPARENT);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            paint.setStyle(Paint.Style.FILL);
            osCanvas.drawCircle(mPoint.x, mPoint.y, mRadius, paint);
        }
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mWindowFrame = null;
    }
}