package com.cmst.cache.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.cmst.cache.provider.Contract;

/**
 * It is a singleton class 
 */
public class Cache  {

	private static Cache cacheObject = null;

	private static final String TAG=Cache.class.getSimpleName();


	public static synchronized Cache getInstance(){

		if(cacheObject == null){
			cacheObject = new Cache();
		}
		return cacheObject;
	}

	/**
	 * 
	 *  Please don't call in UI Thread
	 * 
	 * @param cmstId
	 * @param appContext
	 * @return
	 * 
	 *   AlbumList object or null
	 */
	public AlbumList getListOfAlbums(String cmstId,Context appContext){
		AlbumList albumPojo =null;

		//get content from content provider
		String sortOrder = null;
		Uri uri = Contract.Albums.CONTENT_URI;
		String[] selectionArgs = {cmstId};
		String selection = Contract.Albums.CMST_ID+" =?";
		String[] projection = {Contract.Albums.JSON_PATH,Contract.Albums.CMST_ID};
//		Cursor cursor = appContext.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

//		if(cursor!=null){
			//Obtain the file and error handling
			String fileName = cmstId+".ser";

			//fill the albumDetails object and add to listOfAlbums
			try {
				File dir=appContext.getFilesDir();
				File albumListFile=new File(dir.getPath() + File.separator + cmstId, fileName);

				FileInputStream fis =new FileInputStream(albumListFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
				albumPojo = (AlbumList) ois.readObject();
				ois.close();
				fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
//			cursor.close();
//		}
		return albumPojo;
	}

	/**
	 * 
	 *  Please don't call in UI Thread
	 * 
	 * 
	 * @param cmstId
	 * @param album_id
	 * @param appContext
	 * @return
	 * 
	 * 		null or AlbumItems object
	 */
	public AlbumItems getAlbumItemDetails(String cmstId, int album_id, File albmDir, Context appContext){

		AlbumItems albumPojo =null;

		//get content from content provider
		String sortOrder = null;
		Uri uri = Contract.Albums.CONTENT_URI;
		String[] selectionArgs = {cmstId,String.valueOf(album_id)};
		String selection = Contract.Albums.CMST_ID+" =? "+" AND "+ Contract.Albums.ALBUM_ID+"=?";
		String[] projection = {Contract.Albums.JSON_PATH,Contract.Albums.CMST_ID,Contract.Albums.ALBUM_ID};
//		Cursor cursor = appContext.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

//		if(cursor!=null){

			String fileName = album_id+".ser";

			//fill the albumDetails object and add to listOfAlbums
			try {
				File albumListFile=new File(albmDir, fileName);
				FileInputStream fis =new FileInputStream(albumListFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
				albumPojo = (AlbumItems) ois.readObject();
				ois.close();
				fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
//			cursor.close();
//		}
		return albumPojo;
	}

	/**
	 * Please don't call in UI Thread
	 * 
	 * @param cmstId
	 * @param appContext
	 * @return
	 * 
	 * ArrayList of AlbumItemDetails or null
	 *  	
	 */
	public ArrayList<AlbumItemDetails> getAllMedia(String cmstId,Context appContext)
	{
		// this is final list, which will be returned 
		ArrayList<AlbumItemDetails> listOfMedia=new ArrayList<AlbumItemDetails>();


		AlbumList albumsContainer = getListOfAlbums(cmstId, appContext);
		if(albumsContainer == null){

			Log.e(TAG,"ALBUM Comtainer IS NULL, SO I WILL return null from getAllMedia ");
			return null;
		}
		ArrayList<AlbumDetails> listOfAlbums = albumsContainer.getAlbmThmbList();
		if(listOfAlbums==null)
		{
			Log.e(TAG,"ALBUM List IS NULL, SO I WILL return null from getAllMedia ");
			return null;
		}
		for(int i=0;i<listOfAlbums.size();i++)
		{
			AlbumDetails tempAlbum=listOfAlbums.get(i);
			AlbumItems albumItemObj = getAlbumItemDetails(cmstId, tempAlbum.getAlbmId(),tempAlbum.getDir(), appContext);
			if(albumItemObj==null){

				Log.e(TAG, "Unable to fetch items of album -> "+tempAlbum.getAlbmName());
			}
			else
			{

				ArrayList<AlbumItemDetails> listOfAlbumDetails = albumItemObj.getListOfItems();
				listOfMedia.addAll(listOfAlbumDetails);
			}
		}
		return listOfMedia;
	}

	/**
	 * 
	 * Please don't call in UI Thread
	 * 
	 * @param cmstId
	 * @param appContext
	 * @return
	 * 
	 * 		Cache.UNABLE_TO_FETCH or Cache.SUCCESS
	 *	@see Cache 
	 */
	public int getStatus(String cmstId,Context appContext){
		int statusVal=CacheConstants.SYNC_NOTEXIST;
		//get content from content provider
		String sortOrder = null;
		Uri uri = Contract.SyncStatus.CONTENT_URI;
		String[] selectionArgs = {cmstId};
		String selection = Contract.Albums.CMST_ID+" =?";
		String[] projection = {Contract.SyncStatus.STATUS,Contract.SyncStatus.CMST_ID};
		Cursor cursor = appContext.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
		if(cursor!=null){
		  if(cursor.moveToFirst()) {
			statusVal=cursor.getInt(cursor.getColumnIndex(Contract.SyncStatus.STATUS));
		  }
		  cursor.close();
		}


		return statusVal;
	}

	public boolean changeAlbumName(String newAlbumName,String cmstId,int album_id,
    Context appContext)
  {

    AlbumList albumListResponse = getListOfAlbums(cmstId, appContext);
    if(albumListResponse!=null)
    {
      if(albumListResponse.getAlbmThmbList()!=null)
      {
        ArrayList<AlbumDetails> list = albumListResponse.getAlbmThmbList();
        for(int i=0;i<list.size();i++)
        {
          if(list.get(i).getAlbmId()==album_id)
          {

            list.get(i).setAlbmName(newAlbumName);

            String fileName = cmstId + ".ser";

            // write it to file
            File dir = appContext.getFilesDir();
            File albumListFile = new File(dir.getPath() + File.separator
                + cmstId, fileName);

            try {
              FileOutputStream fos = new FileOutputStream(albumListFile);

              ObjectOutputStream oos = new ObjectOutputStream(fos);
              oos.writeObject(albumListResponse);
              oos.flush();
              oos.close();
              
              return true;
            }
            catch (FileNotFoundException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }

          }
        }
      }
    }
    return false;
  }


}
