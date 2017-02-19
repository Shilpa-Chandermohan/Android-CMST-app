package com.cmst.cmstapp;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cmst.cache.util.AlbumDetails;
import com.cmst.cache.util.AlbumItems;
import com.cmst.cache.util.Cache;
import com.cmst.common.AlbumData;
import com.cmst.common.AllMediaData;
import com.cmst.common.Constants;

/**
 * This is a fragment that displays the details of a particular item.
 */

public class GridviewFragment extends Fragment {

  GridView gridGallery;
  Handler handler;
  GridviewAdapter adapter;
  ImageView menuAlbum;
  ImageView menuUpload;
  ImageView menuGallery;
  static Context mcontext;
  static int mLayoutId;
  static View itemView;
  View view;
  String action;
  GestureDetector gestureDetector;
  JSONArray jsonItems;

  public static int albumPosition = 0;
  private boolean isServerDone = false;
  private AlbumItems allMediaItemList;
  TextView actionTitle;
  private int serverStatus;
  RotateAnimation anim;
  private MediaFetcher taskMediaFetcher;

  AlbumData albumData;

  // private ArrayList<AlbumDetails> albumData;

  /**
   * Create a new instance of DetailsFragment, initialized to show the text at 'index'.
   */
  public static GridviewFragment newInstance(int index, Context context, int layoutId) {

    mcontext = context;
    mLayoutId = layoutId;
    /**
     * Supply index as input argument.
     */
    Bundle args = new Bundle();
    args.putInt("index", index);
    GridviewFragment gridFragment = new GridviewFragment();
    gridFragment.setArguments(args);
    albumPosition = index;
    return gridFragment;
  }

  public int getShownIndex() {
    return getArguments().getInt("index", 0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    setRetainInstance(true);
    albumData = AlbumData.getInstance();
    // albumData = (ArrayList<AlbumDetails>) getArguments().get("albumList");

    view = inflater.inflate(R.layout.gridview, container, false);
    view.setVerticalScrollBarEnabled(false);
    actionTitle = (TextView) getActivity().findViewById(R.id.action_title);
    if (Constants.isTablet) {
      getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
          LayoutParams.MATCH_PARENT, 1.0f);
      param.weight = 1.0f;
      view.findViewById(R.id.gridGalleryCont).setLayoutParams(param);
      view.findViewById(R.id.llBottomContainer).setVisibility(View.GONE);
      view.findViewById(R.id.llBottomContainerSeparator).setVisibility(View.GONE);
    } else {
      initBtnClick();
    }
    anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    anim.setInterpolator(new LinearInterpolator());
    anim.setRepeatCount(Animation.INFINITE);
    anim.setDuration(1500);
    view.findViewById(R.id.gridProgressBar).startAnimation(anim);
    if (container == null) {
      return null;
    }

    initializeGrid();
    int orientation = getActivity().getRequestedOrientation();
    getActivity().setRequestedOrientation(orientation);
    taskMediaFetcher = new MediaFetcher();
    taskMediaFetcher.execute(Constants.albumId);

    return view;
  }

