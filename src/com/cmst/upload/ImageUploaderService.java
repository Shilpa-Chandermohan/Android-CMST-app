package com.cmst.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cmst.cache.util.SimpleResponse;
import com.cmst.cmstapp.R;
import com.google.gson.Gson;

public class ImageUploaderService extends IntentService {

  private static final String TAG = ImageUploaderService.class.getSimpleName();

  private static final int MAX_PERCENTAGE = 100;

  private static final int ID = 11;

  public ImageUploaderService() {
    super(TAG);
    setIntentRedelivery(false);
  }

  private SimpleResponse fireServerApi(String url) throws ClientProtocolException, IOException {
    HttpClient newHttpClient = new DefaultHttpClient();
    HttpGet httpGetForLock = new HttpGet(url);
    HttpResponse result = newHttpClient.execute(httpGetForLock);
    Gson gson = new Gson();
    SimpleResponse simpleResponse = gson.fromJson(new InputStreamReader(result.getEntity()
        .getContent()), SimpleResponse.class);
    return simpleResponse;
  }

  @Override
  protected void onHandleIntent(Intent intent) {

    Log.d(TAG, "onHandleIntent");
    String curstrSessionId = intent.getStringExtra(ImageUploadConstants.EXTRA_SESSION_ID);
    ArrayList<String> listOfImages = intent
        .getStringArrayListExtra(ImageUploadConstants.EXTRA_SELECTED_ITEMS_KEY);
    String curstrAlbumId = intent.getStringExtra(ImageUploadConstants.EXTRA_ALBUM_ID_KEY);
    String curstrAlbumRawdate = intent.getStringExtra(ImageUploadConstants.EXTRA_ALBUM_RAWDATE);
    String curstrCMSTIP = intent.getStringExtra(ImageUploadConstants.EXTRA_CMST_IP);

    // printing
    Log.d(TAG, " Total no of images =" + listOfImages.size());
    Log.d(TAG, "SessionId ==" + curstrSessionId);
    Log.d(TAG, "curstrCMSTIP ==" + curstrCMSTIP);
    Log.d(TAG, "AlbumRawDate ==" + curstrAlbumRawdate);
    Log.d(TAG, "AlbumId ==" + curstrAlbumId);

    int amountForEachItem = 0;
    try {
      amountForEachItem = MAX_PERCENTAGE / listOfImages.size();
    } catch (Exception e) {
      e.printStackTrace();
    }
    int progress = 0;

    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

    builder.setContentTitle("Uploading to CMST");
    builder.setSmallIcon(R.drawable.ic_launcher);
    builder.setProgress(MAX_PERCENTAGE, progress, false);
    manager.notify(ID, builder.build());

    // set lock
    String lockurl = curstrCMSTIP + "/api/stg/lckimpt?sessId=" + curstrSessionId;

    SimpleResponse lockResponse = null;
    SimpleResponse setUploadGroupFlagResponse = null;
    try {
      lockResponse = fireServerApi(lockurl);
      if (lockResponse.getRes() == ImageUploadConstants.SUCCESS) {
        Log.i(TAG, "Success in setting LOCK");

        // set upload - group flag
        String strgrpflg = curstrCMSTIP + "/api/stg/setupldgrpflg?sessId=" + curstrSessionId
            + "&flg=" + ImageUploadConstants.SET_UPLOAD_GROUP_FLAG;
        setUploadGroupFlagResponse = fireServerApi(strgrpflg);
        if (setUploadGroupFlagResponse.getRes() == ImageUploadConstants.SUCCESS) {
          Log.i(TAG, "Success in setting UploadGroupFlag");

          // start uploading...

          Log.d(TAG, "Starting Upload ....");
          for (int i = 0; i < listOfImages.size(); i++) {

            String strAddItemUrl = curstrCMSTIP + "/api/stg/additm" + "?sessId=" + curstrSessionId
                + "&fname=" + listOfImages.get(i) + "&albmId=" + curstrAlbumId + "&albmRawDate="
                + curstrAlbumRawdate;

            SimpleResponse uploadResponse = uploadPhoto(strAddItemUrl, listOfImages.get(i), i);
            if (uploadResponse.getRes() != ImageUploadConstants.SUCCESS) {
              Log.e(TAG, "--------------------------------------------");
              Log.e(TAG, "Upload failed  for -> " + listOfImages.get(i));
              Log.e(TAG, "--------------------------------------------");

              // remove progress
              builder.setProgress(0, 0, false);
              builder.setContentText((i + 1) + "/" + listOfImages + " uploaded");
              manager.notify(ID, builder.build());
              break;
            } else {
              builder.setContentText((i + 1) + "/" + listOfImages.size());
              progress += amountForEachItem;
              builder.setProgress(MAX_PERCENTAGE, progress, false);
              manager.notify(ID, builder.build());
            }

          }
          Log.d(TAG, " Upload Finished");

        } else {
          Log.e(TAG, "Error in setting Upload group Flag , response code is -> "
              + setUploadGroupFlagResponse.getRes());
          // remove progress
          builder.setProgress(0, 0, false);
          builder.setContentText("Upload Failed");
          manager.notify(ID, builder.build());
        }
      } else {
        Log.e(TAG, "Error in setting LOCK , response code is -> " + lockResponse.getRes());
        // remove progress
        builder.setProgress(0, 0, false);
        builder.setContentText("Upload Failed");
        manager.notify(ID, builder.build());

      }
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, "Exception occurred , msg -> " + e.getLocalizedMessage());
      // remove progress
      builder.setProgress(0, 0, false);
      builder.setContentText("Upload Failed");
      manager.notify(ID, builder.build());
    } finally {

      Log.e(TAG, "Finally Block");
      if (setUploadGroupFlagResponse != null) {
        if (setUploadGroupFlagResponse.getRes() == ImageUploadConstants.SUCCESS) {
          // reset the upload group flag
          String resetGrpflgURL = curstrCMSTIP + "/api/stg/setupldgrpflg?sessId=" + curstrSessionId
              + "&flg=" + ImageUploadConstants.RESET_UPLOAD_GROUP_FLAG;
          try {
            SimpleResponse resetFlagResponse = fireServerApi(resetGrpflgURL);
            if (resetFlagResponse.getRes() == ImageUploadConstants.SUCCESS) {
              Log.d(TAG, "Reset flag Successfull");
            } else {
              Log.e(TAG, "Reset flag FAILURE");
            }
          } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Reset flag FAILURE, exception -> " + e.getMessage());
          }
        }
      }

