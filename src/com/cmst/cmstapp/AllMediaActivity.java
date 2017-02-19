package com.cmst.cmstapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cmst.cache.util.AlbumItemDetails;
import com.cmst.cache.util.Cache;
import com.cmst.common.AlarmService;
import com.cmst.common.AllMediaData;
import com.cmst.common.Constants;

/**
 * 
 * @Filename: AllMediaActivity.java
 * @author: Demo
 * @version: 0.1
 * @Description: Show all items from all the albums
 */

public class AllMediaActivity extends Activity {
  /**
   * Main menu buttons.
   */
  private ImageView menuAlbum;
  private ImageView menuUpload;
  private ImageView menuGallery;

  private ImageView allMediaLoader;
  private GridviewAdapter gridAdapter;
  private GridView allMediaGrid;
  private ImageView actionLeft;
  private RotateAnimation anim;
  private RotateAnimation popanim;
  private ImageView metaProgress;
  private Dialog metaDialog;
  private Dialog shadowDialog;
  private View pieChart;
  private GetAllMedia taskAlbumData;
  private LinearLayout memoryContainer;
  private StorageData taskMetaData;

  /**
   * Called on activity is created.
   */
  @Override
  public final void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setTitle(R.string.allmedia_title);
    if (Constants.isTablet) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    setContentView(R.layout.allmedia);

    allMediaLoader = (ImageView) findViewById(R.id.allMediaLoader);
    anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    anim.setInterpolator(new LinearInterpolator());
    anim.setRepeatCount(Animation.INFINITE);
    anim.setDuration(1500);
    allMediaLoader.startAnimation(anim);
    allMediaGrid = (GridView) findViewById(R.id.allMediaGrid);

