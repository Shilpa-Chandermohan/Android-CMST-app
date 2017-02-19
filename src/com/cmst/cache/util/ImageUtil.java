package com.cmst.cache.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

public class ImageUtil {

  private static final String TAG = "ImageUtil";

  /**
   * 
   * Please don't call this method from UI thread<br/>
   * <br/>
   * Overriden
   * 
   * @param appContext
   * @param url
   * @param fileName
   * @param dir
   *          File object refering to appcontexts folder
   * @return
   * 
   *         boolean value , true or false
   */
  public static final boolean downloadImageFromServerAndSaveToInternalStorage(Context appContext,
      String url, String fileName, File dir) {
    URLConnection connection = null;
    InputStream inputStream = null;
    FileOutputStream fileOutputStream = null;
    try {
      URL serverURL = new URL(url);
      Log.d(TAG, "thumbnail url is: " + url);
      connection = serverURL.openConnection();
      inputStream = connection.getInputStream();
      byte[] buffer = new byte[CacheConstants.BYTE_SIZE];

      // File dir=appContext.getDir(CacheConstants.IMAGES_FOLDER, Context.MODE_PRIVATE);
      if (!dir.exists()) {
        dir.mkdirs();
      }
      File imgFile = new File(dir, fileName);
      fileOutputStream = new FileOutputStream(imgFile);

      // fileOutputStream=appContext.openFileOutput(fileName, Context.MODE_PRIVATE);
      int length = 0;
      while ((length = inputStream.read(buffer)) != -1) {
        fileOutputStream.write(buffer, 0, length);
      }
      fileOutputStream.flush();
      Log.d(TAG, " (image) downloading finished ...");
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      Log.d(TAG, "thumbnail url is: " + url);
      Log.e(TAG, "Exception in downloading " + fileName + " image from server -> " + e.getMessage());
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          // e.printStackTrace();
        }
      }

      if (fileOutputStream != null) {
        try {
          fileOutputStream.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

    }
    return false;
  }

  /**
   * 
   * Please don't call this method from UI thread<br/>
   * <br/>
   * 
   * 
   * @param path
   * @param imageWidth
   * @param imageHeight
   * @param outWidth
   * @param outHeight
   * @return
   * 
   *         Bitmap Object or null
   */
  public static final Bitmap getBitmap(Context appContext, String imageName, File albumDir) {

    FileInputStream fis = null;
    Log.d(TAG, "file name :" + imageName + " album dir :" + albumDir);
    try {
      File imgFile = new File(albumDir, imageName);
      fis = new FileInputStream(imgFile);
      return BitmapFactory.decodeStream(fis);
    } catch (FileNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }  catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } 
    finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  public static Bitmap getBitmap(Context appContext, String dirPath, String imgName) {

    File imgFile = new File(dirPath, imgName);
    FileInputStream fis = null;
    Log.d(TAG, "file name :" + imgName + " album dir :" + dirPath);
    try {
      fis = new FileInputStream(imgFile);
      return BitmapFactory.decodeStream(fis);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (Exception e) {
          // TODO: handle exception
          e.printStackTrace();
        }
      }
    }

    return null;
  }

  /**
   * 
   * @param imageWidth
   * @param imageHeight
   * @param outWidth
   * @param outHeight
   * @return
   * 
   *         integer
   */
  private static int getSampleSize(int imageWidth, int imageHeight, int outWidth, int outHeight) {

    int sampleSize = 1;

    if (outWidth < imageWidth || outHeight < imageHeight) {
      int halfImageWidth = imageWidth / 2;
      int halfImageHeight = imageHeight / 2;

      while (((halfImageWidth / sampleSize) > outWidth)
          && ((halfImageHeight / sampleSize) > outHeight)) {
        sampleSize *= 2;
      }

    }
    return sampleSize;
  }

  public static final boolean deleteImageFromStorage(Context appContext, String imageName) {
    File file = appContext.getFileStreamPath(imageName);
    if (file.exists()) {
      return file.delete();
    }

    return false;
  }

  /**
   * 
   * Please don't call this method from UI thread<br/>
   * <br/>
   * 
   * 
   * @param path
   * @param imageWidth
   * @param imageHeight
   * @param outWidth
   * @param outHeight
   * @return
   * 
   *         Bitmap Object or null
   */
//  public static final Bitmap getBitmap(Context appContext, String imageName, String cmstId,
//      File dir, int rectWidth, int rectHeight) {
//
//    Log.e("getSubSampleBitmap", "imageName ---->" + imageName + " : " + rectWidth + " : "
//        + rectHeight);
//
//    File imgFile = new File(dir, imageName);
//
//    Options options = new Options();
//    options.inJustDecodeBounds = true;
//    BitmapFactory.decodeFile(imgFile.getPath(), options);
//    
//    Log.e("getSubSampleBitmap", "image width ---->" + options.outWidth + " :  h-> " + options.outHeight );
//    
//    options.inSampleSize = getSampleSize(options.outWidth, options.outHeight, rectWidth, rectHeight);
//    options.inJustDecodeBounds = false;
//    return BitmapFactory.decodeFile(imgFile.getPath(), options);
//
//  }

}
