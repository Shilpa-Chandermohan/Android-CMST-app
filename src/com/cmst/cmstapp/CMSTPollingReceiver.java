package com.cmst.cmstapp;

import java.text.DateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cmst.common.Constants;

/**
 * 
 * @Filename: CMSTPollingReceiver.java
 * @author: Demo
 * @version: 0.1
 * @Description: It receives the request to reset the session timer and prevent the session from
 *               expiring
 */
public class CMSTPollingReceiver extends BroadcastReceiver {
  /**
   * 
   * Callback function that gets called to reset the session.
   */
  @Override
  public void onReceive(Context context, Intent intent) {
    if (!Constants.ipAddress.equalsIgnoreCase("") && !Constants.sessionId.equalsIgnoreCase("")) {
      Intent dailyUpdater = new Intent(context, CMSTPollingService.class);
      String strUrl = Constants.ipAddress + Constants.apiResetSessId.concat(Constants.sessionId);
      dailyUpdater.putExtra("url", strUrl);
      dailyUpdater.putExtra("apiType", "resetSession");
      dailyUpdater.putExtra("receiver", DiscoveryActivity.mReceiver);
      Log.e("Home Activity", DateFormat.getDateInstance().format(new Date()));
      Log.e("Home Activity", "%%%%%%% API  : " + strUrl);
      context.startService(dailyUpdater);

    }
  }
}