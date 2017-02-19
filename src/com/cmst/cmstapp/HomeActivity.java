package com.cmst.cmstapp;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cmst.common.AlarmService;
import com.cmst.common.Constants;
import com.cmst.upload.GalleryPickerActivity;

/**
 * Activity which is shown on application startup and displays
 * 
 * the image and video thumb nails.
 * 
 * @author test
 */
public class HomeActivity extends FragmentActivity {
  Context context;
  public static CMSTResponseReceiver mReceiver;
  public AlarmManager alarmManager;
  PendingIntent pendingIntent;
  AlbumFragment albumListFragment;
  DrawerLayout settingDrawer;
  ListView mdrawerList;
  ImageView menuAlbum;
  ImageView menuUpload;
  ImageView menuGallery;
  View layout;

  /**
   * Called when home activity is created.
   */
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    context = this;
    setUpActionBarIcon();

    albumListFragment = new AlbumFragment();
    showAlbums();
  }

  void setUpActionBarIcon() {
    getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getActionBar().setCustomView(R.layout.header);
    ImageView actionLeft = (ImageView) findViewById(R.id.action_left);

    actionLeft.setVisibility(View.INVISIBLE);

    ImageView actionRight = (ImageView) findViewById(R.id.action_right);
    actionRight.setVisibility(View.INVISIBLE);

    TextView actionTitle = (TextView) findViewById(R.id.action_title);
    actionTitle.setText(Constants.cmstName);
  }

  /**
   * Adding onclick for footer icons.
   */
  private void initBtnClick() {
    /**
     * Find out which is the current active activity.
     */
    ActivityManager mactivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> RunningTask = mactivityManager.getRunningTasks(1);
    ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
    final String curActivity = ar.topActivity.getClassName().toString().split("\\.")[3];
    menuAlbum = (ImageView) findViewById(R.id.album);
    menuAlbum.setImageDrawable(getResources().getDrawable(R.drawable.albumselected));
    /**
     * Adding onclick for Goto album screen.
     */
    menuAlbum.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View view) {
        if (!curActivity.equalsIgnoreCase("HomeActivity")) {
          Intent intent = new Intent(context, HomeActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          startActivity(intent);
          finish();
        }

      }
    });
    menuUpload = (ImageView) findViewById(R.id.upload);
    /**
     * Adding onclick for upload photos to CMST.
     */
    menuUpload.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), GalleryPickerActivity.class);
        startActivity(intent);
      }
    });
    menuGallery = (ImageView) findViewById(R.id.gallery);
    /**
     * Adding onclick for Goto All media
     */
    menuGallery.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!curActivity.equalsIgnoreCase("DeviceGallery")) {
          Intent intent = new Intent(HomeActivity.this, AllMediaActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          intent.putExtra("index", 0);
          startActivity(intent);
          finish();
        }
      }
    });
  }

  /**
   * Show gridview for tablet version
   */
  public void showAlbums() {
    RotateAnimation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    anim.setInterpolator(new LinearInterpolator());
    anim.setRepeatCount(Animation.INFINITE);
    anim.setDuration(1500);
    setContentView(R.layout.fragment_layout);

    try {
      ImageView view = (ImageView) findViewById(R.id.albumLoading);
      view.startAnimation(anim);
    } catch (Exception e) {
      e.printStackTrace();
    }
    initBtnClick();

  }

  /**
   * Called when activity starts or resumes.
   */
  protected void onResume() {
    super.onResume();
  }

  /**
   * Called when activity is paused.
   */
  protected void onPause() {
    super.onPause();
  }

  /**
   * Called when activity is Stopped.
   */
  protected void onStop() {
    super.onStop();

  }

  /**
   * Called when activity is destroyed.
   */
  protected void onDestroy() {
    super.onDestroy();

  }

  /**
   * Called when return key is pressed
   */
  public void onBackPressed() {
    AlarmService.getInstance(getApplicationContext()).stop();
    new DoLogout().execute("");
  }

  private class DoLogout extends AsyncTask<String, Void, String> {

    protected String doInBackground(String... params) {
      StringBuilder thumbListUrl = new StringBuilder();
      thumbListUrl.append(Constants.ipAddress).append(Constants.apiLogout)
          .append(Constants.sessionId);
      HttpClient client = new DefaultHttpClient();
      HttpGet get = new HttpGet(thumbListUrl.toString());
      Log.e("AllMedia Screen ", "%%%%%%%%%%%%% API: " + thumbListUrl);
      try {
        HttpResponse response = client.execute(get);
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();

        return String.valueOf(statusCode);
      } catch (ClientProtocolException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }

    protected void onPostExecute(String statusCode) {
      // Clear shared preferences
      SharedPreferences sh_Pref = getSharedPreferences("IPAddress", MODE_PRIVATE);
      Editor toEdit = sh_Pref.edit();
      toEdit.clear();
      toEdit.commit();

      Constants.sessionId = "";
      Constants.cmstName = "";
      Intent intent = new Intent();
      if (Constants.enableDiscovery) {
        intent.setClass(HomeActivity.this, DiscoveryActivity.class);
      } else {
        intent.setClass(HomeActivity.this, ManualDiscoveryActivity.class);
      }

      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      finish();
    }
  }

}
