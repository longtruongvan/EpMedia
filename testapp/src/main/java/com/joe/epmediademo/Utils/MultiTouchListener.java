package com.joe.epmediademo.Utils;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class MultiTouchListener implements View.OnTouchListener {

    private static final int INVALID_POINTER_ID = -1;

    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;

    private ScaleGestureDetector mScaleGestureDetector;
    private OnTransformListener mListener;

    public interface OnTransformListener {
        void onScale(float scaleFactor);
        void onTranslate(float dx, float dy);
        void onTransformEnded();
    }

    public MultiTouchListener(Context context, OnTransformListener listener) {
        mListener = listener;
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (mListener != null) {
                    mListener.onScale(detector.getScaleFactor());
                }
                return true;
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);

        final int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = event.getActionIndex();
                final float x = event.getRawX();
                final float y = event.getRawY();

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = event.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex == INVALID_POINTER_ID) {
                    break;
                }

                // Since we translate the view, we must use raw X and Y to calculate deltas
                final float x = event.getRawX();
                final float y = event.getRawY();

                if (!mScaleGestureDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    if (mListener != null) {
                        mListener.onTranslate(dx, dy);
                    }
                }
                
                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                if (mListener != null) {
                    mListener.onTransformEnded();
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);

                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    // Note: finding the actual rawX for a specific pointer requires
                    // some math or we just skip updating rawX here and let the next MOVE handle it.
                    // A simple hack is to just invalidate the last touch and reset on next MOVE.
                    mActivePointerId = event.getPointerId(newPointerIndex);
                    // It's safer to just let the jump happen or reset lastTouch variables
                    mLastTouchX = event.getRawX(); // This might be slightly off if multiple fingers are far apart, but acceptable
                    mLastTouchY = event.getRawY();
                }
                break;
            }
        }
        return true;
    }
}
