package com.cmst.cmstapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmst.cache.util.AlbumItemDetails;
import com.cmst.cmstapp.GestureDetection.SimpleGestureListener;
import com.cmst.cmstapp.ImageDownloader.ImageLoaderListener;
import com.cmst.common.AllMediaData;
import com.cmst.common.Constants;

public class SlideshowActivity extends Activity implements SimpleGestureListener, OnTouchListener {

  Context mContext;
  private Matrix matrix = new Matrix();
  private float scale = 1f;
  ImageView videoText;
  private int itemPosition = 0;
  private ImageView btnStart, ivDownload, ivRight, ivLeft;
  private Handler mHandler;
  public final int DELAY = 3000;
  Runnable mRunnable;

  CustomViewPager slidePager;
  // ViewPager slidePager;

  Timer swipeTimer;
  private TextView tvTitle;
  private Boolean bSlideShowOn = false;
  ImageDownloader mDownloader;
  Bitmap bitmapImg;
  Dialog infoDialog, shadowDialog;
  LinearLayout normalMode, tvMode;
  private ImageView imgTvMode;
  private String previousScreen;
  LinearLayout shadow;
  private Handler handler;
  private Runnable fadeRunnable;
  private Runnable headerrunnable;
  private int curIndex = 0;
  LinearLayout header;
  Boolean slideShowOnCMST = false;
  // private AlbumItems allMediaItemList = new AlbumItems();
  private boolean imageDownloadProgress = false;
  // private ArrayList<ItemThmbList> allMediaList = new ArrayList<ItemThmbList>();

  private ArrayList<AlbumItemDetails> allMediaFullList;
  private AllMediaData mediaList;

  private Boolean onTVMode = false;

  /**
   * Used for recognizing gestures.
   */
  private GestureDetection gestureDetection;

  private GetItemDetails taskItemDetails;

