package com.cmst.cmstapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cmst.cache.util.AlbumDetails;
import com.cmst.cache.util.AlbumList;
import com.cmst.cache.util.Cache;
import com.cmst.common.AlarmService;
import com.cmst.common.AlbumData;
import com.cmst.common.Constants;

/**
 * This is a top level fragment, showing a list of items that the user can pick. Upon picking an
 * item, it takes care of displaying the data to the user as appropriate, based on the current
 * screen size and orientation.
 * 
 * @Filename: AlbumFragment.java
 * @author: Demo
 * @version: 0.1
 */

public class AlbumFragment extends ListFragment {
  public static final int TASK_KEY = -563;
  private Dialog metaDialog;
  private Dialog shadowDialog;
  private Dialog filterDialog;
  private EditText edTitle;
  private CustomEditText edDesc;
  private ImageView metaProgress;
  private ImageView actionLeft;
  private String apiType = "getMeta";

  private AlbumMetaData taskMetaData;
  private GetAlbumData taskAlbumData;

  private LinearLayout albumLoadingCont;
  private AlbumListAdapter custom;
  private int listPosition = 0;
  AlbumData albumData;
  /**
   * Activity context which contains this fragment.
   */
  private Context mainContext;

  private int curCheckPosition = 0;

  /**
   * List of all the albums.
   */
  // private ArrayList<AlbumDetails> albumData = new ArrayList<AlbumDetails>();

  public static ListView albumContext;

  private TextView actionTitle;

  private DatePicker datePicker;

  private int day;
  private int month;
  private int year;

  private int index = -1;
  public static int top = 0;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Constants.isFilter = false;
    albumLoadingCont = (LinearLayout) getActivity().findViewById(R.id.albumLoadingCont);
    setUpActionBarIcon();
    initMetaPopup();
    initFilterPopup();
    int orientation = getActivity().getRequestedOrientation();
    getActivity().setRequestedOrientation(orientation);

    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public void onResume() {
    if (albumData != null && albumData.albumList != null) {
      refreshList();
      getListView().setSelector(R.drawable.album_selector);
    }

    // if(index!=-1){
    // this.getListView().setSelectionFromTop(index, top);
    // }

    if (getListView().getSelector() != null) {
      getListView().setItemChecked(GridviewFragment.albumPosition, true);
      getListView().setSelectionFromTop(GridviewFragment.albumPosition, top);
    }

    super.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    try {
      index = this.getListView().getFirstVisiblePosition();
      View v = this.getListView().getChildAt(0);
      top = (v == null) ? 0 : v.getTop();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   * Initializes meta data popup.
   */
  public void initMetaPopup() {
    try {
      RotateAnimation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
          Animation.RELATIVE_TO_SELF, 0.5f);
      anim.setInterpolator(new LinearInterpolator());
      anim.setRepeatCount(Animation.INFINITE);
      anim.setDuration(1500);
      metaDialog = new Dialog(getActivity(), android.R.style.Theme_NoTitleBar_OverlayActionModes);
      metaDialog.setCanceledOnTouchOutside(true);
      shadowDialog = new Dialog(getActivity(), android.R.style.Theme_NoTitleBar_OverlayActionModes);
      shadowDialog.setContentView(R.layout.shadow);
      shadowDialog.getWindow().setBackgroundDrawable(
          new ColorDrawable(android.graphics.Color.TRANSPARENT));

      metaDialog.setContentView(R.layout.popup_meta);
      metaDialog.getWindow().setBackgroundDrawable(
          new ColorDrawable(android.graphics.Color.TRANSPARENT));

      WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
      lp.copyFrom(metaDialog.getWindow().getAttributes());
      lp.width = 970;
      lp.height = 1300;
      metaDialog.getWindow().setAttributes(lp);

      metaProgress = (ImageView) metaDialog.findViewById(R.id.metaProgress);
      metaProgress.startAnimation(anim);
      edTitle = (EditText) metaDialog.findViewById(R.id.metaTitle);
      edDesc = (CustomEditText) metaDialog.findViewById(R.id.metaDesc);
      ImageView saveBtn = (ImageView) metaDialog.findViewById(R.id.btnSave);

      // if button is clicked, close the custom dialog
      saveBtn.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          if (!edTitle.getText().toString().equalsIgnoreCase("")) {
            apiType = "setMeta";
            taskMetaData = new AlbumMetaData();
            taskMetaData.execute();

          } else {

            Toast.makeText(getActivity(), getString(R.string.empty_album), Toast.LENGTH_SHORT)
                .show();
          }
        }
      });

