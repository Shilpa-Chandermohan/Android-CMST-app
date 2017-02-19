package com.cmst.cmstapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * 
 * @Filename: CMSTResponseReceiver.java
 * @author: Demo
 * @version: 0.1
 * @Description: Receives the response from the service
 */
public class CMSTResponseReceiver extends ResultReceiver {
  private Receiver mreceiver;

  /**
   * 
   * Response receiver method.
   * 
   * @param handler
   */
  public CMSTResponseReceiver(Handler handler) {
    super(handler);
  }

  /**
   * 
   * Set receiver method.
   * 
   * @param receiver
   */
  public void setReceiver(Receiver receiver) {
    mreceiver = receiver;
  }

  /**
   * 
   * Method called when response is received.
   */
  public interface Receiver {
    public void onReceiveResult(int resultCode, Bundle resultData);
  }

  /**
   * 
   * Callback called when response is received.
   * 
   * @param resultCode
   * @param resultData
   */
  @Override
  protected void onReceiveResult(int resultCode, Bundle resultData) {
    Log.e("CMST", "inside response receiver "+mreceiver);
    if (mreceiver != null) {
      mreceiver.onReceiveResult(resultCode, resultData);
    }
  }
}