      if (lockResponse != null) {
        if (lockResponse.getRes() == ImageUploadConstants.SUCCESS) {
          // release the lock
          String releaseLockUrl = curstrCMSTIP + "/api/stg/unlckimpt?sessId=" + curstrSessionId;
          try {
            SimpleResponse releaseLockResponse = fireServerApi(releaseLockUrl);
            if (releaseLockResponse.getRes() == ImageUploadConstants.SUCCESS) {
              Log.d(TAG, "Release LOCK Successfull");
            } else {
              Log.e(TAG, "Release LOCK FAILURE");
            }
          } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Release LOCK FAILURE, exception -> " + e.getMessage());
          }
        }
      }
    }
  }

  private SimpleResponse uploadPhoto(String strAddItemUrl, String uploadFilePath, int number)
      throws ClientProtocolException, IOException {

    Log.d(TAG, "Number -> " + number + "Url -> " + strAddItemUrl + " uploadFile Path : "
        + uploadFilePath);
    DefaultHttpClient httpclient = new DefaultHttpClient();
    HttpPost httpPostRequest = new HttpPost(strAddItemUrl);
    File imgFileForUpload = new File(uploadFilePath);
    FileBody filebody_ph = new FileBody(imgFileForUpload);
    MultipartEntityBuilder multiPartEntityBuilder = MultipartEntityBuilder.create();
    multiPartEntityBuilder.addPart(String.valueOf(number), filebody_ph);
    httpPostRequest.setEntity(multiPartEntityBuilder.build());

    HttpResponse result = httpclient.execute(httpPostRequest);
    Gson gson = new Gson();
    SimpleResponse simpleResponse = gson.fromJson(new InputStreamReader(result.getEntity()
        .getContent()), SimpleResponse.class);
    return simpleResponse;

  }

}