    if (Constants.isTablet) {
      allMediaGrid.setNumColumns(8);
    } else {
      allMediaGrid.setNumColumns(5);
    }
    initBtnClick();
    allMediaGrid.setOnItemClickListener(mitemMulClickListener);
    setUpActionBarIcon();
    loadAllMedia();
    initMetaPopup();
  }

  /**
   * Initializes the metadata popup.
   */
  public void initMetaPopup() {

    metaDialog = new Dialog(AllMediaActivity.this,
        android.R.style.Theme_NoTitleBar_OverlayActionModes);
    metaDialog.setCanceledOnTouchOutside(true);
    shadowDialog = new Dialog(AllMediaActivity.this,
        android.R.style.Theme_NoTitleBar_OverlayActionModes);
    shadowDialog.setContentView(R.layout.shadow);
    shadowDialog.getWindow().setBackgroundDrawable(
        new ColorDrawable(android.graphics.Color.TRANSPARENT));
    metaDialog.setContentView(R.layout.memory);

    metaDialog.getWindow().setBackgroundDrawable(
        new ColorDrawable(android.graphics.Color.TRANSPARENT));

    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    lp.copyFrom(metaDialog.getWindow().getAttributes());
    if (Constants.isTablet) {
      lp.width = 900;
    } else {
      lp.width = 970;
    }
    lp.height = 1300;
    metaDialog.getWindow().setAttributes(lp);
    metaProgress = (ImageView) metaDialog.findViewById(R.id.metaProgress);
    popanim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    popanim.setInterpolator(new LinearInterpolator());
    popanim.setRepeatCount(Animation.INFINITE);
    popanim.setDuration(1500);
    metaProgress.startAnimation(popanim);
    TextView btnOk = (TextView) metaDialog.findViewById(R.id.btn_ok);
    metaDialog.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        if (taskMetaData != null) {
          taskMetaData.cancel(true);
        }
        memoryContainer.removeView(pieChart);
        shadowDialog.dismiss();
      }
    });
    metaDialog.setOnKeyListener(new Dialog.OnKeyListener() {

      @Override
      public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
          if (taskMetaData != null) {
            taskMetaData.cancel(true);
          }
          shadowDialog.dismiss();
          memoryContainer.removeView(pieChart);
          metaDialog.dismiss();
        }
        return true;
      }
    });
    btnOk.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (taskMetaData != null) {
          taskMetaData.cancel(true);
        }
        shadowDialog.dismiss();
        memoryContainer.removeView(pieChart);
        metaDialog.dismiss();
      }
    });
  }

  /**
   * Class that creates the piechart for memory popup.
   */
  private class PieChart extends View {
    private float sweepAngle;

    public PieChart(Context context, float sweepAng) {
      super(context);
      sweepAngle = 360 - sweepAng;
    }

    @SuppressLint({ "DrawAllocation", "ResourceAsColor" })
    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);

      Paint paint4 = new Paint();
      final RectF rect = new RectF();

      paint4.setColor(Color.BLACK);
      Log.e("meta height", "" + memoryContainer.getHeight());
      canvas.drawCircle(getWidth() / 2, getHeight() / 2, memoryContainer.getHeight() / 3, paint4);

      float radius = (float) (memoryContainer.getHeight() / 3.42);
      rect.set(getWidth() / 2 - radius, getHeight() / 2 - radius, getWidth() / 2 + radius,
          getHeight() / 2 + radius);
      rect.set(getWidth() / 2 - radius, getHeight() / 2 - radius, getWidth() / 2 + radius,
          getHeight() / 2 + radius);
      Paint paint1 = new Paint();
      paint1.setColor(getResources().getColor(R.color.cmstMemoryBlue));
      paint1.setStrokeWidth(14);
      paint1.setAntiAlias(true);
      paint1.setStrokeCap(Paint.Cap.BUTT);
      paint1.setStyle(Paint.Style.STROKE);
      canvas.drawArc(rect, 270, sweepAngle, false, paint1);
      paint4.setColor(Color.WHITE);

      canvas.drawCircle(getWidth() / 2, getHeight() / 2, memoryContainer.getHeight() / 4, paint4);
    }
  }

  /**
   * Shows the meta data popup.
   */
  public void showMetaPopup() {
    try {
      try {
        shadowDialog.show();
        metaDialog.show();
        memoryContainer = (LinearLayout) metaDialog.findViewById(R.id.memory_container);
        Log.e("memory_container=", "" + memoryContainer);

      } catch (Exception e) {
        e.printStackTrace();
      }

    } catch (Exception e) {
      Log.e(Constants.TAG, "show Meta Data : " + e.getMessage());
    }
  }

  /**
   * To customize the title bar.
   */
  void setUpActionBarIcon() {
    getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getActionBar().setCustomView(R.layout.header);
    actionLeft = (ImageView) findViewById(R.id.action_left);

    actionLeft.setImageDrawable(getResources().getDrawable(R.drawable.active_search));

    actionLeft.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
      }
    });

    ImageView actionRight = (ImageView) findViewById(R.id.action_right);
    actionRight.setImageDrawable(getResources().getDrawable(R.drawable.active_memory));
    actionRight.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (DiscoveryActivity.checkForWiFi(AllMediaActivity.this)) {
          taskMetaData = new StorageData();
          taskMetaData.execute();
          showMetaPopup();
        } else {
          DiscoveryActivity.errorDialog(AllMediaActivity.this, getString(R.string.err),
              Constants.getErrorMsg(getBaseContext(), 0, "wifi"), false, "");
        }
      }
    });
    TextView actionTitle = (TextView) findViewById(R.id.action_title);
    actionTitle.setText(Constants.allMediaTitle);
  }

  /**
   * Called on activity is created.
   */
  private void initBtnClick() {
    ActivityManager mactivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> runningTask = mactivityManager.getRunningTasks(1);
    ActivityManager.RunningTaskInfo ar = runningTask.get(0);
    final String curActivity = ar.topActivity.getClassName().toString().split("\\.")[3].toString();

    menuAlbum = (ImageView) findViewById(R.id.album);
    menuAlbum.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!curActivity.equalsIgnoreCase("HomeActivity")) {
          Intent intent = new Intent(getBaseContext(), HomeActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          startActivity(intent);
          finish();
        }
      }
    });

    menuUpload = (ImageView) findViewById(R.id.upload);
    menuGallery = (ImageView) findViewById(R.id.gallery);
    menuGallery.setImageDrawable(getResources().getDrawable(R.drawable.allmediaselected));
  }

  /**
   * Called when activity is stopped.
   */
  protected void onStop() {
    super.onStop();

  }

  /**
   * Called when activity is destroyed.
   */
  protected void onDestroy() {
    super.onDestroy();

    if (metaDialog != null) {
      if (metaDialog.isShowing())
        metaDialog.dismiss();
      metaDialog = null;
    }
    if (shadowDialog != null) {
      if (shadowDialog.isShowing())
        shadowDialog.dismiss();
      shadowDialog = null;
    }
  }

  /**
   * Called when activity is resumed.
   */
  protected void onResume() {
    super.onResume();
  }

  /**
   * Called when return key is pressed.
   */
  public void onBackPressed() {
    if (taskAlbumData != null) {
      taskAlbumData.cancel(true);
    }
    AlarmService.getInstance(getApplicationContext()).stop();
    new DoLogout().execute("");
  }

  /**
   * Call the API to fetch all the album data.
   */
  public void loadAllMedia() {
    try {
      taskAlbumData = new GetAllMedia();
      taskAlbumData.execute("");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Adding onclick to each gridview element to call slideshow.
   */
  AdapterView.OnItemClickListener mitemMulClickListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
      try {
        Intent intent = new Intent(AllMediaActivity.this, SlideshowActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("previousScreen", "AllMedia");

        // ** Create a Bundle and Put Bundle in to it
        // Bundle bundleObject = new Bundle();
        // bundleObject.putSerializable("myMediaList", allMediaList);

        // ** Put Bundle in to Intent and call start Activity
        // intent.putExtras(bundleObject);
        startActivity(intent);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };

  /**
   * Asynchronous task to fetch allmedia data.
   */
  private class GetAllMedia extends AsyncTask<String, Void, ArrayList<AlbumItemDetails>> {
    protected void onPreExecute() {
      allMediaLoader.startAnimation(anim);
      allMediaLoader.setVisibility(View.VISIBLE);
    }

    @Override
    protected ArrayList<AlbumItemDetails> doInBackground(String... params) {
      if (isCancelled()) {
        return null;
      }
      // return getAllMedia
      return Cache.getInstance().getAllMedia(Constants.cmstName, getApplicationContext());
      // bserverError = false;
      // StringBuilder strUrl = new StringBuilder().append(Constants.ipAddress)
      // .append(Constants.apiGetAlbmCnt).append(Constants.sessionId);
      // Log.e("AllMedia", "%%%%%%% API  : " + strUrl);
      // StringBuilder data = new StringBuilder();
      // String line;
      //
      // try {
      // HttpClient countFetchingClient = new DefaultHttpClient();
      // HttpGet httpGetAlbumCount = new HttpGet(strUrl.toString());
      // HttpResponse resultForCount = countFetchingClient.execute(httpGetAlbumCount);
      // AlbumCountResponse albumCount = new AlbumCountResponse();
      // BufferedReader rd = new BufferedReader(new InputStreamReader(resultForCount.getEntity()
      // .getContent()));
      // while ((line = rd.readLine()) != null) {
      // data.append(line + "\n");
      // }
      //
      // JSONObject jsonObj = null;
      // try {
      // jsonObj = new JSONObject(data.toString());
      // serverStatus = jsonObj.getInt("res");
      //
      // } catch (JSONException e) {
      // e.printStackTrace();
      // }
      //
      // if (Constants.getServerStatus(serverStatus)) {
      // albumCount.setRes(jsonObj.getInt("res"));
      // albumCount.setAlbumCnt(jsonObj.getInt("albmNum"));
      // StringBuilder thumbListUrl = new StringBuilder().append(Constants.ipAddress)
      // .append(Constants.apiGetThmbLst).append(Constants.sessionId)
      // .append("&prevNum=" + albumCount.getAlbumCnt());
      // Log.e("AllMedia", "%%%%%%% API  : " + thumbListUrl);
      //
      // HttpClient newHttpClient = new DefaultHttpClient();
      // HttpGet httpGetForAlbumList = new HttpGet(thumbListUrl.toString());
      // HttpResponse result = newHttpClient.execute(httpGetForAlbumList);
      // AlbumData albumList = new AlbumData();
      // data.setLength(0);
      // line = "";
      // rd = new BufferedReader(new InputStreamReader(result.getEntity().getContent()));
      // while ((line = rd.readLine()) != null) {
      // data.append(line + "\n");
      // }
      // try {
      // jsonObj = new JSONObject(data.toString());
      // serverStatus = jsonObj.getInt("res");
      // if (Constants.getServerStatus(serverStatus)) {
      // albumList.setRes(jsonObj.getInt("res"));
      // JSONArray jsonArr = jsonObj.getJSONArray("albmThmbList");
      // ArrayList<AlbumThmbList> arrLst = new ArrayList<AlbumThmbList>();
      //
      // for (int i = 0; i < jsonArr.length(); i++) {
      // AlbumThmbList albmLst = new AlbumThmbList();
      // albmLst.setAlbumId(jsonArr.getJSONObject(i).getInt("albmId"));
      // albmLst.setAlbumDate(jsonArr.getJSONObject(i).getString("albmDate"));
      // albmLst.setAlbumName(jsonArr.getJSONObject(i).getString("albmName"));
      // albmLst.setAlbumRawDate(jsonArr.getJSONObject(i).getString("albmRawDate"));
      // albmLst.setTPath(jsonArr.getJSONObject(i).getString("tpath"));
      // albmLst.setOrientation(jsonArr.getJSONObject(i).getInt("orientation"));
      // arrLst.add(albmLst);
      // }
      // albumList.setAlbumThmbList(arrLst);
      // } else {
      // return null;
      // }
      // } catch (JSONException e) {
      // e.printStackTrace();
      // }
      //
      // return albumList;
      // } else {
      // return null;
      // }
      //
      // } catch (Exception e) {
      // bserverError = true;
      // e.printStackTrace();
      // Log.e(tag, "Exception in fetching album list");
      // }

    }

    /**
     * If success then call album list from CMST device else show error dialog.
     * 
     * @param: Result
     */
    @Override
    protected void onPostExecute(ArrayList<AlbumItemDetails> list) {
      allMediaLoader.clearAnimation();
      allMediaLoader.setVisibility(View.GONE);
      if (isCancelled()) {
        return;
      }

      AllMediaData mediaData;
      mediaData = AllMediaData.getInstance();
      mediaData.allMediaList = list;
      gridAdapter = new GridviewAdapter(AllMediaActivity.this, list);

      // allMediaList = list;
      // gridAdapter.addAllMediaItems(list);
      allMediaGrid.setAdapter(gridAdapter);
    }
  }

  private class StorageData extends AsyncTask<String, Void, String> {

    protected void onPreExecute() {
      metaProgress.startAnimation(popanim);
      metaProgress.setVisibility(View.VISIBLE);
    }

    @Override
    protected String doInBackground(String... params) {
      if (isCancelled()) {
        return "";
      }
      StringBuilder data = new StringBuilder();
      String line = "";
      try {
        StringBuilder strMetaUrl = new StringBuilder();
        strMetaUrl.append(Constants.ipAddress).append(Constants.apiGetStorageInfo)
            .append(Constants.sessionId);
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(strMetaUrl.toString());
        Log.e(Constants.TAG, strMetaUrl.toString());

        try {
          HttpResponse response = client.execute(get);
          StatusLine statusLine = response.getStatusLine();
          int statusCode = statusLine.getStatusCode();
          if (statusCode == 200) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity()
                .getContent()));
            while ((line = rd.readLine()) != null) {
              data.append(line + "\n");
            }
            Log.e("CMST RESPONSE : ", "" + data);
          } else {
            return null;

          }
        } catch (ClientProtocolException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return data.toString();
    }

    protected void onPostExecute(String result) {
      metaProgress.clearAnimation();
      metaProgress.setVisibility(View.GONE);
      if (isCancelled() || result == "") {
        return;
      }

      if (result == null) {
        DiscoveryActivity.errorDialog(AllMediaActivity.this, getString(R.string.err),
            Constants.getErrorMsg(getBaseContext(), 0, "network"), false, "");
        return;
      }

      JSONObject jsonObj = null;

      try {
        jsonObj = new JSONObject(result);
        if (Constants.getServerStatus(jsonObj.getInt("res"))) {
          Float consumedMemory;

          Float freeSpace;
          consumedMemory = (Float.parseFloat(jsonObj.getString("space")) - Float.parseFloat(jsonObj
              .getString("level"))) / (1024 * 1024 * 1024);

          freeSpace = Float.parseFloat(jsonObj.getString("level")) / (1024 * 1024 * 1024);
          int free = Math.round(freeSpace);
          TextView freeAvailableSpace = (TextView) metaDialog.findViewById(R.id.freeSpace);
          freeAvailableSpace.setText("" + free + "GB");
          Float totalMemory;
          totalMemory = Float.parseFloat(jsonObj.getString("space")) / (1024 * 1024 * 1024);
          int tot = Math.round(totalMemory);

          TextView totalSpace = (TextView) metaDialog.findViewById(R.id.totalSize);
          totalSpace.setText("" + tot + "GB");
          int consumed = Math.round(consumedMemory);
          long sweep = (consumed * 360) / tot;

          pieChart = new PieChart(getBaseContext(), sweep);
          memoryContainer.addView(pieChart);
        } else {

          if (jsonObj.getInt("res") == 2001 || jsonObj.getInt("res") == 2002
              || jsonObj.getInt("res") == 2003 || jsonObj.getInt("res") == 2004) {

            SharedPreferences sh_Pref = getSharedPreferences("IPAddress", MODE_PRIVATE);
            Editor toEdit = sh_Pref.edit();
            toEdit.clear();
            toEdit.commit();

            Constants.sessionId = "";
            Constants.cmstName = "";

            DiscoveryActivity.errorDialog(AllMediaActivity.this, getString(R.string.err), Constants
                .getErrorMsg(AllMediaActivity.this.getBaseContext(), jsonObj.getInt("res"), ""),
                true, "allmedia");
          } else {
            DiscoveryActivity.errorDialog(AllMediaActivity.this, getString(R.string.err), Constants
                .getErrorMsg(AllMediaActivity.this.getBaseContext(), jsonObj.getInt("res"), ""),
                false, "");
          }
        }

      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
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
        intent.setClass(AllMediaActivity.this, DiscoveryActivity.class);
      } else {
        intent.setClass(AllMediaActivity.this, ManualDiscoveryActivity.class);
      }
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      finish();
    }
  }

}