  /**
   * Initialize grid fragment depending on the device.
   */
  private void initializeGrid() {
    try {
      gridGallery = (GridView) view.findViewById(R.id.gridGallery);
      if (Constants.isTablet) {
        gridGallery.setNumColumns(6);
      } else {
        gridGallery.setNumColumns(5);
      }
      gridGallery.setFastScrollEnabled(true);
      gridGallery.setOnItemClickListener(mitemMulClickListener);

      adapter = new GridviewAdapter(getActivity(), null);
      final GestureDetector gesture = new GestureDetector(getActivity(),
          new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent event) {
              return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
              Log.d("Grid view", "On FFFFFFFFFFFFFFFFFFFFling");
              float sensitvity = 200;
              if ((e1.getX() - e2.getX()) > sensitvity) {
                setGridAnimation("left");
              } else if ((e2.getX() - e1.getX()) > sensitvity) {
                setGridAnimation("right");
                // SwipeRight();
              }
              return false;
            }
          });
      gridGallery.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
          return gesture.onTouchEvent(event);
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void setGridAnimation(final String direction) {
    try {
      Animation slide = null;
      if (direction.equalsIgnoreCase("left") && albumPosition < albumData.albumList.size() - 1
          && isServerDone) {
        Log.e("move right", "mm");
        slide = AnimationUtils.loadAnimation(getActivity(), R.anim.current_to_right);
      } else if (direction.equalsIgnoreCase("right") && albumPosition > 0 && isServerDone) {
        Log.e("move left", "mm");
        slide = AnimationUtils.loadAnimation(getActivity(), R.anim.current_to_left);
      }
      if (slide != null) {
        slide.setAnimationListener(new AnimationListener() {

          @Override
          public void onAnimationStart(Animation animation) {
            // TODO Auto-generated method stub

          }

          @Override
          public void onAnimationRepeat(Animation animation) {
            // TODO Auto-generated method stub

          }

          @Override
          public void onAnimationEnd(Animation animation) {
            // TODO Auto-generated method stub
            if (direction.equalsIgnoreCase("left")) {
              swipeLeft();
            } else {
              swipeRight();
            }
          }
        });
        gridGallery.startAnimation(slide);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * When grid view is swiped left, Load previous album data.
   */
  private void swipeLeft() {

    isServerDone = false;

    adapter.clear();

    albumPosition = albumPosition + 1;
    Constants.albumId = String.valueOf(albumData.albumList.get(albumPosition).getAlbmId());
    Constants.albumDir = albumData.albumList.get(albumPosition).getDir();
    // AlbumFragment.albumContext.setSelection(albumPosition);
    if (!Constants.isTablet) {
      ((GridviewActivity) getActivity()).setActionTitle(albumData.albumList.get(albumPosition)
          .getAlbmName());
    } else {
      // AlbumFragment.albumContext.setSelection(albumPosition);
      AlbumFragment.albumContext.setItemChecked(GridviewFragment.albumPosition, true);
      actionTitle.setText(albumData.albumList.get(albumPosition).getAlbmName());
    }

    AlbumFragment.albumContext.smoothScrollToPosition(albumPosition);

    // taskMediaFetcher = new MediaFetcher();
    // taskMediaFetcher.execute(Constants.albumId);
    new MediaFetcher().execute(Constants.albumId);
    adapter.setFlag("0");

  }

  /**
   * When grid view is swiped right, Load next album data.
   */
  private void swipeRight() {
    Log.e("Swipe left", albumPosition + " -:- " + albumData.albumList.size());

    isServerDone = false;

    adapter.clear();

    albumPosition = albumPosition - 1;
    Constants.albumId = String.valueOf(albumData.albumList.get(albumPosition).getAlbmId());
    Constants.albumDir = albumData.albumList.get(albumPosition).getDir();
    // AlbumFragment.albumContext.setSelection(albumPosition);
    if (!Constants.isTablet) {
      ((GridviewActivity) getActivity()).setActionTitle(albumData.albumList.get(albumPosition)
          .getAlbmName());
    } else {
      // AlbumFragment.albumContext.setSelection(albumPosition);
      AlbumFragment.albumContext.setItemChecked(GridviewFragment.albumPosition, true);
      actionTitle.setText(albumData.albumList.get(albumPosition).getAlbmName());
    }

    AlbumFragment.albumContext.smoothScrollToPosition(albumPosition);

    // taskMediaFetcher = new MediaFetcher();
    // taskMediaFetcher.execute(Constants.albumId);
    new MediaFetcher().execute(Constants.albumId);
    adapter.setFlag("1");
  }

  /**
   * Adding onclick to each gridview element to call slideshow.
   */
  AdapterView.OnItemClickListener mitemMulClickListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
      Log.e("Grid Fragment", "" + allMediaItemList.getListOfItems().size());
      Intent intent = new Intent(mcontext, SlideshowActivity.class);
      intent.putExtra("position", position);
      intent.putExtra("previousScreen", "GridView");

      AllMediaData mediaData;
      mediaData = AllMediaData.getInstance();
      mediaData.allMediaList = allMediaItemList.getListOfItems();

      // Log.e("Grid Fragment", "" + allMediaItemList.getListOfItems().size());
      // intent.putExtra("MyAlbumList", allMediaItemList.getListOfItems());

      mcontext.startActivity(intent);
    }
  };

  /**
   * Adding onclick for footer icons.
   */
  private void initBtnClick() {
    menuAlbum = (ImageView) view.findViewById(R.id.album);
    menuAlbum.setImageDrawable(getResources().getDrawable(R.drawable.albumselected));
    /**
     * Adding onclick for Goto album screen.
     */
    menuAlbum.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // if (HomeActivity.checkForWiFi(getActivity())) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(getActivity(), HomeActivity.class);
        startActivity(intent);
        getActivity().finish();
        // } else {
        // HomeActivity.errorDialog(getActivity(), Constants.errorTitle,
        // Constants.getErrorMsg(0, "wifi"), false);
        // }
      }
    });
    menuGallery = (ImageView) view.findViewById(R.id.gallery);
    /**
     * Adding onclick for Goto All media.
     */
    menuGallery.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // if (HomeActivity.checkForWiFi(getActivity())) {
        Intent intent = new Intent(getActivity().getApplicationContext(), AllMediaActivity.class);
        ComponentName cn = intent.getComponent();
        Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
        startActivity(mainIntent);
        // } else {
        // HomeActivity.errorDialog(getActivity(), Constants.errorTitle,
        // Constants.getErrorMsg(0, "wifi"), false);
        // }
      }
    });
  }

  private class MediaFetcher extends AsyncTask<String, Void, AlbumItems> {

    protected void onPreExecute() {
      view.findViewById(R.id.gridProgressBar).startAnimation(anim);
      view.findViewById(R.id.gridProgressBar).setVisibility(View.VISIBLE);
    }

    @Override
    protected AlbumItems doInBackground(String... param) {
      if (isCancelled()) {
        return null;
      }
      return Cache.getInstance().getAlbumItemDetails(Constants.cmstName, Integer.valueOf(param[0]),
          Constants.albumDir, mcontext);
    }

    protected void onPostExecute(AlbumItems itemList) {
      try {
        view.findViewById(R.id.gridProgressBar).clearAnimation();
        view.findViewById(R.id.gridProgressBar).setVisibility(View.GONE);
        if (isCancelled()) {
          return;
        }

        if (itemList == null && !Constants.getServerStatus(itemList.getRes())) {
          DiscoveryActivity.errorDialog(getActivity(), getString(R.string.err),
              Constants.getErrorMsg(getActivity().getBaseContext(), serverStatus, ""), false, "");
        } else {

          adapter.clear();

          allMediaItemList = new AlbumItems();
          allMediaItemList.setRes(itemList.getRes());
          allMediaItemList.setListOfItems(itemList.getListOfItems());

          adapter.addAllMediaItems(itemList.getListOfItems());
          gridGallery.setAdapter(adapter);

          isServerDone = true;

          Animation slide;
          if (adapter.getFlagValue().equalsIgnoreCase("0")) {
            slide = AnimationUtils.loadAnimation(getActivity(), R.anim.left);
            gridGallery.startAnimation(slide);
          } else {
            slide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_right);
            gridGallery.startAnimation(slide);
          }

        }
      } catch (Exception e) {
        e.printStackTrace();

      }
    }
  }

  public void onDestroy() {
    super.onDestroy();
    releaseMemory();
  }

  private void releaseMemory() {
    Log.e("GridViewFragment", "Release Memory");
    if (taskMediaFetcher != null)
      taskMediaFetcher.cancel(true);
  }

}
