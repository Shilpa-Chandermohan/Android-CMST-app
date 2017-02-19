package com.cmst.cache.sync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.cmst.cache.provider.Contract;
import com.cmst.cache.provider.Contract.Albums;
import com.cmst.cache.provider.Contract.SyncStatus;
import com.cmst.cache.util.AlbumCountResponse;
import com.cmst.cache.util.AlbumDetails;
import com.cmst.cache.util.AlbumItemDetails;
import com.cmst.cache.util.AlbumItems;
import com.cmst.cache.util.AlbumItemsCountResponse;
import com.cmst.cache.util.AlbumList;
import com.cmst.cache.util.Cache;
import com.cmst.cache.util.CacheConstants;
import com.cmst.cache.util.CmstAPIException;
import com.cmst.cache.util.ImageUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

  private Context appContext;
  private static final String TAG = "SyncAdapter";

  public SyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
    this.appContext = context;
  }

  public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
    super(context, autoInitialize, allowParallelSyncs);
    this.appContext = context;
  }

  private int getStatus(String cmstId, Context appContext) {
    int statusVal = CacheConstants.SYNC_NOTEXIST;
    // get content from content provider
    String sortOrder = null;
    Uri uri = Contract.SyncStatus.CONTENT_URI;
    String[] selectionArgs = { cmstId };
    String selection = Contract.Albums.CMST_ID + " =?";
    String[] projection = { Contract.SyncStatus.STATUS, Contract.SyncStatus.CMST_ID };
    Cursor cursor = appContext.getContentResolver().query(uri, projection, selection,
        selectionArgs, sortOrder);
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        statusVal = cursor.getInt(cursor.getColumnIndex(Contract.SyncStatus.STATUS));
      }
      cursor.close();
    }

    return statusVal;
  }
  
  @Override
  public void onPerformSync(Account account, Bundle extras, String authority,
      ContentProviderClient provider, SyncResult syncResult) {
    Log.d(TAG, "started sync");
    // retrieve the parameters passed
    String cmstId = extras.getString(CacheConstants.KEY_CMST_ID);
    long sessionId = extras.getLong(CacheConstants.KEY_SESSION_ID);
    String cmstIp = extras.getString(CacheConstants.KEY_CMST_IP);
    int syncType = extras.getInt(CacheConstants.KEY_SYNC_TYPE);
    AlbumList albums = null;
    AlbumItems albumItems = null;

    int totalItemsTobDownloaded = 0;
    int downloadedItems = 0;
    long startTime = System.currentTimeMillis();
    int syncStatus = syncType;

    // if (CacheConstants.SYNC_FRESH == syncType) {

    int stat = getStatus(cmstId, appContext);
    if ((stat == CacheConstants.SYNC_FAILURE) || (stat == CacheConstants.SYNC_NOTEXIST)) {
      // fresh sync
      try {
        albums = getAlbumsList(cmstIp, sessionId);
        if (albums.getAlbmThmbList() != null) {
          Log.e(TAG, "Albumns size :" + albums.getAlbmThmbList().size());
          totalItemsTobDownloaded += albums.getAlbmThmbList().size();
          for (AlbumDetails a : albums.getAlbmThmbList()) {

            downloadedItems++;
            downloadAlbumThumbnail(a, cmstIp, cmstId);
            File dir = a.getDir();
            albumItems = getAlbumItems(cmstIp, sessionId, a.getAlbmId());
            Log.e(TAG, "&&&&&&&&&& > " + albumItems.getListOfItems());
            if (albumItems.getListOfItems() != null) {
              Log.e(TAG, "Albumns size :" + albumItems.getListOfItems().size());
              totalItemsTobDownloaded += albumItems.getListOfItems().size();
              for (AlbumItemDetails item : albumItems.getListOfItems()) {

                downloadedItems++;
                downloadAlbumItem(item, cmstIp, dir, a.getAlbmId());
              }
              // Log.d(TAG, "albums obsolute path :"
              // +a.getDir().getAbsolutePath());
              insertAlbumRowToAlbumsTable(provider, a, cmstId, albumItems.getListOfItems().size());
              serializeAndStore(dir, a.getAlbmId() + ".ser", albumItems);
            }

          }
          File cmstDir = new File(appContext.getFilesDir() + File.separator + cmstId);
          serializeAndStore(cmstDir, cmstId + ".ser", albums);

          Log.e(TAG, ">>>>>>>&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& Total items vers Doenloaded"
              + totalItemsTobDownloaded + " and " + downloadedItems);
        }
        syncStatus = CacheConstants.SYNC_FRESH;
        notifySyncComplete(syncStatus);

      } catch (CmstAPIException e) {
        updateSyncStatus(provider, CacheConstants.SYNC_FAILURE, cmstId);
        e.printStackTrace();
        notifySyncComplete(CacheConstants.SYNC_FAILURE);
      } catch (NullPointerException e) {
        updateSyncStatus(provider, CacheConstants.SYNC_FAILURE, cmstId);
        e.printStackTrace();
        notifySyncComplete(CacheConstants.SYNC_FAILURE);
      }
    } else {
      Log.d(TAG, "Resync running....");
      try {
        notifySyncComplete(syncStatus);
        reSyncAlbumList(provider, cmstIp, cmstId, sessionId);
        updateAlbumList(cmstIp, cmstId, sessionId);
       
      } catch (CmstAPIException e) {
        notifySyncComplete(CacheConstants.SYNC_FAILURE);
      }

    }

    updateSyncStatus(provider, CacheConstants.SYNC_SUCCESS, cmstId);

    long endTime = System.currentTimeMillis();
    long timeTaken = endTime - startTime;
    Log.i(TAG, "Time taken to complete the sync is : " + timeTaken);
    Log.e(TAG, "Sync complete");
    // send notification to listeners
   
    // displayAlbumList(provider);
  }

  private void updateAlbumList(String cmstIp, String cmstId, long sessionId)
      throws CmstAPIException {
    AlbumList albums = getAlbumsList(cmstIp, sessionId);
    File cmstDir = new File(appContext.getFilesDir() + File.separator + cmstId);
    try {
      for (AlbumDetails item : albums.getAlbmThmbList()) {
        item.setSmallThumbnailUrl(item.getTpath());
        item.setLargeThumbnailUrl(prepareLargeThumbnailUrl(item.getTpath()));
        item.setSmallThumbnailFileName(extractFilenameFromUrl(item.getTpath()));
        File dir = new File(appContext.getFilesDir() + File.separator + cmstId + File.separator
            + item.getAlbmId());
        Log.e(TAG, "Large thumgbnail url : " + item.getLargeThumbnailUrl());
        item.setLargeThumbnailFileName(extractFilenameFromUrl(item.getLargeThumbnailUrl()));
        item.setDir(dir);
      }
      serializeAndStore(cmstDir, cmstId + ".ser", albums);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private void syncAddedAlbums(ContentProviderClient provider, String cmstIp, String cmstId,
      long sessionId, ArrayList<AlbumDetails> addalbmLst) throws CmstAPIException {
    Log.d(TAG, "Syncng " + addalbmLst.size() + " newly added albums");
    try {

      for (AlbumDetails resyncAlbumData : addalbmLst) {
        AlbumItems albumItems = getAlbumItems(cmstIp, sessionId, resyncAlbumData.getAlbmId());

        downloadAlbumThumbnail(resyncAlbumData, cmstIp, cmstId);
        File dir = resyncAlbumData.getDir();
        if (albumItems.getListOfItems() != null) {
          for (AlbumItemDetails item : albumItems.getListOfItems()) {
            downloadAlbumItem(item, cmstIp, dir, resyncAlbumData.getAlbmId());
          }
          // TODO: fetch album info and update the table
          // insertAlbumRowToAlbumsTable(provider, a,
          // cmstId, albumItems.getListOfItems()
          // .size());
          serializeAndStore(dir, resyncAlbumData.getAlbmId() + ".ser", albumItems);
        }

      }

    } catch (NullPointerException e) {
      e.printStackTrace();
      e.printStackTrace();
    }

  }

  private void notifySyncComplete(int syncFailure) {
    Intent notifyIntent = new Intent(CacheConstants.SYNC_FINISHED_ACTION);
    notifyIntent.putExtra(CacheConstants.EXTRA_SYNC_STATUS, syncFailure);
    appContext.sendBroadcast(notifyIntent);
  }

  private void insertAlbumRowToAlbumsTable(ContentProviderClient provider, AlbumDetails a,
      String cmstId, int itemsCount) {
    ContentValues values = new ContentValues();
    values.put(Albums.ALBUM_ID, a.getAlbmId());
    values.put(Albums.CMST_ID, cmstId);
    values.put(Albums.ITEMS_COUNT, itemsCount);
    values.put(Albums.NAME, a.getAlbmName());
    values.put(Albums.JSON_PATH, a.getDir().getPath() + "/" + a.getAlbmId() + ".ser");
    try {
      provider.insert(Albums.CONTENT_URI, values);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  private void updateSyncStatus(ContentProviderClient provider, int status, String cmstId) {
    ContentValues values = new ContentValues();
    values.put(SyncStatus.STATUS, status);
    String selection = SyncStatus.CMST_ID + "=?";
    String[] selectionArgs = { cmstId };
    int affectedRows;
    try {
      affectedRows = provider.update(SyncStatus.CONTENT_URI, values, selection, selectionArgs);
      if (0 == affectedRows) {
        values.put(SyncStatus.CMST_ID, cmstId);
        provider.insert(SyncStatus.CONTENT_URI, values);
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    }

  }

  private void downloadAlbumThumbnail(AlbumDetails a, String cmstIp, String cmstId) {
    a.setSmallThumbnailUrl(a.getTpath());
    a.setLargeThumbnailUrl(prepareLargeThumbnailUrl(a.getTpath()));
    a.setSmallThumbnailFileName(extractFilenameFromUrl(a.getTpath()));
    a.setLargeThumbnailFileName(extractFilenameFromUrl(a.getLargeThumbnailUrl()));
    String smallTUrl = cmstIp + "/" + a.getSmallThumbnailUrl();
    String largeTUrl = cmstIp + "/" + a.getLargeThumbnailUrl();
    File dir = new File(appContext.getFilesDir() + File.separator + cmstId + File.separator
        + a.getAlbmId());
    ImageUtil.downloadImageFromServerAndSaveToInternalStorage(appContext, smallTUrl,
        a.getSmallThumbnailFileName(), dir);
    ImageUtil.downloadImageFromServerAndSaveToInternalStorage(appContext, largeTUrl,
        a.getLargeThumbnailFileName(), dir);
    a.setDir(dir);
  }

  private void downloadAlbumItem(AlbumItemDetails item, String cmstIp, File albumDir, int albumId) {
    Log.d(TAG, ">>>>>>>>>>>item path :" + item.getTpath());

    item.setSmallThumbnailUrl(item.getTpath());
    item.setLargeThumbnailUrl(prepareLargeThumbnailUrl(item.getTpath()));
    item.setSmallThumbnailFileName(extractFilenameFromUrl(item.getTpath()));

    Log.e(TAG, "Large thumgbnail url : " + item.getLargeThumbnailUrl());
    item.setLargeThumbnailFileName(extractFilenameFromUrl(item.getLargeThumbnailUrl()));

    item.setAlbmId(albumId);

    String itemSmallTUrl = cmstIp + "/" + item.getSmallThumbnailUrl();
    String itemLargeTUrl = cmstIp + "/" + item.getLargeThumbnailUrl();
    ImageUtil.downloadImageFromServerAndSaveToInternalStorage(appContext, itemSmallTUrl,
        item.getSmallThumbnailFileName(), albumDir);
    ImageUtil.downloadImageFromServerAndSaveToInternalStorage(appContext, itemLargeTUrl,
        item.getLargeThumbnailFileName(), albumDir);
    item.setDir(albumDir);
  }

  private String prepareLargeThumbnailUrl(String tpath) {
    String largePath = tpath.replace("thms", "thml");
    return largePath;
  }

  private String extractFilenameFromUrl(String url) {
    Log.e(TAG, ">>URL" + url);
    int index = url.lastIndexOf("/");
    int length = url.length();
    String fileName = url.substring(index + 1, length);
    return fileName;
  }

  private boolean serializeAndStore(File dir, String fileName, Object object) {
    if (!dir.exists()) {
      dir.mkdirs();
    }
    File tempFile = new File(dir, fileName);
    if (!tempFile.exists()) {
      try {
        tempFile.createNewFile();
        Log.e(TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<.ser file created");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
      ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
      out.writeObject(object);
      out.close();
      fileOutputStream.close();
      return true;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private int getAlbumsCount(String cmstIp, long sessionId) throws CmstAPIException {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();

    try {
      String sessionIdUrlEnc = URLEncoder.encode(sessionId + "", "UTF-8");
      String path = cmstIp + CacheConstants.API_GET_ALBUM_COUNT + sessionIdUrlEnc;
      Log.d(TAG, "Server Path -> " + path);
      URL url = new URL(path);
      URLConnection conn = url.openConnection();
      AlbumCountResponse response = gson.fromJson(new InputStreamReader(conn.getInputStream()),
          AlbumCountResponse.class);
      if (response.getRes() != CacheConstants.SUCCESS) {
        throw new CmstAPIException();
      } else {
        return response.getAlbmNum();
      }
    } catch (JsonSyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonIOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    throw new CmstAPIException();
  }

  private AlbumList getAlbumsList(String cmstIp, long sessionId) throws CmstAPIException {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    try {
      int albumCount = getAlbumsCount(cmstIp, sessionId);
      Log.e(TAG, "albums count is " + albumCount);
      String sessionIdUrlEnc = URLEncoder.encode(sessionId + "", "UTF-8");
      String albumsCountUrlEnc = URLEncoder.encode(albumCount + "", "UTF-8");
      String path = cmstIp + CacheConstants.API_GET_ALBUMS_LIST + sessionIdUrlEnc + "&prevNum="
          + albumsCountUrlEnc;
      URL url = new URL(path);
      URLConnection conn = url.openConnection();
      AlbumList response = gson.fromJson(new InputStreamReader(conn.getInputStream()),
          AlbumList.class);
      // Log.d(TAG, "Total downloaded albums :" + response.getRes()
      // + " and " + response.getAlbmThmbList());
      if (response.getRes() != CacheConstants.SUCCESS) {
        throw new CmstAPIException();
      } else {
        return response;
      }
    } catch (JsonSyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonIOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NullPointerException e) {
      // TODO: handle exception
      e.printStackTrace();
    }
    throw new CmstAPIException();
  }

  private int getAlbumItemsCount(String cmstIp, long sessionId, int albumId)
      throws CmstAPIException {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();

    try {
      String sessionIdUrlEnc = URLEncoder.encode(sessionId + "", "UTF-8");
      String albumIdUrlEnc = URLEncoder.encode(albumId + "", "UTF-8");
      String path = cmstIp + CacheConstants.API_GET_ALBUM_ITEMS_COUNT + sessionIdUrlEnc
          + "&albmId=" + albumIdUrlEnc;
      Log.d(TAG, "Server Path -> " + path);
      URL url = new URL(path);
      URLConnection conn = url.openConnection();
      AlbumItemsCountResponse response = gson.fromJson(
          new InputStreamReader(conn.getInputStream()), AlbumItemsCountResponse.class);
      if (response.getRes() != CacheConstants.SUCCESS) {
        throw new CmstAPIException();
      } else {
        return response.getitemNum();
      }
    } catch (JsonSyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonIOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    throw new CmstAPIException();
    /*
     * xhr.open(“GET”, “/api/stg/getitmcnt?sessId=1234567890&albmId=1”); Response { “res”:1000,
     * "itemCnt":3, }
     */
  }

  private AlbumItems getAlbumItems(String cmstIp, long sessionId, int albumId)
      throws CmstAPIException {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    try {
      int albumItemsCount = getAlbumItemsCount(cmstIp, sessionId, albumId);
      Log.e(TAG, "*************albums list items count is " + albumItemsCount);
      String sessionIdUrlEnc = URLEncoder.encode(sessionId + "", "UTF-8");
      String albumIdUrlEnc = URLEncoder.encode(albumId + "", "UTF-8");
      String albumItemsCountUrlEnc = URLEncoder.encode(albumItemsCount + "", "UTF-8");
      String path = cmstIp + CacheConstants.API_GET_ALBUM_ITEMS_LIST + sessionIdUrlEnc
          + "&prevNum=" + albumItemsCountUrlEnc + "&albmId=" + albumIdUrlEnc;
      URL url = new URL(path);
      Log.d(TAG, "album items list :" + path);
      URLConnection conn = url.openConnection();
      AlbumItems response = gson.fromJson(new InputStreamReader(conn.getInputStream()),
          AlbumItems.class);
      if (response.getRes() != CacheConstants.SUCCESS) {
        throw new CmstAPIException();
      } else {
        return response;
      }
    } catch (JsonSyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonIOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    throw new CmstAPIException();
    /*
     * Request xhr.open(“GET”, “/api/stg/getitmthmlstlast?sessId=1234567890&albmId=1&prevNum=4”);
     * 
     * Response { “res”:1000, "baseOffset":3, “albmThmbList”:[ {"itmId":1,
     * "itemRawDate":"1354201200000", "tpath":"./store/content.thms.jpg", "orientation":0 }, ......
     * ] }
     */
  }

  /**
   * SyncAlbumList find the deleted album and delete, find added albums and if modifiedAlbumIds
   * value contains data then concatenate the modified albums with added albumns
   * 
   * */
  private void reSyncAlbumList(ContentProviderClient provider, String cmstIp, String cmstId,
      Long sessionId) throws CmstAPIException {

    AlbumList downloadedAlbumList = getAlbumsList(cmstIp, sessionId);
    try {
      // fetch AlbumList from cache

      ArrayList<AlbumDetails> downloadedAlbumDetailsList = downloadedAlbumList.getAlbmThmbList();

      ArrayList<AlbumDetails> addedAlbumList = new ArrayList<AlbumDetails>();

      // syncDeletedAlbums(cmstIp, cmstId, sessionId, deletedAlbumList);

      for (AlbumDetails resyncAlbumData : downloadedAlbumDetailsList) {

        AlbumItems downloadedAlbumItems = getAlbumItems(cmstIp, sessionId,
            resyncAlbumData.getAlbmId());

        File albmDir = new File(appContext.getFilesDir() + File.separator + cmstId + File.separator
            + resyncAlbumData.getAlbmId());
        Log.d(TAG, "Modified Album " + albmDir);

        if (!albmDir.exists()) {
          Log.d(TAG, "added to album list");
          addedAlbumList.add(resyncAlbumData);
          continue;
        }

        AlbumItems cachedAlbumItems = Cache.getInstance().getAlbumItemDetails(cmstId,
            resyncAlbumData.getAlbmId(), albmDir, appContext);

        // ArrayList<AlbumItemDetails> deletedItemDetails = new ArrayList<AlbumItemDetails>(
        // cachedAlbumItems.getListOfItems());

        // deletedItemDetails.removeAll(downloadedAlbumItems.getListOfItems());

        ArrayList<AlbumItemDetails> modifiedItemDetails = new ArrayList<AlbumItemDetails>(
            downloadedAlbumItems.getListOfItems());

        modifiedItemDetails.removeAll(cachedAlbumItems.getListOfItems());

        // for (AlbumItemDetails item : deletedItemDetails) {
        // Log.d(TAG, "deleted item Album " + item.getDir().getPath() + File.separator +
        // item.getSmallThumbnailFileName());
        // deleteFile(item.getDir().getPath() + File.separator + item.getSmallThumbnailFileName());
        // deleteFile(item.getDir().getPath() + File.separator + item.getLargeThumbnailFileName());
        // }

        for (AlbumItemDetails item : modifiedItemDetails) {
          downloadAlbumItem(item, cmstIp, albmDir, resyncAlbumData.getAlbmId());
        }

        Log.w(TAG, "Modified Item details :" + modifiedItemDetails);
        if (!modifiedItemDetails.isEmpty()) {
          // if modified then only serialize and store
          Log.e(TAG, "Total cached Items are : " + cachedAlbumItems.getListOfItems().size());

          Log.e(TAG, "Total Downloaded Items are : " + downloadedAlbumItems.getListOfItems().size());
          Log.e(TAG, "Total Modified Items are : " + modifiedItemDetails.size());
          prepareAlbumItemsData(downloadedAlbumItems, resyncAlbumData.getAlbmId(), albmDir);
          serializeAndStore(albmDir, resyncAlbumData.getAlbmId() + ".ser", downloadedAlbumItems);
        }
      }

      syncAddedAlbums(provider, cmstIp, cmstId, sessionId, addedAlbumList);

    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  private void prepareAlbumItemsData(AlbumItems downloadedAlbumItems, int albumId, File albumDir) {
    for (AlbumItemDetails item : downloadedAlbumItems.getListOfItems()) {
      item.setSmallThumbnailUrl(item.getTpath());
      item.setLargeThumbnailUrl(prepareLargeThumbnailUrl(item.getTpath()));
      item.setSmallThumbnailFileName(extractFilenameFromUrl(item.getTpath()));

      Log.e(TAG, "Large thumgbnail url : " + item.getLargeThumbnailUrl());
      item.setLargeThumbnailFileName(extractFilenameFromUrl(item.getLargeThumbnailUrl()));

      item.setAlbmId(albumId);
      item.setDir(albumDir);
    }

  }
}
