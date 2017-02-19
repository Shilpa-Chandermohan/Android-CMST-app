package com.cmst.cmstapp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.PointF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmst.cache.sync.SyncInitiator;
import com.cmst.cache.util.CacheConstants;
import com.cmst.cache.util.ResyncDataResponse;
import com.cmst.common.AlarmService;
import com.cmst.common.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DiscoveryActivity extends Activity implements OnItemClickListener,
    CMSTResponseReceiver.Receiver {

  private ImageView bonjourLoadingerr;
  private ListView listView;
  /**
   * adapter to display list of devices in a network.
   */
  private BonjourListViewAdapter adapter;
  /**
   * denotes service type of devices to be listed.
   */
  private static final String SERVICE_TYPE = "_http._tcp.local.";
  /**
   * handler to run device detection
   */

  private RotateAnimation anim;
  private ImageView view;
  private ImageView refreshText;
  private SharedPreferences sh_Pref;
  private Editor toEdit;

  private WifiManager wifiManager;

  private MulticastLock lock;

  private JmDNS jmDns;
  private DeviceDiscovery taskDiscovery;
  private LinearLayout errorCont;
  private LinearLayout loading;

  private Boolean exitDiscovery;
  private SharedPreferences prefs;

  private ProgressDialog progressDlg;
  private Intent pollingIntent;
  Context context;
  public static CMSTResponseReceiver mReceiver;
  public AlarmManager alarmManager;
  PendingIntent pendingIntent;
  public static AlertDialog alertDlg = null;

  /**
   * Called when activity is started.
   * 
   * @param savedInstanceState
   */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    /**
     * changes orientation to landscape for Tablet.
     */
    if (Constants.isTablet) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    context = this;
    exitDiscovery = false;
    setContentView(R.layout.discovery);
    if (!Constants.isTablet) {
      LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT,
          0.0f);
      LinearLayout left_right = (LinearLayout) findViewById(R.id.listLeft);
      left_right.setLayoutParams(param);
      left_right = (LinearLayout) findViewById(R.id.listRight);
      left_right.setLayoutParams(param);
      RelativeLayout relCenter = (RelativeLayout) findViewById(R.id.listCenter);
      relCenter
          .setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 100.0f));
    }
    refreshText = (ImageView) findViewById(R.id.refreshText);
    errorCont = (LinearLayout) findViewById(R.id.bonjourContainer);
    loading = (LinearLayout) findViewById(R.id.bonjourErrorCnt);
    listView = (ListView) findViewById(R.id.bonjourListview);
    listView.setOnItemClickListener(this);
    adapter = new BonjourListViewAdapter(getApplicationContext(), this, listView);
    prefs = this.getSharedPreferences("deviceList", Context.MODE_PRIVATE);

    // sh_Pref = getSharedPreferences("deviceList", MODE_PRIVATE);
    // toEdit = sh_Pref.edit();
    // removeDevicesList();

    Constants.cmstCount = prefs.getInt("cmstCount", 0);

    if (Constants.cmstCount == 0) {
      taskDiscovery = new DeviceDiscovery();
      taskDiscovery.execute();
    } else {
      setDataFromSharedPref();
    }

    setUpActionBarIcon();

    /**
     * Loading animation for device discovery.
     */
    anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    anim.setInterpolator(new LinearInterpolator());
    anim.setRepeatCount(Animation.INFINITE);
    anim.setDuration(1500);
    view = (ImageView) findViewById(R.id.bonjourLoading);
    bonjourLoadingerr = (ImageView) findViewById(R.id.bonjourLoadingerr);
    // /**
    // * Called when we want to refresh the discovery

    bonjourLoadingerr.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        if (checkForWiFi(DiscoveryActivity.this)) {
          errorCont.setVisibility(LinearLayout.VISIBLE);
          loading.setVisibility(LinearLayout.INVISIBLE);
          taskDiscovery = new DeviceDiscovery();
          taskDiscovery.execute();

        } else {
          errorDialog(DiscoveryActivity.this, getString(R.string.err),
              Constants.getErrorMsg(getBaseContext(), 0, "wifi"), false, "");
        }

      }
    });
    view.startAnimation(anim);

    /**
     * Start device discovery.
     */
    final GestureDetector gesture = new GestureDetector(DiscoveryActivity.this,
        new GestureDetector.SimpleOnGestureListener() {

          @Override
          public boolean onDown(MotionEvent e) {
            return false;
          }

          @Override
          public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            PointF p1 = new PointF(e1.getX(), e1.getY());
            PointF p2 = new PointF(e2.getX(), e2.getY());
            float diffY = p2.y - p1.y;
            float diffX = p2.x - p1.x;

            int dirY = (diffY < 0) ? -1 : 1;
            if (diffX < diffY) {
              if (dirY > 0) {
                Log.e("bottom", "b");
                // discoveryHandler.removeCallbacks(discoveryRunner);
                // if (oldServiceInfo != null) {
                // adapter.removeFromList(oldServiceInfo);
                // }
                listView.setAdapter(null);
                taskDiscovery = new DeviceDiscovery();
                taskDiscovery.execute();
                LinearLayout loading = (LinearLayout) findViewById(R.id.bonjourContainer);
                loading.setVisibility(LinearLayout.VISIBLE);
                refreshText.setVisibility(View.INVISIBLE);
                view.startAnimation(anim);
              }
            }
            return false;
          }
        });
    listView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return gesture.onTouchEvent(event);
      }
    });

    registerReceiver(myBroadcastReceiver, new IntentFilter(CacheConstants.SYNC_FINISHED_ACTION));
  }

  /**
   * Method to set data from shared preferences
   */
  public void setDataFromSharedPref() {
    LinearLayout loading = (LinearLayout) findViewById(R.id.bonjourContainer);
    loading.setVisibility(LinearLayout.INVISIBLE);
    refreshText.setVisibility(View.VISIBLE);
    listView.setAdapter(adapter);
    adapter.setData(null);
    adapter.notifyDataSetChanged();
  }

  /**
   * Method to store CMST name,IPAddress and port number using shared preference
   */
  public void sharedPrefernces(String cmstName, String cmstIp, int cmstPortNumber) {
    sh_Pref = getSharedPreferences("IPAddress", MODE_PRIVATE);
    toEdit = sh_Pref.edit();
    toEdit.putString("ipAaddress", cmstIp);
    toEdit.putString("CMSTId", cmstName);
    toEdit.putString("CMSTPortNumber", Integer.toString(cmstPortNumber));
    toEdit.commit();
  }

  /**
   * Method to store all CMST device names
   */
  public void storeDevicesList(ServiceInfo[] list) {
    sh_Pref = getSharedPreferences("deviceList", MODE_PRIVATE);
    toEdit = sh_Pref.edit();
    removeDevicesList();
    for (int loopVar = 0; loopVar < list.length; loopVar++) {
      toEdit.putString("cmstName_" + loopVar, list[loopVar].getName());
      toEdit.putString("cmstIp_" + loopVar, list[loopVar].getHostAddress());
      toEdit.putString("cmstPort_" + loopVar, Integer.toString(list[loopVar].getPort()));
    }
    toEdit.putInt("cmstCount", list.length);
    toEdit.commit();
  }

  /**
   * Method to remove all CMST device names
   */
  public void removeDevicesList() {
    toEdit.clear();
    toEdit.commit();
  }

  public void onBackPressed() {
    super.onBackPressed();
    exitDiscovery = true;

    Intent downloader = new Intent(getApplicationContext(), CMSTPollingReceiver.class);
    downloader.setData(Uri.parse("custom://" + Constants.ALARM_ID));
    downloader.setAction(Constants.ALARM_ID);
    downloader.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
        downloader, PendingIntent.FLAG_CANCEL_CURRENT);
    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(pendingIntent);

    if (pollingIntent != null)
      ((ContextWrapper) context).stopService(pollingIntent);

  }

  /**
   * To customize the title bar.
   */
  void setUpActionBarIcon() {
    getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getActionBar().setCustomView(R.layout.header);
    ImageView actionLeft = (ImageView) findViewById(R.id.action_left);
    actionLeft.setVisibility(View.INVISIBLE);

    ImageView actionRight = (ImageView) findViewById(R.id.action_right);
    actionRight.setVisibility(View.INVISIBLE);

    TextView actionTitle = (TextView) findViewById(R.id.action_title);
    actionTitle.setText("Select CMST");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return super.onCreateOptionsMenu(menu);
  }

  /**
   * Selection of CMST device from list view.
   */
  @SuppressWarnings("deprecation")
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    String IPAddr = "";
    Log.e("Discovery", ".............1");
    listView.setSelection(position);
    listView.setItemChecked(position, true);
    Log.e("Discovery", ".............2");
    if (checkForWiFi(DiscoveryActivity.this)) {
      Log.e("Discovery", ".............3");
      ServiceInfo serviceInformation = (ServiceInfo) adapter.getItem(position);
      if (serviceInformation != null) {
        IPAddr = serviceInformation.getHostAddress().toString();

        sharedPrefernces(serviceInformation.getName().toString(), IPAddr,
            serviceInformation.getPort());
        Log.e("Discovery", ".............4" + serviceInformation.getHostAddress());
        if (!serviceInformation.getHostAddress().equalsIgnoreCase("")) {
          Constants.ipAddress = "http://" + IPAddr;
          Log.e("Discovery", ".............4.1");
          Constants.cmstName = serviceInformation.getName().toString();
          Log.e("Discovery", ".............4.2");
          doLogin();
          Log.e("Discovery", ".............4.3");
        } else {
          Log.e("Discovery", ".............4 else ");
          errorDialog(DiscoveryActivity.this, getString(R.string.err),
              Constants.getErrorMsg(getBaseContext(), 0, "cmstError"), false, "");
        }
        Log.e("Discovery", ".............5");

      } else if (serviceInformation == null && Constants.cmstCount > 0) {
        sh_Pref = getSharedPreferences("deviceList", MODE_PRIVATE);
        Log.e("Discovery", ".............11");
        Constants.ipAddress = "http://" + sh_Pref.getString("cmstIp_" + position, "");
        Constants.cmstName = sh_Pref.getString("cmstName_" + position, "");
        Log.e("Discovery", ".............12");
        doLogin();
        Log.e("Discovery", ".............13");
        // if (!Constants.ipAddress.equalsIgnoreCase("http://"
        // + sh_Pref.getString("cmstIp_" + position, ""))
        // && !Constants.ipAddress.equalsIgnoreCase("")
        // && !Constants.sessionId.equalsIgnoreCase("")) {
        // Log.e("Constants.ipAddress=", "123" + Constants.ipAddress);
        // Log.e("shared=", "123" + sh_Pref.getString("cmstIp_" + position, ""));
        // new DoLogout().execute("");
        // tempIpAddress = "http://" + sh_Pref.getString("cmstIp_" + position, "");
        // } else {
        // Log.e("temp5=", "123" + sh_Pref.getString("cmstIp_" + position, ""));
        // Constants.ipAddress = "http://" + sh_Pref.getString("cmstIp_" + position, "");
        // Intent intent = new Intent(DiscoveryActivity.this, HomeActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // startActivity(intent);
        // finish();
        // }
        // Constants.cmstName = sh_Pref.getString("cmstName_" + position, "");
      }
    } else {
      errorDialog(DiscoveryActivity.this, getString(R.string.err),
          Constants.getErrorMsg(getBaseContext(), 0, "wifi"), false, "");
    }
    Log.e("Discovery", ".............00");
  }

  protected void onResume() {
    super.onResume();
  }

  protected void onPause() {
    super.onPause();
  }

  /**
   * Called when activity is killed.
   */
  protected void onDestroy() {
    super.onDestroy();

    unregisterReceiver(myBroadcastReceiver);

    // if (alertDlg != null) {
    // if (alertDlg.isShowing())
    // alertDlg.dismiss();
    // alertDlg = null;
    // }
    if (progressDlg != null) {
      if (progressDlg.isShowing())
        progressDlg.dismiss();
      progressDlg = null;
    }

  }

  private class DeviceDiscovery extends AsyncTask<Void, Void, ServiceInfo[]> {

    protected void onPreExecute() {

    }

    @Override
    protected ServiceInfo[] doInBackground(Void... params) {
      // Log.e(TAG, "Service Discovery will start -> " + JmDNS.VERSION);
      wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
      lock = wifiManager.createMulticastLock("DNS_LOCK");

      lock.setReferenceCounted(true);
      Log.e(Constants.TAG, "I will accquire the lock");
      lock.acquire();

      try {

        WifiManager wifi = (WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);

        WifiInfo wifiinfo = wifi.getConnectionInfo();
        int intaddr = wifiinfo.getIpAddress();

        byte[] byteaddr = new byte[] { (byte) (intaddr & 0xff), (byte) (intaddr >> 8 & 0xff),
            (byte) (intaddr >> 16 & 0xff), (byte) (intaddr >> 24 & 0xff) };
        InetAddress addr = InetAddress.getByAddress(byteaddr);

        jmDns = JmDNS.create(addr);
        ServiceInfo[] list = jmDns.list(SERVICE_TYPE);
        Log.e(Constants.TAG, "Length -> " + list.length);
        jmDns.close();

        lock.release();

        return list;
      } catch (UnknownHostException e1) {
        e1.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return null;
    }

    protected void onPostExecute(ServiceInfo[] list) {
      LinearLayout loading = (LinearLayout) findViewById(R.id.bonjourContainer);
      LinearLayout errorCont = (LinearLayout) findViewById(R.id.bonjourErrorCnt);
      if (list == null) {

        errorCont.setVisibility(LinearLayout.VISIBLE);
        loading.setVisibility(LinearLayout.INVISIBLE);
        return;
      }

      if (list.length <= 0) {
        errorCont.setVisibility(LinearLayout.VISIBLE);
        loading.setVisibility(LinearLayout.INVISIBLE);
        return;
      }

      storeDevicesList(list);
      loading.setVisibility(LinearLayout.INVISIBLE);
      refreshText.setVisibility(View.VISIBLE);
      listView.setAdapter(adapter);

      adapter.setData(list);

      adapter.notifyDataSetChanged();

    }

  }

  public void doLogin() {
    Log.e("Discovery", "dologin.........1");
    if (checkForWiFi(DiscoveryActivity.this)) {
      Log.e("Discovery", "dologin.........2");
      Log.d(Constants.TAG, Constants.sessionId);
      Log.e("Discovery", "dologin.........3");
      if (Constants.sessionId.equalsIgnoreCase("")) {
        progressDlg = ProgressDialog.show(this, getString(R.string.info),
            getString(R.string.sync_progress), true);
        mReceiver = new CMSTResponseReceiver(new Handler());
        mReceiver.setReceiver(this);
        Log.e("Home Activity", "%%%%%%% API  : " + Constants.ipAddress + Constants.apiLogin);
        // albumListFragment = new AlbumFragment();
        pollingIntent = new Intent(Intent.ACTION_SYNC, null, context, CMSTPollingService.class);
        pollingIntent.putExtra("url", Constants.ipAddress + Constants.apiLogin);
        pollingIntent.putExtra("apiType", "login");
        pollingIntent.putExtra("receiver", mReceiver);
        ((ContextWrapper) context).startService(pollingIntent);
      }
      // else {
      // showAlbums();
      // }
    } else {
      Log.e("CMST", "Wifi not available");
      errorDialog(context, getString(R.string.err),
          Constants.getErrorMsg(getBaseContext(), 0, "wifi"), false, "");
    }
  }

  /**
   * Callback that is called when service response is recieved
   */
  @Override
  public void onReceiveResult(int resultCode, Bundle resultData) {
    switch (resultCode) {
    case CMSTPollingService.STATUS_RUNNING:
      setProgressBarIndeterminateVisibility(true);
      break;
    case CMSTPollingService.STATUS_FINISHED:
      String str = String.valueOf(resultData.get("result"));
      String apiType = String.valueOf(resultData.get("apiType"));
      Log.e("Home Response", apiType);
      Log.e("Home Response", str);
      JSONObject obj = null;
      ResyncDataResponse data = null;
      try {

        obj = new JSONObject(str);
      } catch (JSONException e) {
        e.printStackTrace();
      }

      try {
        if (!Constants.getServerStatus(obj.getInt("res"))) {
          progressDlg.dismiss();
          errorDialog(DiscoveryActivity.this, getString(R.string.err),
              Constants.getErrorMsg(getBaseContext(), obj.getInt("res"), ""), false, "");

          return;
        }
      } catch (JSONException e1) {
        e1.printStackTrace();
      } catch (NullPointerException e2) {
        e2.printStackTrace();
      }

      if (apiType.equalsIgnoreCase("login")) {
        try {
          Constants.sessionId = obj.getString("sessId");
        } catch (JSONException e) {
          e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_SYNC, null, getBaseContext(),
            CMSTPollingService.class);
        StringBuilder strUrl = new StringBuilder();
        strUrl.append(Constants.ipAddress).append(Constants.apiSetSessId)
            .append(Constants.sessionId);
        strUrl.append("&timeout=").append(String.valueOf(Constants.sessionTimeout));
        Log.e("Home Activity", "%%%%%%% API  : " + strUrl);
        intent.putExtra("url", strUrl.toString());
        intent.putExtra("apiType", "setSession");
        intent.putExtra("receiver", mReceiver);
        startService(intent);
      } else if (apiType.equalsIgnoreCase("setSession")) {
        AlarmService.getInstance(getApplicationContext()).start();
        // setRecurringAlarm(context);
        // showAlbums();
        Log.e("Discover", "Start sync         1");
        SyncInitiator syncIn = new SyncInitiator(getApplicationContext());
        syncIn.startSync(Constants.ipAddress, Constants.cmstName,
            Long.valueOf(Constants.sessionId), CacheConstants.SYNC_FRESH, null);

      } else if (apiType.equalsIgnoreCase("resetSession")) {
        try {
          GsonBuilder gsonBuilder = new GsonBuilder();
          Gson gson = gsonBuilder.create();

          data = gson.fromJson(str, ResyncDataResponse.class);
          if (data.getAddalbmLst().size() != 0 || data.getDelalbmLst().size() != 0
              || data.getModalbmLst().size() != 0) {
            // call re-sync

            // progress = ProgressDialog.show(this, "CMST Sync", "Sync in progress, Please wait..",
            // true);
            Log.e("Discover", "Start sync         2");
            SyncInitiator syncIn = new SyncInitiator(getApplicationContext());
            syncIn.startSync(Constants.ipAddress, Constants.cmstName,
                Long.valueOf(Constants.sessionId), CacheConstants.SYNC_MODIFIED, data);

          }
        } catch (NullPointerException e) {
          e.printStackTrace();
        }

      }
      break;
    case CMSTPollingService.STATUS_ERROR:
      /* Handle the error */
      progressDlg.dismiss();
      errorDialog(DiscoveryActivity.this, getString(R.string.err),
          Constants.getErrorMsg(getBaseContext(), 0, "cmstError"), false, "");

      break;
    }
  }

  /**
   * Receiver object whose onReceive will get called
   * 
   * when sync with server is complete.
   */
  private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(Constants.TAG, "Sync Broadcast message recieved");

      if (null != intent && intent.getExtras() != null) {
        int syncResult = intent.getIntExtra(CacheConstants.EXTRA_SYNC_STATUS,
            CacheConstants.SYNC_FAILURE);
        Log.d(Constants.TAG, "Sync status : " + syncResult);
        if (syncResult == CacheConstants.SYNC_FAILURE) {

          Log.e(Constants.TAG, "Sync Failed ...");
          try {
            progressDlg.dismiss();
          } catch (Exception e) {
            // TODO: handle exception
          }
          AlarmService.getInstance(getApplicationContext()).stop();
          if (!Constants.sessionId.equalsIgnoreCase(""))
            new doLogout().execute("");
        } else if (syncResult == CacheConstants.SYNC_FRESH) {
          try {
            progressDlg.dismiss();
          } catch (Exception e) {
          }
          Intent myIntent = new Intent(DiscoveryActivity.this, HomeActivity.class);
          myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          startActivity(myIntent);
          // / finish();

          Log.e(Constants.TAG, "Yahoooooooo Sync complete ..");

          // showAlbums();

        } else if (syncResult == CacheConstants.SYNC_MODIFIED) {
          try {
            progressDlg.dismiss();
          } catch (Exception e) {
            // TODO: handle exception
          }

          Intent myIntent = new Intent(DiscoveryActivity.this, HomeActivity.class);
          myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          startActivity(myIntent);
          // / finish();

          Log.e(Constants.TAG, "Yahoooooooo Sync complete ..");

        }

      }

    }

  };

  private class doLogout extends AsyncTask<String, Void, String> {

    protected String doInBackground(String... params) {
      StringBuilder thumbListUrl = new StringBuilder();
      thumbListUrl.append(Constants.ipAddress).append(Constants.apiLogout)
          .append(Constants.sessionId);
      HttpClient client = new DefaultHttpClient();
      HttpGet get = new HttpGet(thumbListUrl.toString());
      Log.e("Discovery Screen ", "%%%%%%%%%%%%% API: " + thumbListUrl);
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
      errorDialog(DiscoveryActivity.this, getString(R.string.err),
          Constants.getErrorMsg(getBaseContext(), 0, "syncError"), false, "");
    }
  }

  /**
   * Method to store CMST name,IPAddress and port number using shared preference
   */
  public void sessionIdStorage(String sessionId) {
    sh_Pref = getSharedPreferences("IPAddress", MODE_PRIVATE);
    toEdit = sh_Pref.edit();
    toEdit.putString("sesionId", sessionId);
    toEdit.commit();
  }

  /**
   * Called when activity is destroyed.
   * 
   * @param context
   * @param error
   *          title
   * @param error
   *          message
   */
  public static void errorDialog(final Context context, String title, String message,
      final Boolean backToDiscovery, final String screenName) {
    try {
      Log.e(Constants.TAG, "error dialog ...........1" + alertDlg);
      // Log.e(Constants.TAG, "error dialog ...........1" + alertDlg.isShowing());
      if (alertDlg != null && alertDlg.isShowing()) {
        return;
      }
      Log.e(Constants.TAG, "error dialog ...........2");
      AlertDialog.Builder errBuilder = new AlertDialog.Builder(context);
      errBuilder.setTitle(title);
      errBuilder.setMessage(message);
      errBuilder.setCancelable(true);
      errBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          dialog.cancel();
          if (backToDiscovery) {

            Intent intent = new Intent();
            if (Constants.enableDiscovery) {
              intent.setClass(((Dialog) dialog).getContext(), DiscoveryActivity.class);
            } else {
              intent.setClass(((Dialog) dialog).getContext(), ManualDiscoveryActivity.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ((Dialog) dialog).getContext().startActivity(intent);

            if (screenName == "grid") {
              ((GridviewActivity) context).finish();
            } else if (screenName == "home") {
              ((HomeActivity) context).finish();
            } else if (screenName == "allmedia") {
              ((AllMediaActivity) context).finish();
            }

            // ((HomeActivity) context).finish();
          }
          alertDlg = null;
        }
      });
      // AlertDialog alertDlg = errBuilder.create();
      alertDlg = errBuilder.create();
      alertDlg.setOnKeyListener(new Dialog.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_BACK) {
            dialog.cancel();
            if (backToDiscovery) {
              Intent intent = new Intent();
              if (Constants.enableDiscovery) {
                intent.setClass(((Dialog) dialog).getContext(), DiscoveryActivity.class);
              } else {
                intent.setClass(((Dialog) dialog).getContext(), ManualDiscoveryActivity.class);
              }
              intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              ((Dialog) dialog).getContext().startActivity(intent);
              if (screenName == "grid") {
                ((GridviewActivity) context).finish();
              } else if (screenName == "home") {
                ((HomeActivity) context).finish();
              } else if (screenName == "allmedia") {
                ((AllMediaActivity) context).finish();
              }

            }
            alertDlg = null;
            return true;
          }
          return false;
        }
      });
      // if (isAppInForeground(context))
      alertDlg.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Check if Wifi connection is available.
   * 
   * @param context
   */
  public static Boolean checkForWiFi(Context context) {
    ConnectivityManager connManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo mainWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    return mainWifi.isConnected();
  }

  /**
   * Called when activity is stopped.
   */
  protected void onStop() {
    super.onStop();

  }

}
