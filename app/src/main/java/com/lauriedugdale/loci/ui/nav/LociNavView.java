package com.lauriedugdale.loci.ui.nav;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.activity.upload.UploadActivity;

/**
 * logic behind the navigation buttons.
 *
 * @author Laurie Dugdale
 */

public class LociNavView extends FrameLayout implements ViewPager.OnPageChangeListener {

    // represents the four buttons
    private ImageView mCenterImage;
    private FrameLayout mStartImage;
    private FrameLayout mEndImage;
    private ImageView mBottomImage;
    private ImageView mCenterBackgroundImage;

    private FrameLayout mContainer;

    // indicator of current page
    private View mIndicator;

    // variables for changing the colour of the buttons on scroll left and right
    private ArgbEvaluator mArgbEvaluator;
    private int mCenterColor;
    private int mSideColor;


    private int mEndViewsTranslationX;
    private int mIndicatorTranslationX;
    private int mCenterTranslationY;

    public LociNavView(@NonNull Context context) {
        this(context, null);
    }

    public LociNavView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LociNavView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public void setInvisible(){
        mContainer.setVisibility(INVISIBLE);
    }

    public void setVisible(){
        mContainer.setVisibility(VISIBLE);
    }

    /**
     * Initialise the ImageViews
     */
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.nav_main, this, true);

        mCenterBackgroundImage = (ImageView) findViewById(R.id.vst_center_background_image);
        mCenterImage = (ImageView) findViewById(R.id.vst_center_image);
//        mCenterImageAdd = (ImageView) findViewById(R.id.vst_center_image_add);
        mStartImage = (FrameLayout) findViewById(R.id.vst_start_image);
        mEndImage = (FrameLayout) findViewById(R.id.vst_end_image);
        mBottomImage = (ImageView) findViewById(R.id.vst_bottom_image);

        mIndicator = findViewById(R.id.vst_indicator_image);

        mContainer = (FrameLayout) findViewById(R.id.nav_menu);

        mCenterColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        mSideColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);

        mArgbEvaluator = new ArgbEvaluator();
        mIndicatorTranslationX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());

        /*
         *
         */
        mBottomImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                mEndViewsTranslationX = (int) ((mBottomImage.getX() - mStartImage.getX() - 25) - mIndicatorTranslationX);
                mBottomImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mCenterTranslationY = getHeight() - mBottomImage.getBottom();
            }
        });


    }

    /**
     * listeners for the tabbed icons (ImageView)
     * @param viewPager
     */
    public void setUpWithViewPager(final ViewPager viewPager, final Context context) {
        viewPager.addOnPageChangeListener(this);

        // go to page 0 on click of start image
        mStartImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem() != 0){
                    viewPager.setCurrentItem(0);
                }
            }
        });

        // go to page 1 on click however if already on page 1
        mCenterImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem() != 1){
                    viewPager.setCurrentItem(1);
                } else if (viewPager.getCurrentItem() == 1){
                    Intent startChildActivityIntent = new Intent(context, UploadActivity.class);
                    context.startActivity(startChildActivityIntent);
                }
            }
        });

        // go to page 2 on click of end image
        mEndImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem() != 2){
                    viewPager.setCurrentItem(2);
                }
            }
        });
    }

    @Override
    /**
     *
     */
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

//        mCenterImageAdd.setAlpha(1 - positionOffset);

        if (position == 0) {

            setColor(1 - positionOffset);
            moveViews( 1 - positionOffset);
            moveAndScaleCenter(1 - positionOffset);
            mIndicator.setTranslationX((positionOffset - 1) * mIndicatorTranslationX);
//            mCenterImageAdd.setAlpha(positionOffset);
        } else if ( position == 1){

            setColor(positionOffset);
            moveViews(positionOffset);
            moveAndScaleCenter(positionOffset);
            mIndicator.setTranslationX(positionOffset * mIndicatorTranslationX);
            if(positionOffset == 0.0f) {
//                mCenterImageAdd.setAlpha(1f);
            }
        } else if(position == 2){

//            mCenterImageAdd.setAlpha(0f);
        }

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     *
     * @param fractionFromCenter
     */
    private void moveAndScaleCenter(float fractionFromCenter){

        float scale = .7f + ((1 - fractionFromCenter) * .3f);

        mCenterImage.setScaleX(scale);
        mCenterImage.setScaleY(scale);
        mCenterBackgroundImage.setScaleX(scale);
        mCenterBackgroundImage.setScaleY(scale);

        int translation = (int) (fractionFromCenter * mCenterTranslationY);

        mCenterBackgroundImage.setTranslationY(translation);
        mCenterImage.setTranslationY(translation);
        mBottomImage.setTranslationY(translation);

        mBottomImage.setAlpha(1 - fractionFromCenter);
    }

    /**
     *
     * @param fractionFromCenter
     */
    private void moveViews(float fractionFromCenter) {
        mStartImage.setTranslationX(fractionFromCenter * mEndViewsTranslationX);
        mEndImage.setTranslationX(-fractionFromCenter * mEndViewsTranslationX);


        mIndicator.setAlpha(fractionFromCenter);
        mIndicator.setScaleX(fractionFromCenter);
    }

    /**
     *
     * @param fractionFromCenter
     */
    private void setColor(float fractionFromCenter) {

        int color = (int) mArgbEvaluator.evaluate(fractionFromCenter, mCenterColor, mSideColor);

        mCenterImage.setColorFilter(color);
    }
}
