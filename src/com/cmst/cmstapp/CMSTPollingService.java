package com.cmst.cmstapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

/**
 * 
 * @Filename: CMSTPollingService.java
 * @author: Demo
 * @version: 0.1
 * @Description: Used to start a service and fetch data from theCMST device
 */
public class CMSTPollingService extends IntentService {

  public static final int STATUS_RUNNING = 0;
  public static final int STATUS_FINISHED = 1;
  public static final int STATUS_ERROR = 2;

  private static final String TAG = "DownloadService";

  /**
   * 
   * Constructor.
   */
  public CMSTPollingService() {
    super(CMSTPollingService.class.getName());
  }

  /**
   * 
   * Called when service has started and sends the result back to the calling activity.
   */
  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(TAG, "Service Started!");
    final ResultReceiver receiver = intent.getParcelableExtra("receiver");
    String url = intent.getStringExtra("url");

    Bundle bundle = new Bundle();
    String apiType = intent.getStringExtra("apiType");
    if (!TextUtils.isEmpty(url)) {

      if (receiver != null) {
        receiver.send(STATUS_RUNNING, Bundle.EMPTY);
      }

      try {
        String results = downloadData(url);
        /* Sending result back to activity */
        if (null != results && results.length() > 0) {
          bundle.putString("result", results);
          bundle.putString("apiType", apiType);
          if (receiver != null) {
            receiver.send(STATUS_FINISHED, bundle);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        /* Sending error message back to activity */
        bundle.putString(Intent.EXTRA_TEXT, e.toString());
        receiver.send(STATUS_ERROR, bundle);
      }
    }
    Log.d(TAG, "Service Stopping!");
    this.stopSelf();
  }

  /**
   * 
   * Sending a http request to fetch the data.
   * 
   * @param requestUrl
   */
  private String downloadData(String requestUrl) throws IOException, DownloadException {

    HttpURLConnection urlConnection = null;

    /* forming the java.net.URL object */
    URL url = new URL(requestUrl);
    urlConnection = (HttpURLConnection) url.openConnection();

    /* for Get request */
    urlConnection.setRequestMethod("GET");
    // set the connection timeout to 5 seconds and the read timeout to 10 seconds
    urlConnection.setConnectTimeout(5000);
    urlConnection.setReadTimeout(10000);

    int statusCode = urlConnection.getResponseCode();

    /* 200 represents HTTP OK */
    if (statusCode == 200) {

      BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line + "\n");
      }
      Log.e("CMSTPollingService", sb.toString());
      br.close();
      return sb.toString();
    } else {
      throw new DownloadException(getString(R.string.failed_data));
    }
  }

  @SuppressWarnings("serial")
  public class DownloadException extends Exception {
    public DownloadException(String message) {
      super(message);
    }

    public DownloadException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
