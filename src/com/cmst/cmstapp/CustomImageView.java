package com.cmst.cmstapp;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class CustomImageView extends ImageView {

  public int NONE = 0;

  public int DRAG = 1;

  public int Zoom = 2;

  // FOR SCALING
  public float MIN_SCALE = 1f;

  public float MAX_SCALE = 5f;

  // Matrix for translation(panning) and scaling
  private Matrix matrix;
  private float[] m;

  // Mode to maintain the 3 states
  private int mode = NONE;

  // to keep track of points (start and last point)
  private PointF lastPoint = new PointF();
  private PointF startPoint = new PointF();

  // Parameters required to keep track of parentViewSize , oldContentSize and contentViewSize
  private int parentViewWidth, parentViewHeight;
  private float saveScale = 1f;
  private float origContentWidth, origContentHeight;

  // For scaling
  private ScaleGestureDetector mScaleDetector;
  private static final int MAX_CLICK_DURATION = 200;
  private long startClickTime;

  private float downX, upX;
  private float downY, upY;

  private static Boolean isScaled = false;

  int MIN_DISTANCE = 150;
  int clickCount = 0;

  long duration;
  // Context
  private Context mycontext;

  public CustomImageView(Context context) {
    super(context);
    constructingCustomComp(context);
  }

  private void stopInterceptEvent() {
    getParent().requestDisallowInterceptTouchEvent(true);
  }

  private void startInterceptEvent() {
    getParent().requestDisallowInterceptTouchEvent(false);
  }

  private void constructingCustomComp(Context context) {
    super.setClickable(true);
    this.mycontext = context;
    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    matrix = new Matrix();
    m = new float[9];
    setImageMatrix(matrix);
    setScaleType(ScaleType.MATRIX);

    setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {

        mScaleDetector.onTouchEvent(event);
        PointF curr = new PointF(event.getX(), event.getY());

        switch (event.getAction()) {

        case MotionEvent.ACTION_DOWN:
          lastPoint.set(curr);
          startPoint.set(lastPoint);
          mode = DRAG;
          startClickTime = Calendar.getInstance().getTimeInMillis();
          if (event.getPointerCount() == 1) {
            downX = event.getX();
            downY = event.getY();
            clickCount++;
          } else {
            downX = 0;
            downY = 0;
          }
          break;

        case MotionEvent.ACTION_MOVE:
          if (mode == DRAG) {
            float differenceXVal = curr.x - lastPoint.x;
            float differenceYVal = curr.y - lastPoint.y;
            float fixTransX = getFixDragTrans(differenceXVal, parentViewWidth, origContentWidth
                * saveScale);
            float fixTransY = getFixDragTrans(differenceYVal, parentViewHeight, origContentHeight
                * saveScale);
            if (fixTransX == 0 || saveScale == 1.0) {
              startInterceptEvent();
            } else {
              stopInterceptEvent();
            }
            matrix.postTranslate(fixTransX, fixTransY);
            fixTrans();
            lastPoint.set(curr.x, curr.y);
            // Log.e("CIV", "1 clickCount  = 0");

          }
          if (event.getPointerCount() == 2) {
            // Clear all the values on 2 finger selection
            upX = 0;
            upY = 0;
            downX = 0;
            downY = 0;
            startClickTime = 0;
            Log.e("CIV", "2 clickCount  = 0");
            clickCount = 0;
            duration = 0;
            startClickTime = 0;
          }
          break;

        case MotionEvent.ACTION_UP:
          mode = NONE;
          if (event.getPointerCount() == 1) {
            upX = event.getX();
            upY = event.getY();
          } else {
            upX = 0;
            upY = 0;
          }
          startInterceptEvent();

          if (saveScale < MIN_SCALE) {
            saveScale = MIN_SCALE;
            isScaled = false;
          }

          long time = Calendar.getInstance().getTimeInMillis() - startClickTime;
          duration = duration + time;
          Log.e("CIV", "Double tap 1   : ClickCount: " + clickCount);
          if (clickCount == 2 && event.getPointerCount() == 1) {
            Log.e("CIV", "Double tap 2   : ClickCount: " + clickCount);
            // if (duration < MAX_CLICK_DURATION) {
            ((SlideshowActivity) mycontext).zoomInAnim();
            // }
            Log.e("CIV", "3 clickCount  = 0");
            clickCount = 0;
            duration = 0;
            return true;
          }

          float delta = Math.abs(downY - upY);
          if (downY < upY && delta > 50 && event.getPointerCount() == 1) {
          } else if (downY > upY && delta > 50 && !isScaled && event.getPointerCount() == 1) {
            Log.e("CIV", "4 clickCount  = 0");
            clickCount = 0;
            duration = 0;
            ((SlideshowActivity) mycontext).onTVMode();
          }

          break;

        case MotionEvent.ACTION_POINTER_UP:
          mode = NONE;
          break;
        }

        setImageMatrix(matrix);
        invalidate();

        return false;
        // indicates event was handled
      }

    });
  }

  private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
      mode = Zoom;
      return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
      if (saveScale <= MIN_SCALE) {
        isScaled = false;
      }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      upX = 0;
      upY = 0;
      downX = 0;
      downY = 0;

      float mScaleFactor = detector.getScaleFactor();
      float origScale = saveScale;
      saveScale *= mScaleFactor;
      // Not to scale after maxScale is reached.
      if (saveScale > MAX_SCALE) {

        saveScale = MAX_SCALE;
        mScaleFactor = MAX_SCALE / origScale;
        isScaled = true;
      }
      // Not to scale after minScale is reached.
      else if (saveScale < MIN_SCALE) {

        saveScale = MIN_SCALE;
        mScaleFactor = MIN_SCALE / origScale;
        isScaled = false;
      }

      // If Scaled ImageSize is less than viewSize then scale according to
      // viewSize
      if (origContentWidth * saveScale <= parentViewWidth
          || origContentHeight * saveScale <= parentViewHeight) {
        matrix.postScale(mScaleFactor, mScaleFactor, parentViewWidth / 2, parentViewHeight / 2);
        isScaled = true;
      }
      // If scaled imageSize is greater than viewSize then scale according
      // to detector x and y focus.
      else {
        matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
        isScaled = true;
      }

      fixTrans();

      return true;
    }
  }

  // To avoid panning the content Image out of Views size by retrieving the x
  // and y positions from the matrix
  void fixTrans() {
    // Copies values from matrix to m array
    matrix.getValues(m);
    float transX = m[Matrix.MTRANS_X];
    float transY = m[Matrix.MTRANS_Y];

    float fixTransX = getFixTrans(transX, parentViewWidth, origContentWidth * saveScale);
    float fixTransY = getFixTrans(transY, parentViewHeight, origContentHeight * saveScale);

    if (fixTransX != 0 || fixTransY != 0)
      matrix.postTranslate(fixTransX, fixTransY);
  }

  // To avoid panning the content Image out of Views size
  float getFixTrans(float trans, float viewSize, float contentSize) {
    float minTrans, maxTrans;

    if (contentSize <= viewSize) {
      minTrans = 0;
      maxTrans = viewSize - contentSize;
    } else {
      minTrans = viewSize - contentSize;
      maxTrans = 0;
    }

    if (trans < minTrans)
      return -trans + minTrans;
    if (trans > maxTrans)
      return -trans + maxTrans;
    return 0;
  }

  // To drag to content view if(contentsize(Image size) is less then parent
  // view size)
  float getFixDragTrans(float delta, float viewSize, float contentSize) {
    if (contentSize <= viewSize) {
      return 0;
    }
    return delta;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    // To obtain the parent component width and height.
    parentViewWidth = MeasureSpec.getSize(widthMeasureSpec);
    parentViewHeight = MeasureSpec.getSize(heightMeasureSpec);

    if (saveScale == 1) {
      // To fit to screen.
      float scale;

      // Obtain the w and h of the imageview(Bitmap image)
      Drawable drawable = getDrawable();
      if (drawable == null || drawable.getIntrinsicWidth() == 0
          || drawable.getIntrinsicHeight() == 0)
        return;
      int bmWidth = drawable.getIntrinsicWidth();
      int bmHeight = drawable.getIntrinsicHeight();

      // scaling the bitmap image to fit the screen.
      float scaleX = (float) parentViewWidth / (float) bmWidth;
      float scaleY = (float) parentViewHeight / (float) bmHeight;
      scale = Math.min(scaleX, scaleY);
      matrix.setScale(scale, scale);

      // calculating the redundant space(vs-(scale*bnSize)) to Center the
      // image
      float redundantYSpace = (float) parentViewHeight - (scale * (float) bmHeight);
      float redundantXSpace = (float) parentViewWidth - (scale * (float) bmWidth);
      redundantYSpace /= (float) 2;
      redundantXSpace /= (float) 2;
      matrix.postTranslate(redundantXSpace, redundantYSpace);

      // original width of the image after scaling it by a scaling
      // factor.
      origContentWidth = parentViewWidth - 2 * redundantXSpace;
      origContentHeight = parentViewHeight - 2 * redundantYSpace;
      setImageMatrix(matrix);
    }
    fixTrans();
  }
}