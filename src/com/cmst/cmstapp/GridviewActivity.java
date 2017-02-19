package com.cmst.cmstapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cmst.cache.util.AlbumDetails;
import com.cmst.cache.util.Cache;
import com.cmst.common.AlbumData;
import com.cmst.common.Constants;

/**
 * This activity displays the details using a DetailsFragment. This activity is started by a
 * TitlesFragment when a title in the list is selected. The activity is used only if a
 * DetailsFragment is not on the screen.
 */

public class GridviewActivity extends FragmentActivity {

  private ImageView metaProgress;

  private String apiType = "getMeta";
  Dialog metaDialog;
  Dialog shadowDialog;
  EditText edTitle;
  CustomEditText edDesc;
  TextView actionTitle;
  TextView metaDescription;
  String myAlbumName = "";
  private RotateAnimation anim;
  private AlbumMetaData taskMetaData;
  // private ArrayList<AlbumDetails> albumData;
  AlbumData albumData;

  /**
   * Called on fragment creation.
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      // Bundle bundleObject = getIntent().getExtras();
      // Get ArrayList Bundle
      // albumData = (ArrayList<AlbumDetails>) bundleObject.getSerializable("albumList");
      albumData = AlbumData.getInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (savedInstanceState == null) {
      GridviewFragment details = new GridviewFragment();
      details.setArguments(getIntent().getExtras());
      getSupportFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
    }
    /**
     * Change orientation to landscape for tablet.
     */
    if (Constants.isTablet) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    Intent intent = getIntent();
    GridviewFragment.mcontext = GridviewActivity.this;
    GridviewFragment.albumPosition = intent.getExtras().getInt("index");
    setUpActionBarIcon(intent.getExtras().getString("albumName"));
    initMetaPopup(intent.getExtras().getString("albumName"));
  }

  /**
   * Customize the action title bar.
   */
  void setUpActionBarIcon(String albumName) {
    getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getActionBar().setCustomView(R.layout.header);

    ImageView actionLeft = (ImageView) findViewById(R.id.action_left);
    actionLeft.setImageDrawable(getResources().getDrawable(R.drawable.active_back));

    actionLeft.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        GridviewActivity.this.onBackPressed();
      }
    });

    ImageView actionRight = (ImageView) findViewById(R.id.action_right);
    actionRight.setImageDrawable(getResources().getDrawable(R.drawable.active_meta));
    actionRight.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (DiscoveryActivity.checkForWiFi(GridviewActivity.this)) {
          showMetaPopup();
        } else {
          DiscoveryActivity.errorDialog(GridviewActivity.this, getString(R.string.err),
              Constants.getErrorMsg(getBaseContext(), 0, "wifi"), false, "");
        }
      }
    });

    actionTitle = (TextView) findViewById(R.id.action_title);
    actionTitle.setText(albumName);
  }

  void setActionTitle(String albumName) {
    myAlbumName = albumName;
    actionTitle.setText(albumName);
  }

  /**
   * Called when activity is Paused.
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
    releaseMemory();
  }

  /**
   * Called when activity is destroyed.
   */
  protected void onResume() {
    super.onResume();
  }

  /**
   * Called when return key is pressed.
   */
  @Override
  public void onBackPressed() {
    super.onBackPressed();
    releaseMemory();
  }

  /**
   * Method to initialize the meta data popup.
   */
  public void initMetaPopup(String albumName) {
    anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    anim.setInterpolator(new LinearInterpolator());
    anim.setRepeatCount(Animation.INFINITE);
    anim.setDuration(1500);

    metaDialog = new Dialog(GridviewActivity.this,
        android.R.style.Theme_NoTitleBar_OverlayActionModes);
    metaDialog.setCanceledOnTouchOutside(true);
    shadowDialog = new Dialog(GridviewActivity.this,
        android.R.style.Theme_NoTitleBar_OverlayActionModes);
    shadowDialog.setContentView(R.layout.shadow);
    shadowDialog.getWindow().setBackgroundDrawable(
        new ColorDrawable(android.graphics.Color.TRANSPARENT));

    metaDialog.setContentView(R.layout.popup_meta);
    metaDialog.getWindow().setBackgroundDrawable(
        new ColorDrawable(android.graphics.Color.TRANSPARENT));

    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    lp.copyFrom(metaDialog.getWindow().getAttributes());
    lp.width = 970;
    lp.dimAmount = 0.0f;
    lp.height = 1300;
    metaDialog.getWindow().setAttributes(lp);
    metaProgress = (ImageView) metaDialog.findViewById(R.id.metaProgress);
    metaProgress.startAnimation(anim);
    edTitle = (EditText) metaDialog.findViewById(R.id.metaTitle);

    edDesc = (CustomEditText) metaDialog.findViewById(R.id.metaDesc);

    metaDescription = (TextView) metaDialog.findViewById(R.id.metaDescription);
    metaDescription.setText("\"" + albumName + "\"");
    myAlbumName = albumName;
    ImageView saveBtn = (ImageView) metaDialog.findViewById(R.id.btnSave);
    metaDialog.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        if (taskMetaData != null) {
          taskMetaData.cancel(true);
        }
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
          metaDialog.dismiss();
          shadowDialog.dismiss();
          return true;
        }
        return false;
      }
    });
    // if button is clicked, close the custom dialog
    saveBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!edTitle.getText().toString().equalsIgnoreCase("")) {
          apiType = "setMeta";
          taskMetaData = new AlbumMetaData();
          taskMetaData.execute();
        } else {
          Toast.makeText(getBaseContext(), getString(R.string.empty_album), Toast.LENGTH_SHORT)
              .show();
        }
      }
    });

    ImageView cancelBtn = (ImageView) metaDialog.findViewById(R.id.btnCancel);
    // if button is clicked, close the custom dialog
    cancelBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (metaProgress.getVisibility() != View.VISIBLE) {
          metaDialog.dismiss();
          shadowDialog.dismiss();
        }
      }
    });
    setFilter();
  }

  /**
   * Method to show the meta data popup.
   */
  public void showMetaPopup() {
    try {
      metaDescription.setText("\"" + myAlbumName + "\"");
      edTitle.setText("");
      edDesc.setText("");
      shadowDialog.show();
      metaDialog.show();
      apiType = "getMeta";
      new AlbumMetaData().execute();
    } catch (Exception e) {
      Log.e(Constants.TAG, "show Meta Data : " + e.getMessage());
    }
  }

  /**
   * Class to fetch the meta data of a particular album.
   */
  private class AlbumMetaData extends AsyncTask<String, Void, String> {

    protected void onPreExecute() {
      metaProgress.startAnimation(anim);
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
        if (apiType.equalsIgnoreCase("getMeta")) {
          strMetaUrl.append(Constants.ipAddress).append(Constants.apiGetMeta)
              .append(Constants.sessionId).append("&albmId=").append(Constants.albumId);
        } else {
          strMetaUrl.append((Constants.ipAddress + Constants.apiSetMeta + Constants.sessionId
              + "&albmId=" + Constants.albumId + "&albmName="
              + String.valueOf(edTitle.getText()).replace(" ", "%20") + "&albmCmnt=" + String
              .valueOf(edDesc.getText()).replace(" ", "%20")));
        }

        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(strMetaUrl.toString());
        Log.e(Constants.TAG, apiType + " : " + strMetaUrl);

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

            JSONObject jsonObj = null;
            try {
              jsonObj = new JSONObject(data.toString());
              if (Constants.getServerStatus(jsonObj.getInt("res"))
                  && apiType.equalsIgnoreCase("setMeta")) {
                Log.e("Album Fragment", "...........Cache updated..........");
                Cache.getInstance().changeAlbumName(String.valueOf(edTitle.getText()),
                    Constants.cmstName, Integer.parseInt(Constants.albumId),
                    getApplicationContext());
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
            jsonObj = null;

            Log.e("CMST RESPONSE : ", "" + data);
            return data.toString();
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
      if (apiType.equalsIgnoreCase("setMeta")) {
        metaDialog.dismiss();
        shadowDialog.dismiss();
      }
      if (isCancelled() || result == "") {
        return;
      }

      if (result == null) {
        DiscoveryActivity.errorDialog(GridviewActivity.this, getString(R.string.err),
            Constants.getErrorMsg(getBaseContext(), 0, "network"), false, "");
        return;
      }

      JSONObject jsonObj = null;

      try {
        jsonObj = new JSONObject(result);

        if (Constants.getServerStatus(jsonObj.getInt("res"))) {
          if (apiType.equalsIgnoreCase("getMeta")) {
            edTitle.setText(jsonObj.getString("albmName"));
            edTitle.setSelection(edTitle.getText().length());
            edDesc.setText(jsonObj.getString("albmCmnt"));
            edDesc.setSelection(edDesc.getText().length());
          } else {
            Toast
                .makeText(GridviewActivity.this, getString(R.string.meta_saved), Toast.LENGTH_LONG)
                .show();

            setActionTitle(String.valueOf(edTitle.getText()));

            albumData.albumList.get(GridviewFragment.albumPosition).setAlbmName(
                String.valueOf(edTitle.getText()));

          }
        } else {

          if (jsonObj.getInt("res") == 2001 || jsonObj.getInt("res") == 2002
              || jsonObj.getInt("res") == 2003 || jsonObj.getInt("res") == 2004) {
            SharedPreferences sh_Pref = getSharedPreferences("IPAddress", MODE_PRIVATE);
            Editor toEdit = sh_Pref.edit();
            toEdit.clear();
            toEdit.commit();

            Constants.sessionId = "";
            Constants.cmstName = "";

            DiscoveryActivity.errorDialog(GridviewActivity.this, getString(R.string.err), Constants
                .getErrorMsg(GridviewActivity.this.getBaseContext(), jsonObj.getInt("res"), ""),
                true, "grid");
          } else {
            DiscoveryActivity.errorDialog(GridviewActivity.this, getString(R.string.err), Constants
                .getErrorMsg(GridviewActivity.this.getBaseContext(), jsonObj.getInt("res"), ""),
                false, "");
          }

        }

      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  private void releaseMemory() {
    if (taskMetaData != null) {
      taskMetaData.cancel(true);
    }
  }

  /**
   * Method to convert pixel to dp.
   */
  public int pxToDp(int px) {
    DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
    int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    return dp;
  }

  /**
   * Method to validate album name.
   */
  private void setFilter() {
    InputFilter[] titleFilters = new InputFilter[2];
    titleFilters[0] = new InputFilter() {
      @Override
      public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
          int dend) {
        if (end > start) {

          char[] acceptedChars = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
              'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A',
              'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
              'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8',
              '9', '_', '-', ' ', '/', ':' };

          for (int index = start; index < end; index++) {
            if (!new String(acceptedChars).contains(String.valueOf(source.charAt(index)))) {
              Toast.makeText(GridviewActivity.this, getString(R.string.no_special_characters),
                  Toast.LENGTH_SHORT).show();
              return "";
            }
            if (metaProgress.getVisibility() == View.VISIBLE) {

              return "";
            }
          }
        }
        return null;
      }
    };
    titleFilters[1] = new InputFilter.LengthFilter(15);
    edTitle.setFilters(titleFilters);
    InputFilter[] descFilters = new InputFilter[2];
    descFilters[0] = titleFilters[0];
    descFilters[1] = new InputFilter.LengthFilter(40);
    edDesc.setFilters(descFilters);
  }

}
