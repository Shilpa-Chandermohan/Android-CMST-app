package com.cmst.cmstapp;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cmst.cache.util.AlbumItemDetails;
import com.cmst.cache.util.ImageUtil;

public class GridviewAdapter extends BaseAdapter {

  private Context appContext;

  private ArrayList<AlbumItemDetails> itemList;

  // TODO: need to discuss on this
  private String flag = "0";

  private LayoutInflater inflater;

  private static final int HOLDER_KEY = -21;

  private static final int TASK_KEY = -25;

  /**
   * 
   * @param context
   */
  public GridviewAdapter(Context context, ArrayList<AlbumItemDetails> list) {
    appContext = context;
    inflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    itemList = list;
  }

  /**
   * returns number of albums.
   */
  @Override
  public int getCount() {

    if (this.itemList != null) {
      return this.itemList.size();
    }
    return 0;
  }

  /**
   * returns AlbumItemDetails object or null (please handle) at a particular position.
   */
  @Override
  public AlbumItemDetails getItem(int position) {
    if (this.itemList != null) {
      return this.itemList.get(position);
    }
    return null;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  /**
   * returns flag value to check if left/right swipe has been performed.
   */
  public String getFlagValue() {
    return flag;
  }

  /**
   * Sets left/right swipe flag.
   */
  public void setFlag(String param) {
    flag = param;
  }

  /**
   * Adding album details to array.
   */
  public void addAllMediaItems(ArrayList<AlbumItemDetails> list) {
    this.itemList = list;
    // this.itemList.addAll(files);
    notifyDataSetChanged();
  }

  /**
   * Method to convert dp to pixels.
   */
  private int convertDpToPixels(float dp, Context context) {
    Resources resources = context.getResources();
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
        resources.getDisplayMetrics());
  }

  /**
   * 
   * Re-cycling is done.
   * 
   * Decoding bitmap is done in a different thread.
   * 
   */
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    if (convertView == null) {

      View myView = inflater.inflate(R.layout.gridview_item, parent, false);
      ImageView smallImageView = (ImageView) myView.findViewById(R.id.smallImageInGrid);

      // TODO: need to discuss on this
      // if (!Constants.isTablet) {
      // smallImageView.getLayoutParams().height = convertDpToPixels(68, appContext);
      // }
      ViewHolder holder = new ViewHolder();
      holder.setSmallImageView(smallImageView);
      myView.setTag(HOLDER_KEY, holder);

      BitmapBackgroundWorker worker = new BitmapBackgroundWorker(smallImageView, this.itemList.get(
          position).getSmallThumbnailFileName(), this.itemList.get(position).getDir());
      smallImageView.setTag(TASK_KEY, worker);
      worker.execute();
      return myView;

    }

    ViewHolder tempHolder = (ViewHolder) convertView.getTag(HOLDER_KEY);
    ImageView tempSmallImageView = tempHolder.getSmallImageView();
    // if (!Constants.isTablet) {
    // tempSmallImageView.getLayoutParams().height = convertDpToPixels(68, appContext);
    // }
    tempSmallImageView.setImageResource(android.R.color.transparent);
    cancelOldTask(tempSmallImageView);
    BitmapBackgroundWorker workerTask = new BitmapBackgroundWorker(tempSmallImageView,
        this.itemList.get(position).getSmallThumbnailFileName(), this.itemList.get(position)
            .getDir());
    tempSmallImageView.setTag(TASK_KEY, workerTask);
    workerTask.execute();
    return convertView;
  }

  private void cancelOldTask(ImageView imgView) {

    BitmapBackgroundWorker oldTask = (BitmapBackgroundWorker) imgView.getTag(TASK_KEY);
    if (oldTask != null) {
      oldTask.cancel(true);
    }
  }

  private class BitmapBackgroundWorker extends AsyncTask<String, Void, Bitmap> {

    private WeakReference<ImageView> weakRefOfImageView;

    private String imageName;

    private File albumDir;

    public BitmapBackgroundWorker(ImageView imgView, String imgName, File albmDir) {
      weakRefOfImageView = new WeakReference<ImageView>(imgView);
      this.albumDir = albmDir;
      this.imageName = imgName;
    }

    @Override
    protected Bitmap doInBackground(String... params) {

      return ImageUtil.getBitmap(appContext, imageName, albumDir);
    }

    protected void onPostExecute(Bitmap bmp) {
      if (this.isCancelled()) {
        bmp = null;
      }
      if (bmp != null && weakRefOfImageView != null) {
        if (weakRefOfImageView.get() != null) {
          weakRefOfImageView.get().setImageBitmap(bmp);
        }

      }
    }
  }

  private class ViewHolder {
    private ImageView smallImageView;

    public ImageView getSmallImageView() {
      return smallImageView;
    }

    public void setSmallImageView(ImageView smallImageView) {
      this.smallImageView = smallImageView;
    }
  }

  public void clear() {
    if (this.itemList != null) {
      this.itemList.clear();
    }
    notifyDataSetChanged();
  }
}