  /**
   * Called when slideshow activity is created
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
      requestWindowFeature(Window.FEATURE_PROGRESS);
      getWindow().getDecorView().setSystemUiVisibility(
          View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      setContentView(R.layout.slideshow);

      setProgressBarIndeterminate(true);
      setProgressBarVisibility(false);
      handler = new Handler();
      initialize();
      setUpActionBarIcon();
      loadAllDetails();
      loadViewPager();
      addListeners();
    } catch (Exception e) {
      e.printStackTrace();
    }
    // gridGallery.startAnimation(slide);
  }

  private void hideHeader() {
    headerrunnable = new Runnable() {
      @Override
      public void run() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);
      }
    };
    handler.postDelayed(headerrunnable, 2000);
  }

  @SuppressWarnings("unchecked")
  private void initialize() {
    try {
      mContext = this.getApplicationContext();
      Intent slideshowIntent = getIntent();
      previousScreen = slideshowIntent.getExtras().getString("previousScreen");
      videoText = (ImageView) findViewById(R.id.videoHint);

      slidePager = (CustomViewPager) findViewById(R.id.slidePager);
      // slidePager = (ViewPager) findViewById(R.id.slidePager);

      imgTvMode = (ImageView) findViewById(R.id.tvmode_1);
      normalMode = (LinearLayout) findViewById(R.id.show_normal_mode);

      btnStart = (ImageView) findViewById(R.id.btnSlideStart);

      tvMode = (LinearLayout) findViewById(R.id.show_tv_mode);

      mediaList = AllMediaData.getInstance();
      allMediaFullList = mediaList.allMediaList;

      if (previousScreen.equalsIgnoreCase("GridView")) {
        if (allMediaFullList.size() > 1) {
          fadeOutAnim();
        } else {
          hideHeader();
        }
      } else {
        hideHeader();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Loading images to view pager
   */
  public void loadViewPager() {
    try {

      if (previousScreen.equalsIgnoreCase("GridView")) {
        if (allMediaFullList.size() == 1) {
          btnStart.setVisibility(View.GONE);
        }
        SlideshowAdapter adapter = new SlideshowAdapter(SlideshowActivity.this);
        slidePager.setAdapter(adapter);
        itemPosition = getIntent().getExtras().getInt("position");
        slidePager.setCurrentItem(itemPosition);
        if (allMediaFullList.get(itemPosition).getFileType() != null
            && allMediaFullList.get(itemPosition).getFileType().equalsIgnoreCase("video/quicktime")) {

          videoText.setVisibility(View.VISIBLE);
        } else {
          if (allMediaFullList.get(itemPosition).getFileType() != null) {
            ivDownload.setVisibility(View.VISIBLE);
          }
          videoText.setVisibility(View.INVISIBLE);
        }
        tvTitle.setText(allMediaFullList.get(slidePager.getCurrentItem()).getFileName());
      } else {

        btnStart.setVisibility(View.GONE);

        SlideshowAdapter adapter = new SlideshowAdapter(SlideshowActivity.this);

        slidePager.setAdapter(adapter);
        itemPosition = getIntent().getExtras().getInt("position");
        slidePager.setCurrentItem(itemPosition);
        videoText.setVisibility(View.INVISIBLE);
        ivDownload.setVisibility(View.INVISIBLE);
        // if (allMediaFullList.get(itemPosition).getFileType() != null
        // && allMediaFullList.get(itemPosition).getFileType().equalsIgnoreCase("video/quicktime"))
        // {
        // // videoText.setVisibility(View.VISIBLE);
        // videoText.setVisibility(View.INVISIBLE);
        // } else {
        // ivDownload.setVisibility(View.INVISIBLE);
        // videoText.setVisibility(View.INVISIBLE);
        // }
        tvTitle.setText(allMediaFullList.get(slidePager.getCurrentItem()).getFileName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void startSlideshow() {
    if (fadeRunnable != null) {

      handler.removeCallbacks(fadeRunnable);
      btnStart.clearAnimation();

    }
    header = (LinearLayout) findViewById(R.id.slide_header);
    if (!bSlideShowOn) {
      // getWindow().getDecorView().setSystemUiVisibility(
      // View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
      // | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
      // | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);
      header.setVisibility(View.GONE);
      bSlideShowOn = true;
      itemPosition = slidePager.getCurrentItem();
      btnStart.setImageDrawable(getResources().getDrawable(R.drawable.active_slideshow_exit));
      mHandler = new Handler();
      mRunnable = new Runnable() {
        public void run() {
          if (itemPosition == allMediaFullList.size()) {
            itemPosition = 0;
          }
          slidePager.setCurrentItem(itemPosition++, true);
        }
      };

      swipeTimer = new Timer();
      swipeTimer.schedule(new TimerTask() {

        @Override
        public void run() {
          mHandler.post(mRunnable);
        }
      }, 100, DELAY);

    } else {
      // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
      header.setVisibility(View.VISIBLE);
      bSlideShowOn = false;
      stopSlideShow();
    }
    fadeOutAnim();

  }

  public void addListeners() {
    if (previousScreen.equalsIgnoreCase("GridView")) {
      btnStart.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (btnStart.getVisibility() == 0) {
            startSlideshow();
          }
        }
      });
    }

    slidePager.setOnPageChangeListener(new OnPageChangeListener() {
      public void onPageScrollStateChanged(int state) {
      }

      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      }

      public void onPageSelected(int position) {
        if (previousScreen.equalsIgnoreCase("GridView")) {
          tvTitle.setText(allMediaFullList.get(slidePager.getCurrentItem()).getFileName());
          if (allMediaFullList.get(position).getFileType() == null) {
            ivDownload.setVisibility(View.INVISIBLE);
            videoText.setVisibility(View.INVISIBLE);
          } else {
            String fileType = allMediaFullList.get(position).getFileType();
            if (fileType != null && fileType.equalsIgnoreCase("video/quicktime")) {
              videoText.setVisibility(View.VISIBLE);
              ivDownload.setVisibility(View.INVISIBLE);
            } else {
              videoText.setVisibility(View.INVISIBLE);
              ivDownload.setVisibility(View.VISIBLE);
            }
          }
        } else {
          tvTitle.setText(allMediaFullList.get(slidePager.getCurrentItem()).getFileName());
          videoText.setVisibility(View.INVISIBLE);
          // if (allMediaFullList.get(position).getFileType() == null) {
          //
          // videoText.setVisibility(View.INVISIBLE);
          // } else {
          // String fileType = allMediaFullList.get(position).getFileType();
          // if (fileType != null && fileType.equalsIgnoreCase("video/quicktime"))
          // videoText.setVisibility(View.VISIBLE);
          // else
          // videoText.setVisibility(View.INVISIBLE);
          // }
        }

      }
    });
    gestureDetection = new GestureDetection(this, this);

    normalMode.setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {

        gestureDetection.onTouchEvent(event);
        return false;
      }
    });

    tvMode.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return gestureDetection.onTouchEvent(event);
      }
    });

  }

  public void loadAllDetails() {
    taskItemDetails = new GetItemDetails();
    taskItemDetails.execute(curIndex);
  }

  public void fadeOutAnim() {

    if (btnStart.getVisibility() == View.VISIBLE) {

      fadeRunnable = new Runnable() {
        @Override
        public void run() {
          Animation fadeOut = new AlphaAnimation(3.00f, 0.00f);
          fadeOut.setDuration(3000);
          fadeOut.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
              btnStart.setVisibility(View.GONE);

            }
          });
          btnStart.startAnimation(fadeOut);
          hideHeader();
        }
      };
      handler.postDelayed(fadeRunnable, 3000);
    }
  }

  public void zoomInAnim() {
    if (previousScreen.equalsIgnoreCase("GridView") && allMediaFullList.size() > 1) {
      if (btnStart.getVisibility() == View.GONE) {
        Animation scale = new ScaleAnimation(0, 1, 0, 1, 50, 50);
        scale.setDuration(500);
        scale.setAnimationListener(new AnimationListener() {
          public void onAnimationStart(Animation animation) {
            // if (previousScreen.equalsIgnoreCase("GridView")) {
            btnStart.setVisibility(View.VISIBLE);
            // }
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
          }

          public void onAnimationRepeat(Animation animation) {
          }

          public void onAnimationEnd(Animation animation) {
            fadeOutAnim();
          }
        });
        btnStart.startAnimation(scale);
      }
    } else {
      getWindow().getDecorView().setSystemUiVisibility(
          View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      hideHeader();
    }
  }

  /**
   * To customize the title bar
   */
  void setUpActionBarIcon() {

    getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getActionBar().setCustomView(R.layout.header_slideshow);
    LinearLayout ll = (LinearLayout) findViewById(R.id.slide_header);
    ll.setBackgroundResource(R.color.cmstSlideshow);

    LinearLayout bottom_ll = (LinearLayout) findViewById(R.id.slide_header_line);
    bottom_ll.setBackgroundResource(R.color.cmstSlideshow);
    // bottom_ll.setVisibility(View.GONE);

    ivLeft = (ImageView) findViewById(R.id.slide_action_left);
    ivLeft.setImageDrawable(getResources().getDrawable(R.drawable.active_back_white));

    ivLeft.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        SlideshowActivity.this.onBackPressed();
      }
    });
    ivRight = (ImageView) findViewById(R.id.slide_action_right);

    ivRight.setImageDrawable(getResources().getDrawable(R.drawable.active_info));
    infoDialog = new Dialog(SlideshowActivity.this,
        android.R.style.Theme_NoTitleBar_OverlayActionModes);
    infoDialog.setCanceledOnTouchOutside(true);
    shadowDialog = new Dialog(SlideshowActivity.this,
        android.R.style.Theme_NoTitleBar_OverlayActionModes);
    shadowDialog.setContentView(R.layout.shadow);
    shadowDialog.getWindow().setBackgroundDrawable(
        new ColorDrawable(android.graphics.Color.TRANSPARENT));
    LinearLayout background = (LinearLayout) shadowDialog.findViewById(R.id.shadow_layout);
    background.setBackgroundColor(getResources().getColor(R.color.cmstInfo_transparent));

    infoDialog.setContentView(R.layout.popup_info);
    infoDialog.getWindow().setBackgroundDrawable(
        new ColorDrawable(android.graphics.Color.TRANSPARENT));
    // WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    // lp.copyFrom(infoDialog.getWindow().getAttributes());
    // lp.width = 970;
    // lp.height = 1300;
    // infoDialog.getWindow().setAttributes(lp);
    ImageView informationImage = (ImageView) infoDialog.findViewById(R.id.infoImage);
    informationImage.setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        ivRight.setImageDrawable(getResources().getDrawable(R.drawable.ic_info));
        ivRight.setSelected(false);
        infoDialog.dismiss();
        shadowDialog.dismiss();
        return false;

      }
    });
    ivRight.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        ivRight.setImageDrawable(getResources().getDrawable(R.drawable.ic_info_focus));
        shadowDialog.show();
        ivRight.setSelected(true);
        infoDialog.show();
      }
    });

    infoDialog.setOnKeyListener(new Dialog.OnKeyListener() {

      @Override
      public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
          ivRight.setImageDrawable(getResources().getDrawable(R.drawable.ic_info));
          ivRight.setSelected(false);
          shadowDialog.dismiss();
          infoDialog.dismiss();
        }
        return true;
      }
    });

    ivDownload = (ImageView) findViewById(R.id.slide_action_download);
    if (previousScreen.equalsIgnoreCase("AllMedia")) {
      ivDownload.setVisibility(View.INVISIBLE);
    }
    tvTitle = (TextView) findViewById(R.id.slide_action_title);

    ivDownload.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (DiscoveryActivity.checkForWiFi(SlideshowActivity.this)) {
          if (!imageDownloadProgress) {
            imageDownloadProgress = true;
            ivDownload.setImageResource(R.drawable.ic_download_disabled);
            // ivDownload.setBackground(mContext.getResources().getDrawable(
            // R.drawable.ic_download_disabled));
            String strUrl = "";
            String strPath = "";
            handler.removeCallbacks(fadeRunnable);
            handler.removeCallbacks(headerrunnable);
            if (previousScreen.equalsIgnoreCase("GridView")) {
              Log.e("Slideshow", "gridview download");
              strPath = allMediaFullList.get(slidePager.getCurrentItem()).getTpath();
              strPath = Constants.ipAddress + strPath.substring(1);
              strUrl = strPath.replace(".thms.", ".thml.");

              if (allMediaFullList.get(slidePager.getCurrentItem()).getFileType() == null) {
                // Toast.makeText(SlideshowActivity.this, Constants.getStringInfo(9),
                // Toast.LENGTH_LONG).show();
                return;

              } else {
                if (allMediaFullList.get(slidePager.getCurrentItem()).getFileType()
                    .equalsIgnoreCase("video/quicktime")) {
                  Toast.makeText(SlideshowActivity.this, getString(R.string.video_download),
                      Toast.LENGTH_LONG).show();
                  return;
                }
              }
            } else {
              strPath = allMediaFullList.get(slidePager.getCurrentItem()).getTpath();
              strPath = Constants.ipAddress + strPath.substring(1);
              strUrl = strPath.replace(".thms.", ".thml.");

              if (allMediaFullList.get(slidePager.getCurrentItem()).getFileType() == null) {
                // Toast.makeText(SlideshowActivity.this, Constants.getStringInfo(9),
                // Toast.LENGTH_LONG).show();
                return;
              } else {
                if (allMediaFullList.get(slidePager.getCurrentItem()).getFileType()
                    .equalsIgnoreCase("video/quicktime")) {
                  Toast.makeText(SlideshowActivity.this, getString(R.string.video_download),
                      Toast.LENGTH_LONG).show();
                  return;
                }
              }
            }

            strUrl = strUrl.replace(".thml.", ".");

            GetXMLTask task = new GetXMLTask();
            task.execute(new String[] { strUrl });
            setProgressBarVisibility(true);
            mDownloader = new ImageDownloader(strUrl, bitmapImg, new ImageLoaderListener() {
              @Override
              public void onImageDownloaded(Bitmap bmp) {
                bitmapImg = bmp;
                saveImageToSD();
              }
            });
            mDownloader.execute();
          }
        } else {
          DiscoveryActivity.errorDialog(SlideshowActivity.this, getString(R.string.err),
              Constants.getErrorMsg(getBaseContext(), 0, "wifi"), false, "");
        }
      }
    });

    tvTitle.setTextColor(getResources().getColor(R.color.cmstWhite));
  }

  /**
   * Method to update the Device gallery
   */
  private void saveImageToSD() {
    File storagePath = new File(Environment.getExternalStorageDirectory() + "/CMST/");
    storagePath.mkdirs();

    File myImage = new File(storagePath, Long.toString(System.currentTimeMillis()) + ".jpg");

    try {
      FileOutputStream out = new FileOutputStream(myImage);
      bitmapImg.compress(Bitmap.CompressFormat.JPEG, 80, out);
      out.flush();
      out.close();
      setProgressBarVisibility(false);

      MediaScannerConnection.scanFile(this, new String[] {

      myImage.getAbsolutePath() },

      null, new MediaScannerConnection.OnScanCompletedListener() {

        public void onScanCompleted(String path, Uri uri)

        {
          Log.e("scan completed", "scan completed");

        }

      });
      Toast.makeText(getBaseContext(), getString(R.string.image_downloaded), Toast.LENGTH_LONG)
          .show();
      if (previousScreen.equalsIgnoreCase("GridView") && allMediaFullList.size() > 1) {
        fadeOutAnim();
      } else {
        hideHeader();
      }
    } catch (Exception e) {
      Toast.makeText(getBaseContext(), getString(R.string.image_not_downloaded), Toast.LENGTH_LONG)
          .show();
    }
    imageDownloadProgress = false;
    // change image src to normal
    ivDownload.setImageResource(R.drawable.active_download);
  }

  private class GetXMLTask extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... urls) {
      Bitmap map = null;
      for (String url : urls) {
        map = downloadImage(url);
      }
      return map;
    }

    // Sets the Bitmap returned by doInBackground
    @Override
    protected void onPostExecute(Bitmap result) {
      // cmstImg.setImageBitmap(result);
      Log.e("in post execute", "");
    }

    // Creates Bitmap from InputStream and returns it
    private Bitmap downloadImage(String url) {
      Bitmap bitmap = null;
      InputStream stream = null;
      BitmapFactory.Options bmOptions = new BitmapFactory.Options();
      bmOptions.inSampleSize = 1;
      try {
        stream = getHttpConnection(url);
        bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
        stream.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      return bitmap;
    }

    // Makes HttpURLConnection and returns InputStream
    private InputStream getHttpConnection(String urlString) throws IOException {
      InputStream stream = null;
      URL url = new URL(urlString);
      URLConnection connection = url.openConnection();

      try {
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setRequestMethod("GET");
        httpConnection.connect();

        if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
          stream = httpConnection.getInputStream();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      return stream;
    }
  }

  private class GetItemDetails extends AsyncTask<Integer, Void, String> {

    @Override
    protected String doInBackground(Integer... params) {
      if (isCancelled()) {
        return null;
      }
      if (!DiscoveryActivity.checkForWiFi(SlideshowActivity.this)
          || allMediaFullList.get(params[0]).getFileType() != null) {
        return "";
      }

      // if (previousScreen.equalsIgnoreCase("GridView")
      // && allMediaFullList.get(params[0]).getFileType() != null) {
      // return "";
      // } else if (previousScreen.equalsIgnoreCase("AllMedia")
      // && allMediaFullList.get(params[0]).getFileType() != null) {
      // return "";
      // }

      StringBuilder data = new StringBuilder();
      String line = "";
      StringBuilder strItemUrl = new StringBuilder();
      try {
        if (previousScreen.equalsIgnoreCase("gridview")) {
          strItemUrl.append(Constants.ipAddress + Constants.apiItmById + Constants.sessionId
              + "&itemId=" + allMediaFullList.get(params[0]).getItemId() + "&itemRawDate="
              + allMediaFullList.get(params[0]).getItemRawDate());
        } else {
          strItemUrl.append(Constants.ipAddress + Constants.apiItmById + Constants.sessionId
              + "&itemId=" + allMediaFullList.get(params[0]).getItemId() + "&itemRawDate="
              + allMediaFullList.get(params[0]).getItemRawDate());
        }
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(strItemUrl.toString());
        Log.e("SlideshowActivity", strItemUrl.toString());

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
            // Log.e("CMST RESPONSE : ", "" + data);
          }
        } catch (ClientProtocolException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return data.toString() + "__" + params[0];
    }

    protected void onPostExecute(String result) {
      if (isCancelled()) {
        return;
      }
      if (!result.equalsIgnoreCase("")) {
        String[] separated = result.split("__");
        JSONObject jsonObj = null;
        try {
          jsonObj = new JSONObject(separated[0]);

          if (Constants.getServerStatus(jsonObj.getInt("res"))) {

            JSONObject obj1 = (JSONObject) jsonObj.get("objInfo");
            JSONArray jsonArray = obj1.getJSONArray("fileInfo");
            // int itemId = obj1.getInt("itemId");
            // Log.e("SlideshowActivity", jsonArray.getJSONObject(0).getString("type"));
            String fileType = jsonArray.getJSONObject(0).getString("type");
            if (previousScreen.equalsIgnoreCase("GridView")) {
              allMediaFullList.get(Integer.parseInt(separated[1])).setFileType(fileType);

              allMediaFullList.get(Integer.parseInt(separated[1])).setFileName(
                  jsonArray.getJSONObject(0).getString("fname"));
            } else {
              allMediaFullList.get(Integer.parseInt(separated[1])).setFileType(fileType);
              allMediaFullList.get(Integer.parseInt(separated[1])).setFileName(
                  jsonArray.getJSONObject(0).getString("fname"));
            }

            // if (Integer.parseInt(separated[1]) == slidePager.getCurrentItem()) {
            // if (fileType.equalsIgnoreCase("video/quicktime")) {
            // videoText.setVisibility(View.VISIBLE);
            // }
            // tvTitle.setText(jsonArray.getJSONObject(0).getString("fname"));
            // }

            // Log.e("SlideShowActivity: GetItemById", "success");
          } else {
            // Log.e("SlideShowActivity: GetItemById", "failure");
          }

        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
      if (previousScreen.equalsIgnoreCase("gridview")) {
        if (curIndex < allMediaFullList.size() - 1) {
          curIndex = curIndex + 1;
          taskItemDetails = new GetItemDetails();
          taskItemDetails.execute(curIndex);
        }
      } else {
        if (curIndex < allMediaFullList.size() - 1) {
          curIndex = curIndex + 1;
          taskItemDetails = new GetItemDetails();
          taskItemDetails.execute(curIndex);
        }
      }
    }
  }

  /**
   * Callback method called when swipe gestures are recognized by
   * 
   * 
   * {@link GestureDetection}.
   * 
   */
  @Override
  public void onSwipe(int direction) {
    switch (direction) {
    case GestureDetection.SWIPE_UP:

      Log.e("bSlideShowOn=", "" + bSlideShowOn);
      onTVMode();
      break;
    case GestureDetection.SWIPE_DOWN:
      offTVMode();
      break;
    case GestureDetection.SWIPE_RIGHT:
      if (bSlideShowOn) {
        stopSlideShow();
      }
      break;
    case GestureDetection.SWIPE_LEFT:
      if (bSlideShowOn) {
        stopSlideShow();
      }
      break;
    }
  }

  void onTVMode() {
    if (DiscoveryActivity.checkForWiFi(SlideshowActivity.this)) {
      onTVMode = true;
      if (swipeTimer != null) {
        swipeTimer.cancel();
        swipeTimer = null;
      }
      if (mHandler != null) {
        mHandler.removeCallbacks(mRunnable);
        mHandler = null;
      }
      ivDownload.setVisibility(View.INVISIBLE);
      if (previousScreen.equalsIgnoreCase("GridView")) {
        if (allMediaFullList.get(slidePager.getCurrentItem()).getFileType() != null
            && allMediaFullList.get(slidePager.getCurrentItem()).getFileType()
                .equalsIgnoreCase("video/quicktime")) {
          imgTvMode.setImageDrawable(getResources().getDrawable(R.drawable.infobar_tvmode_video));
        } else {
          imgTvMode.setImageDrawable(getResources().getDrawable(R.drawable.infobar_tvmode_photo));
        }
      } else {
        if (allMediaFullList.get(slidePager.getCurrentItem()).getFileType() != null
            && allMediaFullList.get(slidePager.getCurrentItem()).getFileType()
                .equalsIgnoreCase("video/quicktime")) {
          imgTvMode.setImageDrawable(getResources().getDrawable(R.drawable.infobar_tvmode_video));
        } else {
          imgTvMode.setImageDrawable(getResources().getDrawable(R.drawable.infobar_tvmode_photo));
        }
      }

      tvMode.setVisibility(View.VISIBLE);
      // imgTvMode.setVisibility(View.VISIBLE);
      normalMode.setVisibility(View.INVISIBLE);

      if (bSlideShowOn) {
        slideShowOnCMST = true;
      } else {
        slideShowOnCMST = false;
      }
      new PlayOnCMST().execute(slidePager.getCurrentItem());
    } else {
      DiscoveryActivity.errorDialog(SlideshowActivity.this, getString(R.string.err),
          Constants.getErrorMsg(getBaseContext(), 0, "wifi"), false, "");
    }
  }

  void offTVMode() {

    onTVMode = false;
    Log.e("offTV Mode::::::::bSlideShowOn", "" + bSlideShowOn + " : " + previousScreen);
    if (bSlideShowOn && slideShowOnCMST && previousScreen.equalsIgnoreCase("GridView")) {
      bSlideShowOn = false;
      startSlideshow();
      // continue the slideshow
    } else {

    }
    if (previousScreen.equalsIgnoreCase("GridView")
        && allMediaFullList.get(slidePager.getCurrentItem()).getFileType() != null
        && !allMediaFullList.get(slidePager.getCurrentItem()).getFileType()
            .equalsIgnoreCase("video/quicktime")) {
      ivDownload.setVisibility(View.VISIBLE);
    }
    tvMode.setVisibility(View.INVISIBLE);
    // imgTvMode.setVisibility(View.INVISIBLE);
    normalMode.setVisibility(View.VISIBLE);
  }

  void stopSlideShow() {
    bSlideShowOn = false;
    if (swipeTimer != null) {
      swipeTimer.cancel();
      swipeTimer = null;
    }
    if (mHandler != null) {
      mHandler.removeCallbacks(mRunnable);
      mHandler = null;
    }
    itemPosition = slidePager.getCurrentItem();
    btnStart.setImageDrawable(getResources().getDrawable(R.drawable.active_slideshow_start));
    header.setVisibility(View.VISIBLE);
  }

  @Override
  public void onTap(int noOfTimes) {
    if (noOfTimes == 2) {
      zoomInAnim();
    }
  }

  private class PlayOnCMST extends AsyncTask<Integer, Void, String> {

    @Override
    protected String doInBackground(Integer... params) {
      StringBuilder data = new StringBuilder();
      String line = "";
      try {
        StringBuilder strUrl = new StringBuilder();
        strUrl.append(Constants.ipAddress);
        if (bSlideShowOn) {
          strUrl.append(Constants.apiAlbmPlyBck);
        } else {
          strUrl.append(Constants.apiItemPlyBck);
        }
        if (previousScreen.equalsIgnoreCase("GridView")) {
          strUrl.append(Constants.sessionId + "&albmId=" + Constants.albumId + "&itemId="
              + allMediaFullList.get(params[0]).getItemId() + "&itemRawDate="
              + allMediaFullList.get(params[0]).getItemRawDate());
        } else {
          strUrl.append(Constants.sessionId + "&albmId="
              + allMediaFullList.get(params[0]).getAlbmId() + "&itemId="
              + allMediaFullList.get(params[0]).getItemId() + "&itemRawDate="
              + allMediaFullList.get(params[0]).getItemRawDate());
        }

        Log.e("strUrl=", "" + strUrl);
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(strUrl.toString());
        Log.e("SlideshowActivity", strUrl.toString());

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
          }
        } catch (ClientProtocolException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return data + "__" + params[0];
    }

    protected void onPostExecute(String result) {
      String[] separated = result.split("__");
      JSONObject jsonObj = null;
      try {
        jsonObj = new JSONObject(separated[0]);
        if (Constants.getServerStatus(jsonObj.getInt("res"))) {
          Log.e("SlideShowActivity: Play back", "success");
        } else {
          DiscoveryActivity.errorDialog(SlideshowActivity.this, getString(R.string.err),
              Constants.getErrorMsg(getBaseContext(), jsonObj.getInt("res"), ""), false, "");
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public boolean onSingleTapConfirmed(MotionEvent arg) {
    // TODO Auto-generated method stub

    return false;
  }

  @Override
  public boolean onTouch(View arg0, MotionEvent arg1) {
    // TODO Auto-generated method stub
    Log.e("Slideshow", "onsingle tap confirmed");
    return false;
  }

  /**
   * Called when return key is pressed
   */
  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }

  public void onDestroy() {

    super.onDestroy();
    if (infoDialog != null) {
      if (infoDialog.isShowing())
        infoDialog.dismiss();
      infoDialog = null;
    }
    if (shadowDialog != null) {
      if (shadowDialog.isShowing())
        shadowDialog.dismiss();
      shadowDialog = null;
    }
    releaseMemory();
    if (swipeTimer != null) {
      swipeTimer.cancel();
      swipeTimer = null;
    }
    if (mHandler != null) {
      mHandler.removeCallbacks(mRunnable);
      mHandler = null;
    }
  }

  private void releaseMemory() {
    Log.e("SlideShow", "ReleaseMemory");
    if (taskItemDetails != null)
      taskItemDetails.cancel(true);
  }

  /**
   * Called when activity is stopped.
   */
  protected void onStop() {
    super.onStop();

  }

}
