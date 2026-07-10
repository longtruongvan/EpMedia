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

    private float mLastAngle;
    private boolean mIsRotating;

    private ScaleGestureDetector mScaleGestureDetector;
    private OnTransformListener mListener;

    public interface OnTransformListener {
        void onScale(float scaleFactor);
        void onTranslate(float dx, float dy);
        void onRotate(float deltaAngle);
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

    private float getAngle(MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            return (float) Math.toDegrees(Math.atan2(dy, dx));
        }
        return 0f;
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
                mIsRotating = false;
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (event.getPointerCount() == 2) {
                    mLastAngle = getAngle(event);
                    mIsRotating = true;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex == INVALID_POINTER_ID) {
                    break;
                }

                final float x = event.getRawX();
                final float y = event.getRawY();

                if (!mScaleGestureDetector.isInProgress() && !mIsRotating) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    if (mListener != null) {
                        mListener.onTranslate(dx, dy);
                    }
                }

                if (event.getPointerCount() >= 2 && mIsRotating) {
                    float currentAngle = getAngle(event);
                    float deltaAngle = currentAngle - mLastAngle;
                    
                    if (deltaAngle > 180f) deltaAngle -= 360f;
                    else if (deltaAngle < -180f) deltaAngle += 360f;

                    if (mListener != null) {
                        mListener.onRotate(deltaAngle);
                    }
                    mLastAngle = currentAngle;
                }
                
                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                mIsRotating = false;
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
                    mActivePointerId = event.getPointerId(newPointerIndex);
                    mLastTouchX = event.getRawX();
                    mLastTouchY = event.getRawY();
                }
                
                if (event.getPointerCount() - 1 < 2) {
                    mIsRotating = false;
                }
                break;
            }
        }
        return true;
    }
}
