package com.cmst.cmstapp;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/**
 * Class used to detect gestures like SWIPE_UP,SWIPE_DOWN,
 * 
 * SWIPE_LEFT , SWIPE_RIGHT.
 * 
 * @author test
 * 
 */
public class GestureDetection extends SimpleOnGestureListener {

  /**
   * Variables to hold the swipe gesture values.
   */
  public final static int SWIPE_UP = 1;
  public final static int SWIPE_DOWN = 2;
  public final static int SWIPE_LEFT = 3;
  public final static int SWIPE_RIGHT = 4;

  public final static int MODE_SOLID = 1;
  public final static int MODE_DYNAMIC = 2;

  public final static int DOUBLE_TAP = 2;

  private final static int ACTION_FAKE = -13; // just an unlikely number

  private int mode = MODE_DYNAMIC;
  private boolean running = true;

  private Activity context;

  private SimpleGestureListener listener;

  private GestureDetector detector;

  /**
   * Constructor.
   * 
   * @param context
   * @param sgl
   */
  public GestureDetection(Activity context, SimpleGestureListener sgl) {

    this.context = context;
    this.detector = new GestureDetector(context, this);
    this.listener = sgl;
  }

  /**
   * Called by the one who creates this object for getting swipe
   * 
   * gesture notifications.
   * 
   * @param event
   */
  public Boolean onTouchEvent(MotionEvent event) {

    if (!this.running)
      return false;

    this.detector.onTouchEvent(event);

    /*
     * if (this.mode == MODE_SOLID) event.setAction(MotionEvent.ACTION_CANCEL); else if (this.mode
     * == MODE_DYNAMIC) {
     * 
     * if (event.getAction() == ACTION_FAKE) event.setAction(MotionEvent.ACTION_UP); else if
     * (result) event.setAction(MotionEvent.ACTION_CANCEL); else if (this.tapIndicator) {
     * event.setAction(MotionEvent.ACTION_DOWN); this.tapIndicator = false; }
     * 
     * }
     */
    // else just do nothing, it's Transparent
    return true;
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

    final float xDistance = Math.abs(e1.getX() - e2.getX());
    final float yDistance = Math.abs(e1.getY() - e2.getY());

    /*
     * if (xDistance > this.swipe_Max_Distance || yDistance > this.swipe_Max_Distance) return false;
     */

    boolean result = false;

    if (xDistance > yDistance) {
      if (e1.getX() > e2.getX()) // right to left
        this.listener.onSwipe(SWIPE_RIGHT);
      else
        this.listener.onSwipe(SWIPE_LEFT);

      result = true;
    } else {
      if (e1.getY() > e2.getY()) // bottom to up
        this.listener.onSwipe(SWIPE_UP);
      else
        this.listener.onSwipe(SWIPE_DOWN);

      result = true;
    }

    return result;
  }

  @Override
  public boolean onSingleTapConfirmed(MotionEvent arg) {

    if (this.mode == MODE_DYNAMIC) {
      arg.setAction(ACTION_FAKE);

      this.context.dispatchTouchEvent(arg);
    }

    return false;
  }

  @Override
  public boolean onDoubleTap(MotionEvent e) {

    if (listener != null) {
      listener.onTap(DOUBLE_TAP);
    }

    return super.onDoubleTap(e);
  }

  interface SimpleGestureListener {
    void onSwipe(int direction);

    void onTap(int noOfTimes);

    boolean onSingleTapConfirmed(MotionEvent arg);

  }

}
