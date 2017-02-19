package com.cmst.cmstapp;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cmst.cache.util.AlbumItemDetails;
import com.cmst.cache.util.ImageUtil;
import com.cmst.common.AllMediaData;

public class SlideshowAdapter extends PagerAdapter {
  Context context;
  private ArrayList<AlbumItemDetails> mediaList;

  /**
   * constructor
   * 
   * @param context
   * @param imageLoader
   */
  SlideshowAdapter(Context context) {
    this.context = context;
    this.mediaList = AllMediaData.getInstance().allMediaList;
  }

  /**
   * Method to get number of images for slide show
   */
  @Override
  public int getCount() {
    return this.mediaList.size();
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == ((ImageView) object);
  }

  /**
   * Method called each time image has to be loaded to a page
   */
  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    CustomImageView imageView = new CustomImageView(context);
    ((ViewPager) container).addView(imageView, 0);

    BitmapBackgroundWorker task = new BitmapBackgroundWorker(imageView, this.mediaList
        .get(position).getLargeThumbnailFileName(), this.mediaList.get(position).getDir());
    task.execute();

    // Bitmap bitmap = ImageUtil.getBitmap(this.context, this.mediaList.get(position)
    // .getLargeThumbnailFileName(), this.mediaList.get(position).getDir());
    // imageView.setImageBitmap(bitmap);

    imageView.setTag("myview" + position);
    return imageView;
  }

  /**
   * Method to remove images not in the view
   */
  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    ((ViewPager) container).removeView((ImageView) object);
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
      return ImageUtil.getBitmap(context, imageName, albumDir);
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
}
