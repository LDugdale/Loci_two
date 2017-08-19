package com.lauriedugdale.loci.ui.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Custom view for creating a circular ImageView, Used for profile pictures
 *
 * @author Laurie Dugdale
 */
public class CircularImageView extends android.support.v7.widget.AppCompatImageView {

    public CircularImageView( Context context )
    {
        super( context );
    }
    public CircularImageView( Context context, AttributeSet attrs ){
        super( context, attrs );
    }
    public CircularImageView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        Drawable drawable = getDrawable();

        // if drawable is not set return
        if (drawable == null) {
            return;
        }
        // if the width or height is 0 no need to display image return
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        // get bitmap
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = b.copy( Bitmap.Config.ARGB_8888, true );
        // set bitmap after cropping
        Bitmap circularBmp = cropBitmap(bitmap, getWidth());
        canvas.drawBitmap(circularBmp, 0, 0, null);
    }

    /**
     * used to crop the required bitmap to a given radius
     *
     * @param bmp bitmap to crop
     * @param bitmapRadius the required radius
     * @return The cropped bitmap
     */
    private Bitmap cropBitmap(Bitmap bmp, int bitmapRadius) {
        Bitmap bitmap;

        if ( bmp.getWidth( ) != bitmapRadius || bmp.getHeight( ) != bitmapRadius ) {
            float smallest = Math.min(bmp.getWidth(), bmp.getHeight());
            float f = smallest / bitmapRadius;
            bitmap = Bitmap.createScaledBitmap(bmp, (int)(bmp.getWidth() / f), (int)(bmp.getHeight( ) / f), false);
        } else {
            bitmap = bmp;
        }

        Bitmap output = Bitmap.createBitmap(bitmapRadius, bitmapRadius, Bitmap.Config.ARGB_8888 );
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rectangle = new Rect(0, 0, bitmapRadius, bitmapRadius);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor( "#FFFFFF"));
        canvas.drawCircle(bitmapRadius / 2 + 0.7f,  bitmapRadius / 2 + 0.7f, bitmapRadius / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode( PorterDuff.Mode.SRC_IN ));
        canvas.drawBitmap(bitmap, rectangle, rectangle, paint );

        return output;
    }

}