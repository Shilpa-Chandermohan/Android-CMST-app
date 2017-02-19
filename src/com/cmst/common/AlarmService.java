package com.cmst.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.cmst.cmstapp.CMSTPollingReceiver;

public class AlarmService {

  private static AlarmService obj;

  private static final String TAG = AlarmService.class.getSimpleName();

  private static final long INTERVAL = 300000;

  private PendingIntent operation;

  private AlarmManager alarmManager;

  private static final int REQUEST_CODE = 0;

  private AlarmService(Context appContext) {
    alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(appContext, CMSTPollingReceiver.class);
    operation = PendingIntent.getBroadcast(appContext, REQUEST_CODE, intent, 0);
  }

  public static final AlarmService getInstance(Context appContext) {
    if (obj == null) {
      obj = new AlarmService(appContext);
    }
    return obj;
  }

  public void start() {
    Log.d(TAG, "Alarm Service START");
    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
        INTERVAL, operation);
  }

  public void stop() {
    if (alarmManager != null) {
      Log.d(TAG, "Alarm Service stop");
      alarmManager.cancel(operation);
    }
  }

}
