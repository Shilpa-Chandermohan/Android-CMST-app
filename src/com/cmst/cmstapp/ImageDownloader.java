package com.cmst.cmstapp;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class ImageDownloader extends AsyncTask<Void, Integer, Void> {

  private String url;
  private Bitmap bmp;
  private ImageLoaderListener listener;

  /*--- constructor ---*/
  public ImageDownloader(String url, Bitmap bmp, ImageLoaderListener listener) {
    /*--- we need to pass some objects we are going to work with ---*/
    this.url = url;
    this.bmp = bmp;
    this.listener = listener;
  }

  /**
   * Listener to indicate that image has been downloaded.
   */
  public interface ImageLoaderListener {
    void onImageDownloaded(Bitmap bmp);
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
  }

  @Override
  protected Void doInBackground(Void... arg0) {
    bmp = getBitmapFromURL(url);
    return null;
  }

  @Override
  protected void onProgressUpdate(Integer... values) {
    super.onProgressUpdate(values);
  }

  @Override
  protected void onPostExecute(Void result) {
    if (listener != null) {
      listener.onImageDownloaded(bmp);
    }
    super.onPostExecute(result);
  }

  public static Bitmap getBitmapFromURL(String link) {
    /*--- this method downloads an Image from the given URL, 
     *  then decodes and returns a Bitmap object
     ---*/
    try {
      URL url = new URL(link);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.connect();
      InputStream input = connection.getInputStream();
      Bitmap myBitmap = BitmapFactory.decodeStream(input);
      return myBitmap;
    } catch (Exception e) {
      e.printStackTrace();
      Log.e("getBmpFromUrl error: ", e.getMessage().toString());
      return null;
    }
  }

}
