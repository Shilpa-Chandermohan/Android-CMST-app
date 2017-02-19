package com.cmst.cmstapp;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmst.cache.util.AlbumDetails;
import com.cmst.cache.util.ImageUtil;

public class AlbumListAdapter extends BaseAdapter {

  private Context appContext;

  private ArrayList<AlbumDetails> albumList;

  private LayoutInflater inflater;

  private static final int HOLDER_KEY = -21;

  private static final int TASK_KEY = -25;

  /**
   * 
   * @param context
   */
  public AlbumListAdapter(Context context, ArrayList<AlbumDetails> albumList) {
    appContext = context;
    inflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.albumList = albumList;

  }

  /**
   * returns number of albums.
   */
  @Override
  public int getCount() {

    if (albumList != null) {
      return albumList.size();
    }
    return 0;
  }

  /**
   * returns AlbumItemDetails object or null (please handle) at a particular position.
   */
  @Override
  public AlbumDetails getItem(int position) {
    if (albumList != null) {
      return albumList.get(position);
    }
    return null;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  /**
   * Adding album details to array.
   */
  public void add(ArrayList<AlbumDetails> files) {
    this.albumList.addAll(files);
    notifyDataSetChanged();
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

      View myView = inflater.inflate(R.layout.album_item, parent, false);
      ImageView smallImageView = (ImageView) myView.findViewById(R.id.imageOfAlbum);
      TextView albumName = (TextView) myView.findViewById(R.id.albumName);
      TextView albumDate = (TextView) myView.findViewById(R.id.albumDate);

      albumName.setText(albumList.get(position).getAlbmName());
      albumDate.setText(albumList.get(position).getAlbmDate());

      ViewHolder holder = new ViewHolder();
      holder.setSmallImageView(smallImageView);
      holder.setAlbumDate(albumDate);
      holder.setAlbumName(albumName);
      myView.setTag(HOLDER_KEY, holder);

      BitmapBackgroundWorker worker = new BitmapBackgroundWorker(smallImageView, albumList.get(
          position).getSmallThumbnailFileName(), albumList.get(position).getDir());
      smallImageView.setTag(TASK_KEY, worker);
      worker.execute();
      return myView;

    }

    ViewHolder tempHolder = (ViewHolder) convertView.getTag(HOLDER_KEY);
    tempHolder.getAlbumName().setText(albumList.get(position).getAlbmName());
    tempHolder.getAlbumDate().setText(albumList.get(position).getAlbmDate());
    ImageView tempSmallImageView = tempHolder.getSmallImageView();
    tempSmallImageView.setImageResource(android.R.color.transparent);
    cancelOldTask(tempSmallImageView);
    BitmapBackgroundWorker workerTask = new BitmapBackgroundWorker(tempSmallImageView, albumList
        .get(position).getSmallThumbnailFileName(), albumList.get(position).getDir());
    tempSmallImageView.setTag(TASK_KEY, workerTask);
    workerTask.execute();
    return convertView;
  }

  // /**
  // *
  // * Re-cycling is done.
  // *
  // * Decoding bitmap is done in a different thread.
  // *
  // */
  // @Override
  // public View updateView(int position, String albumName) {
  //
  // if (convertView == null) {
  //
  // View myView = inflater.inflate(R.layout.album_item, parent, false);
  // ImageView smallImageView = (ImageView) myView.findViewById(R.id.imageOfAlbum);
  // TextView albumName = (TextView) myView.findViewById(R.id.albumName);
  // TextView albumDate = (TextView) myView.findViewById(R.id.albumDate);
  //
  // albumName.setText(albumList.get(position).getAlbmName());
  // albumDate.setText(albumList.get(position).getAlbmDate());
  //
  // ViewHolder holder = new ViewHolder();
  // holder.setSmallImageView(smallImageView);
  // holder.setAlbumDate(albumDate);
  // holder.setAlbumName(albumName);
  // myView.setTag(HOLDER_KEY, holder);
  //
  // BitmapBackgroundWorker worker = new BitmapBackgroundWorker(smallImageView, albumList.get(
  // position).getSmallThumbnailFileName(), albumList.get(position).getDir());
  // smallImageView.setTag(TASK_KEY, worker);
  // worker.execute();
  // return myView;
  //
  // }
  //
  // ViewHolder tempHolder = (ViewHolder) convertView.getTag(HOLDER_KEY);
  // tempHolder.getAlbumName().setText(albumList.get(position).getAlbmName());
  // tempHolder.getAlbumDate().setText(albumList.get(position).getAlbmDate());
  // ImageView tempSmallImageView = tempHolder.getSmallImageView();
  // tempSmallImageView.setImageResource(android.R.color.transparent);
  // cancelOldTask(tempSmallImageView);
  // BitmapBackgroundWorker workerTask = new BitmapBackgroundWorker(tempSmallImageView, albumList
  // .get(position).getSmallThumbnailFileName(), albumList.get(position).getDir());
  // tempSmallImageView.setTag(TASK_KEY, workerTask);
  // workerTask.execute();
  // return convertView;
  // }

  public void updateView(int index, String albmName) {
//    
//    int visiblePosition = AlbumFragment.albumContext.getFirstVisiblePosition()
//    View view = getChildAt(index - visiblePosition);
//    this..getAdapter().getView(index, view, mListView);
//
//    View v = this.getChildAt(index - appContext.getFirstVisiblePosition());
//
//    if (v == null)
//      return;
//
//    TextView someText = (TextView) v.findViewById(R.id.sometextview);
//    someText.setText("Hi! I updated you manually!");

    // Log.e(Constants.TAG, "" + index);
    // albumList.get(index).setAlbmName(albmName);
    // // View v =
    // // View v = AlbumFragment.albumContext.getChildAt(index
    // // - AlbumFragment.albumContext.getFirstVisiblePosition());
    //
    // if (v == null)
    // return;
    // TextView someText = (TextView) v.findViewById(R.id.albumName);
    // someText.setText(albmName);
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

    private TextView albumName;

    private TextView albumDate;

    public TextView getAlbumName() {
      return albumName;
    }

    public void setAlbumName(TextView albumName) {
      this.albumName = albumName;
    }

    public TextView getAlbumDate() {
      return albumDate;
    }

    public void setAlbumDate(TextView albumDate) {
      this.albumDate = albumDate;
    }

    public ImageView getSmallImageView() {
      return smallImageView;
    }

    public void setSmallImageView(ImageView smallImageView) {
      this.smallImageView = smallImageView;
    }
  }

  public void clear() {
    albumList.clear();
    notifyDataSetChanged();
  }
}
