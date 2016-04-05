package com.breadwallet.tools.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.breadwallet.presenter.BreadWalletApp;
import com.breadwallet.presenter.activities.MainActivity;
import com.breadwallet.tools.animation.FragmentAnimator;
import com.breadwallet.tools.animation.SpringAnimator;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail on 6/29/15.
 * Copyright (c) 2015 Mihail Gutan <mihail@breadwallet.com>
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

@SuppressLint("NewApi")
public class ParallaxViewPager extends ViewPager {
    public static final String TAG = ParallaxViewPager.class.getName();

    private static final int FIT_WIDTH = 0;
    private static final int FIT_HEIGHT = 1;
    public static final float OVERLAP_FULL = 1f;
    private static final float OVERLAP_HALF = 0.5f;
    public static final float OVERLAP_QUARTER = 0.25f;
    private static final float CORRECTION_PERCENTAGE = 0.01f;
    private Bitmap bitmap;
    private Rect source, destination;
    private int scaleType;
    private int chunkWidth;
    private int projectedWidth;
    private float overlap;
    private OnPageChangeListener secondOnPageChangeListener;

    public ParallaxViewPager(Context context) {
        super(context);
        init();
    }


    public ParallaxViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        source = new Rect();
        destination = new Rect();
        scaleType = FIT_HEIGHT;
        overlap = OVERLAP_HALF;


        //noinspection deprecation
        setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (bitmap != null) {
                    source.left = (int) Math.floor((position + positionOffset - CORRECTION_PERCENTAGE) * chunkWidth);
                    source.right = (int) Math.ceil((position + positionOffset + CORRECTION_PERCENTAGE) * chunkWidth + projectedWidth);
                    destination.left = (int) Math.floor((position + positionOffset - CORRECTION_PERCENTAGE) * getWidth());
                    destination.right = (int) Math.ceil((position + positionOffset + 1 + CORRECTION_PERCENTAGE) * getWidth());
                    invalidate();
//                    Log.e(TAG, "The bitmap params: " + bitmap.getWidth() + "   " + bitmap.getHeight());
                }

                if (secondOnPageChangeListener != null) {
                    secondOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageSelected(final int position) {
                if (secondOnPageChangeListener != null) {
                    secondOnPageChangeListener.onPageSelected(position);
                }
                MainActivity app = MainActivity.app;
                if (app != null) {
                    ((BreadWalletApp) app.getApplication()).cancelToast();
                    ((BreadWalletApp) app.getApplication()).hideKeyboard(app);
                }

                MainActivity.app.setPagerIndicator(position);
                if (FragmentAnimator.level == 0) {
                    if (position == 1) {
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                SpringAnimator.showBouncySlideHorizontal(getRootView(), SpringAnimator.TO_RIGHT, 15);
                            }
                        }, 80);
                    } else if (position == 0) {
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                SpringAnimator.showBouncySlideHorizontal(getRootView(), SpringAnimator.TO_LEFT, 15);
                            }
                        }, 80);
                    }
                }

//                Log.e("AdapterParalax", "Showing animation!!!!!");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (secondOnPageChangeListener != null) {
                    secondOnPageChangeListener.onPageScrollStateChanged(state);
                }
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        destination.top = 0;
        destination.bottom = h;
        if (getAdapter() != null && bitmap != null)
            calculateParallaxParameters();
    }

    private void calculateParallaxParameters() {
        if (bitmap.getWidth() < getWidth() && bitmap.getWidth() < bitmap.getHeight() && scaleType == FIT_HEIGHT) {
//            Log.w(ParallaxViewPager.class.getName(), "Invalid bitmap bounds for the current device, parallax effect will not work.");
        }


        final float ratio = (float) getHeight() / bitmap.getHeight();
        if (ratio != 1) {
            switch (scaleType) {
                case FIT_WIDTH:
                    source.top = (int) ((bitmap.getHeight() - bitmap.getHeight() / ratio) / 2);
                    source.bottom = bitmap.getHeight() - source.top;
                    chunkWidth = (int) Math.ceil((float) bitmap.getWidth() / (float) getAdapter().getCount());
                    projectedWidth = chunkWidth;
                    break;
                case FIT_HEIGHT:
                default:
                    source.top = 0;
                    source.bottom = bitmap.getHeight();
                    projectedWidth = (int) Math.ceil(getWidth() / ratio);
                    chunkWidth = (int) Math.ceil((bitmap.getWidth() - projectedWidth) / (float) getAdapter().getCount() * overlap);
                    break;
            }
        }
    }


    /**
     * Sets the background from a resource file.
     *
     * @param resid
     */
    @Override
    public void setBackgroundResource(int resid) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;
        options.inTargetDensity = 100;
        bitmap = BitmapFactory.decodeResource(getResources(), resid, options);
    }


    /**
     * Sets the background from a Drawable.
     *
     * @param background
     */
    @Override
    public void setBackground(Drawable background) {
        bitmap = ((BitmapDrawable) background).getBitmap();
    }


    /**
     * Deprecated.
     * Sets the background from a Drawable.
     *
     * @param background
     */
    @Override
    public void setBackgroundDrawable(Drawable background) {
        bitmap = ((BitmapDrawable) background).getBitmap();
    }


    /**
     * Sets the background from a bitmap.
     *
     * @param bitmap
     * @return The ParallaxViewPager object itself.
     */
    public ParallaxViewPager setBackground(Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }


    /**
     * Sets how the view should scale the background. The available choices are:
     * <ul>
     * <li>FIT_HEIGHT - the height of the image is resized to matched the height of the View, also stretching the width to keep the aspect ratio. The non-visible part of the bitmap is divided into equal parts, each of them sliding in at the proper position.</li>
     * <li>FIT_WIDTH - the width of the background image is divided into equal chunks, each taking up the whole width of the screen.</li>
     * </ul>
     *
     * @param scaleType
     * @return
     */
    public ParallaxViewPager setScaleType(final int scaleType) {
        if (scaleType != FIT_WIDTH && scaleType != FIT_HEIGHT)
            throw new IllegalArgumentException("Illegal argument: scaleType must be FIT_WIDTH or FIT_HEIGHT");
        this.scaleType = scaleType;
        return this;
    }


    /**
     * Sets the amount of overlapping with the setOverlapPercentage(final float percentage) method. This is a number between 0 and 1, the smaller it is, the slower is the background scrolling.
     *
     * @param percentage
     * @return The ParallaxViewPager object itself.
     */
    public ParallaxViewPager setOverlapPercentage(final float percentage) {
        if (percentage <= 0 || percentage >= 1)
            throw new IllegalArgumentException("Illegal argument: percentage must be between 0 and 1");
        overlap = percentage;
        return this;
    }


    /**
     * Recalculates the parameters of the parallax effect, useful after changes in runtime.
     *
     * @return The ParallaxViewPager object itself.
     */
    public ParallaxViewPager invalidateParallaxParameters() {
        calculateParallaxParameters();
        return this;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null)
            canvas.drawBitmap(bitmap, source, destination, null);
    }

    public void addOnPageChangeListener(OnPageChangeListener listener) {
        secondOnPageChangeListener = listener;
    }
}