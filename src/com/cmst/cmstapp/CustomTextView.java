package com.cmst.cmstapp;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 
 * @Filename: CustomTextView.java
 * @version: 0.1
 * @Description: Used to customize the text view
 */

public class CustomTextView extends TextView {
  /**
   * 
   * Parameterized constructor.
   * 
   * @param context
   * @param attrs
   * @param defstyle
   */
  public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init((attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "textStyle") != null ? true
        : false));
  }

  /**
   * 
   * Parameterized constructor.
   * 
   * @param context
   * @param attrs
   */
  public CustomTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init((attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "textStyle") != null ? true
        : false));
  }

  /**
   * 
   * Parameterized constructor.
   * 
   * @param context
   */
  public CustomTextView(Context context) {
    super(context);
    init(false);
  }

  /**
   * 
   * Method to set the font style for the text.
   */
  private void init(boolean bold) {
    Typeface tf = null;
    if (bold) {
      tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
    } else {
      tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
    }
    setTypeface(tf);
  }

}