      metaDialog.setOnKeyListener(new Dialog.OnKeyListener() {

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Cancel the API request if return key is pressed
            if (taskMetaData != null) {
              taskMetaData.cancel(true);
            }
            shadowDialog.dismiss();
            metaDialog.dismiss();
            return true;
          }
          return false;
        }
      });
      metaDialog.setOnCancelListener(new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          if (taskMetaData != null) {
            taskMetaData.cancel(true);
          }
          shadowDialog.dismiss();
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
    } catch (Exception e) {
      e.printStackTrace();
      Log.e("AlbumFragment", " error in creating view for meta popup");
    }
    setFilter();
  }

  void initFilterPopup() {
    try {
      filterDialog = new Dialog(getActivity(), android.R.style.Theme_NoTitleBar_OverlayActionModes);
      filterDialog.setCanceledOnTouchOutside(true);
      filterDialog.setOnCancelListener(new OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
          shadowDialog.dismiss();
        }
      });
      filterDialog.setContentView(R.layout.popup_filter);
      filterDialog.getWindow().setBackgroundDrawable(
          new ColorDrawable(android.graphics.Color.TRANSPARENT));

      WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
      lp.copyFrom(filterDialog.getWindow().getAttributes());
      lp.width = 970;
      lp.height = 1000;

      // lp.height = 1300;
      filterDialog.getWindow().setAttributes(lp);

      TextView btnFilterOk = (TextView) filterDialog.findViewById(R.id.btnFilterOk);
      datePicker = (DatePicker) filterDialog.findViewById(R.id.dateFilter);

      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
      String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
      Log.e("d1.getDate()=", "" + date.replace("-", "/"));

      Date d = sdf.parse(date.replace("-", "/"));

      datePicker.setMaxDate(d.getTime());
      btnFilterOk.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          Constants.isFilter = true;
          day = datePicker.getDayOfMonth();
          month = datePicker.getMonth() + 1;
          year = datePicker.getYear();

          taskAlbumData = new GetAlbumData();
          taskAlbumData.execute("");
          shadowDialog.dismiss();
          filterDialog.dismiss();
          LinearLayout albumLoadingCont = (LinearLayout) getActivity().findViewById(
              R.id.albumLoadingCont);
          albumLoadingCont.setVisibility(View.VISIBLE);
        }
      });

      filterDialog.setOnKeyListener(new Dialog.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_BACK) {
            shadowDialog.dismiss();
            filterDialog.dismiss();
            return true;
          }
          return false;
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Make metaData popup visible.
   */
  public void showMetaPopup() {

    edTitle.setText("");
    edDesc.setText("");
    TextView metaDescription = (TextView) metaDialog.findViewById(R.id.metaDescription);
    metaDescription.setText("\""
        + albumData.albumList.get(GridviewFragment.albumPosition).getAlbmName() + "\"");
    shadowDialog.show();
    metaDialog.show();
    apiType = "getMeta";
    new AlbumMetaData().execute();

  }

  public void showFilterPopup() {
    shadowDialog.show();
    filterDialog.show();
  }

  /**
   * Customize the action title bar.
   */
  void setUpActionBarIcon() {
    getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getActivity().getActionBar().setCustomView(R.layout.header_main);
    actionLeft = (ImageView) getActivity().findViewById(R.id.action_left);
    actionLeft.setImageDrawable(getResources().getDrawable(R.drawable.active_search));

    actionLeft.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AlarmService.getInstance(mainContext).stop();
        new DoLogout().execute();
      }
    });

    ImageView actionFilter = (ImageView) getActivity().findViewById(R.id.action_filter);
    actionFilter.setVisibility(View.VISIBLE);
    actionFilter.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          showFilterPopup();
        } catch (Exception e) {
          e.printStackTrace();
          Log.e("Albums", "Exception");
        }
      }
    });
    ImageView actionRight = (ImageView) getActivity().findViewById(R.id.action_right);
    if (!Constants.isTablet) {
      actionRight.setVisibility(View.INVISIBLE);

    } else {
      actionRight.setVisibility(View.VISIBLE);
      actionRight.setImageDrawable(getResources().getDrawable(R.drawable.active_meta));
      actionRight.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (DiscoveryActivity.checkForWiFi(getActivity())) {
            showMetaPopup();
          } else {
            DiscoveryActivity.errorDialog(getActivity(), getString(R.string.err),
                getString(R.string.enable_wifi), false, "");
          }
        }
      });
    }

    actionTitle = (TextView) getActivity().findViewById(R.id.action_title);
    actionTitle.setText(Constants.cmstName);
  }

  /**
   * Called when activity is created.
   */
  @SuppressLint("ResourceAsColor")
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setRetainInstance(true);

    if (Constants.isTablet) {
      getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    taskAlbumData = new GetAlbumData();
    taskAlbumData.execute("");

    getListView().setDivider(getResources().getDrawable(R.drawable.discovery_separator));
    setListShown(true);
    getListView().setVerticalScrollBarEnabled(false);
    getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    getListView().setSelector(R.drawable.album_selector);
    albumContext = this.getListView();
  }

  /**
   * Called when activity is finished.
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("curChoice", curCheckPosition);
  }

  /**
   * Click event on selected album to view the items.
   * 
   * @param listView
   * @param position
   */
  @Override
  public void onListItemClick(ListView listView, View view, int position, long id) {
    try {
      listPosition = position;
      Constants.albumId = String.valueOf(albumData.albumList.get(position).getAlbmId());
      Constants.albumDir = albumData.albumList.get(position).getDir();
      if (Constants.isTablet) {
        actionTitle.setText(albumData.albumList.get(position).getAlbmName());
      }
      // getListView().setItemChecked(position, true);
      showDetails(position, listView);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Helper function to show the details of a selected item, either by displaying a fragment
   * in-place in the current UI, or starting a whole new activity in which it is displayed.
   * 
   * @param albumPosition
   * 
   */
  void showDetails(int index, ListView album) {
    try {
      // Bundle bundleObject = new Bundle();
      // bundleObject.putSerializable("albumList", albumData);
      if (Constants.isTablet) {
        if (index == -1) {
          if (getActivity().getSupportFragmentManager().findFragmentById(R.id.details) != null)
            getActivity().getSupportFragmentManager().beginTransaction()
                .remove(getActivity().getSupportFragmentManager().findFragmentById(R.id.details))
                .commit();

          return;
        }
        // We can display everything in-place with fragments, so update
        // the list to highlight the selected item and show the data.

        // getListView().setItemChecked(index, true);

        // Check what fragment is currently shown, replace if needed.
        GridviewFragment details = (GridviewFragment) getFragmentManager().findFragmentById(
            R.id.details);

        if (details == null || details.getShownIndex() != index || Constants.isFilter) {
          // Make new fragment to show this selection.

          details = GridviewFragment.newInstance(index, getActivity(), R.layout.gridview);
          // details.setArguments(bundleObject);

          // Execute a transaction, replacing any existing fragment
          // with this one inside the frame.
          FragmentTransaction ft = getFragmentManager().beginTransaction();
          ft.replace(R.id.details, details);
          ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
          ft.commit();
        }
      } else {
        // Otherwise we need to launch a new activity to display
        // the dialog fragment with selected text.
        Intent intent = new Intent();
        intent.setClass(getActivity(), GridviewActivity.class);
        intent.putExtra("index", index);
        intent.putExtra("albumName", albumData.albumList.get(index).getAlbmName());

        // intent.putExtras(bundleObject);
        startActivity(intent);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private class GetAlbumData extends AsyncTask<String, Void, AlbumList> {

    @Override
    protected AlbumList doInBackground(String... params) {
      Log.e("albumfragment", "..................1");
      if (isCancelled()) {
        return null;
      }
      Log.e("albumfragment", "..................2");
      return Cache.getInstance().getListOfAlbums(Constants.cmstName,
          getActivity().getApplicationContext());

    }

    /**
     * If success then call album list from CMST device else show error dialog.
     * 
     * @param: Result
     */
    @Override
    protected void onPostExecute(AlbumList albumList) {

      LinearLayout albumLoadingCont = (LinearLayout) getActivity().findViewById(
          R.id.albumLoadingCont);
      albumLoadingCont.setVisibility(View.GONE);

      if (isCancelled()) {
        return;
      }

      if (albumList == null) {

        DiscoveryActivity.errorDialog(getActivity(), getResources().getString(R.string.info),
            getString(R.string.no_albums), true, "home");

        return;
      }

      if (albumList.getAlbmThmbList().size() == 0) {
        // show error message
      } else {

        albumData = AlbumData.getInstance();

        if (Constants.isFilter) {
          ArrayList<AlbumDetails> arrLst = new ArrayList<AlbumDetails>();
          for (int i = 0; i < albumList.getAlbmThmbList().size(); i++) {
            if (isFilteredData(albumList.getAlbmThmbList().get(i).getAlbmDate())) {
              Log.e("FilterData", "Filter applied");
              arrLst.add(albumList.getAlbmThmbList().get(i));
            }
          }
          custom = new AlbumListAdapter(getActivity().getApplicationContext(), arrLst);

          albumData.albumList = arrLst;
          if (albumData.albumList.size() == 0) {
            DiscoveryActivity.errorDialog(getActivity(), getString(R.string.err),
                getString(R.string.noAlbum_date), false, "");
          }
        } else {
          custom = new AlbumListAdapter(getActivity().getApplicationContext(),
              albumList.getAlbmThmbList());
          // albumData = albumList.getAlbmThmbList();
          albumData.albumList = albumList.getAlbmThmbList();
        }

        setListAdapter(custom);

        if (Constants.isTablet) {
          try {
            if (albumData.albumList.size() != 0) {
              Constants.albumId = String.valueOf(albumData.albumList.get(0).getAlbmId()); // String.valueOf(albumList.getAlbmThmbList().get(0).getAlbmId());
              Constants.albumDir = albumData.albumList.get(0).getDir(); // albumList.getAlbmThmbList().get(0).getDir();
              actionTitle.setText(albumList.getAlbmThmbList().get(0).getAlbmName());
              getListView().post(new Runnable() {
                @Override
                public void run() {
                  getListView().setSelection(0);
                  getListView().setItemChecked(0, true);
                }
              });
              showDetails(0, null);
            } else {
              showDetails(-1, null);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

      }
    }
  }

  private Calendar setCalendar(int thisDay, int thisMonth, int thisYear) {
    Calendar myCalendar = Calendar.getInstance();
    myCalendar.set(Calendar.DAY_OF_MONTH, thisDay);
    myCalendar.set(Calendar.MONTH, thisMonth - 1);
    myCalendar.set(Calendar.YEAR, thisYear);
    return myCalendar;
  }

  private Boolean isFilteredData(String albumDate) {
    Calendar prevCalendar = setCalendar(day, month, year);
    prevCalendar.add(Calendar.DATE, Constants.prevOffset);

    Calendar nextCalendar = setCalendar(day, month, year);
    nextCalendar.add(Calendar.DATE, Constants.nextOffset);
    Log.e("FilterData", albumDate);

    Calendar curCalendar = setCalendar(Integer.parseInt(albumDate.substring(8, 10)),
        Integer.parseInt(albumDate.substring(5, 7)), Integer.parseInt(albumDate.substring(0, 4)));

    if (curCalendar.getTimeInMillis() >= prevCalendar.getTimeInMillis()
        && curCalendar.getTimeInMillis() <= nextCalendar.getTimeInMillis()) {
      return true;
    }
    return false;
  }

  /**
   * Class to fetch a particular album's meta data.
   * 
   */
  private class AlbumMetaData extends AsyncTask<String, Void, String> {

    protected void onPreExecute() {
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
          strMetaUrl = strMetaUrl.append(Constants.ipAddress).append(Constants.apiGetMeta)
              .append(Constants.sessionId).append("&albmId=" + Constants.albumId);
        } else {
          strMetaUrl = strMetaUrl.append(Constants.ipAddress).append(Constants.apiSetMeta)
              .append(Constants.sessionId).append("&albmId=" + Constants.albumId)
              .append("&albmName=" + String.valueOf(edTitle.getText()).replace(" ", "%20"))
              .append("&albmCmnt=" + String.valueOf(edDesc.getText()).replace(" ", "%20"));
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

                Cache.getInstance().changeAlbumName(String.valueOf(edTitle.getText()),
                    Constants.cmstName, Integer.parseInt(Constants.albumId),
                    getActivity().getApplicationContext());
                // custom.updateView(listPosition, edTitle.getText())
                // custom.getListView().invalidateViews();

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
        DiscoveryActivity.errorDialog(getActivity(), getString(R.string.err),
            Constants.getErrorMsg(getActivity().getBaseContext(), 0, "network"), false, "");
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
            Toast.makeText(getActivity(), getString(R.string.meta_saved), Toast.LENGTH_LONG).show();

            if (Constants.isTablet) {
              actionTitle.setText(String.valueOf(edTitle.getText()));
            }
            albumData.albumList.get(GridviewFragment.albumPosition).setAlbmName(
                String.valueOf(edTitle.getText()));
            refreshList();
          }
        } else {
          if (jsonObj.getInt("res") == 2001 || jsonObj.getInt("res") == 2002
              || jsonObj.getInt("res") == 2003 || jsonObj.getInt("res") == 2004) {

            SharedPreferences sh_Pref = getActivity().getSharedPreferences("IPAddress",
                mainContext.MODE_PRIVATE);
            Editor toEdit = sh_Pref.edit();
            toEdit.clear();
            toEdit.commit();

            Constants.sessionId = "";
            Constants.cmstName = "";

            DiscoveryActivity.errorDialog(getActivity(), getString(R.string.err),
                Constants.getErrorMsg(getActivity().getBaseContext(), jsonObj.getInt("res"), ""),
                true, "home");
          } else {
            DiscoveryActivity.errorDialog(getActivity(), getString(R.string.err),
                Constants.getErrorMsg(getActivity().getBaseContext(), jsonObj.getInt("res"), ""),
                false, "");
          }
        }

      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  public void onDestroy() {
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
    if (filterDialog != null) {
      if (filterDialog.isShowing())
        filterDialog.dismiss();
      filterDialog = null;
    }

    if (taskMetaData != null) {
      taskMetaData.cancel(true);
    }
    if (taskAlbumData != null) {
      taskAlbumData.cancel(true);
    }

  }

  public void onStop() {
    super.onStop();
  }

  /**
   * Function to validate the album name.
   * 
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
              Toast.makeText(getActivity(),
                  getResources().getString(R.string.no_special_characters), Toast.LENGTH_LONG)
                  .show();
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

      SharedPreferences sh_Pref = getActivity().getSharedPreferences("IPAddress",
          mainContext.MODE_PRIVATE);
      Editor toEdit = sh_Pref.edit();
      toEdit.clear();
      toEdit.commit();
      Constants.sessionId = "";
      Constants.cmstName = "";

      Intent intent = new Intent();
      if (Constants.enableDiscovery) {
        intent.setClass(getActivity(), DiscoveryActivity.class);
      } else {
        intent.setClass(getActivity(), ManualDiscoveryActivity.class);
      }

      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      getActivity().finish();

    }
  }

  public void refreshList() {
    Log.e(Constants.TAG, "..................1");
    custom = new AlbumListAdapter(getActivity().getApplicationContext(), albumData.albumList);
    for (int i = 0; i < albumData.albumList.size(); i++) {
      Log.e(Constants.TAG, i + " : " + albumData.albumList.get(i).getAlbmName());
    }
    Log.e(Constants.TAG, "..................2");
    setListAdapter(custom);
    Log.e(Constants.TAG, "..................3");
    custom.notifyDataSetChanged();

    getListView().setSelector(R.drawable.album_selector);

    if (getListView().getSelector() != null) {
      getListView().setItemChecked(GridviewFragment.albumPosition, true);
      getListView().setSelection(GridviewFragment.albumPosition);
    }
  }

} // End class AlbumFragment
