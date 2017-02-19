package com.cmst.upload;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.cmst.cache.util.AlbumDetails;
import com.cmst.cmstapp.R;
import com.cmst.common.Constants;

public class GalleryPickerActivity extends FragmentActivity implements OnClickListener, LoaderManager.LoaderCallbacks<Cursor>,AlbumSelectionListener{


  public GridView imageGridView;

  private Button doneButton;

  private CustomImageAdapter imageAdapter;

  private AlbumListPop popup;

  private static final String TAG=GalleryPickerActivity.class.getSimpleName();


  @Override
  public void onCreate(Bundle savedInstanceState) 
  {

    super.onCreate(savedInstanceState);

    setContentView(R.layout.custom_grid_layout);

    imageGridView = (GridView) findViewById(R.id.gridView);

    doneButton= (Button) findViewById(R.id.doneButton);

    doneButton.setOnClickListener(this);

    imageAdapter=new CustomImageAdapter(getApplicationContext(), null, false);

    imageGridView.setAdapter(imageAdapter);

    getSupportLoaderManager().initLoader(1, null, GalleryPickerActivity.this);


  }


  @Override
  public void onClick(View v) {
    popup=new AlbumListPop();
    popup.show(getSupportFragmentManager(), "Dialog");
  }


  @Override
  public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

    final String[] columns = { MediaStore.Images.Media._ID,MediaStore.Images.Media.DATA,MediaStore.Images.Media.WIDTH,MediaStore.Images.Media.HEIGHT };
    final String orderBy = MediaStore.Images.Media._ID;

    Log.d(TAG, "onCreateLoader func");
    //displaying only camera images
    //		String where = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    //				+ " LIKE ?";
    //		String[] inputArgs = { "Camera" };

    return new CursorLoader(getApplicationContext(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);

  }


  @Override
  public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

    Log.d(TAG, "Cursor Load Finished.. Cursor len -> "+cursor.getCount());
    imageAdapter.changeCursor(cursor);
  }


  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {

    Log.d(TAG, "Cursor Load RESET..");
    imageAdapter.changeCursor(null);

  }


  @Override
  public void albumClicked(AlbumDetails album) {

    if(popup!=null)
    {
      popup.dismiss();
    }

    ArrayList<String> listOfItems = imageAdapter.getListOfSelectedItemPaths();
    if(listOfItems.size()<=0)
    {
      Toast.makeText(getApplicationContext(), "Please select items", Toast.LENGTH_SHORT).show();
      return;
    }

    Log.d(TAG, "List of selected items Size -> "+listOfItems.size());
    Intent uploadService=new Intent(getApplicationContext(),ImageUploaderService.class);
    uploadService.putStringArrayListExtra(ImageUploadConstants.EXTRA_SELECTED_ITEMS_KEY, listOfItems);
    uploadService.putExtra(ImageUploadConstants.EXTRA_ALBUM_ID_KEY, String.valueOf(album.getAlbmId()));
    uploadService.putExtra(ImageUploadConstants.EXTRA_ALBUM_RAWDATE, String.valueOf(album.getAlbmRawDate()));
    uploadService.putExtra(ImageUploadConstants.EXTRA_CMST_IP, Constants.ipAddress);
    uploadService.putExtra(ImageUploadConstants.EXTRA_SESSION_ID, Constants.sessionId);
    startService(uploadService);
  }


  @Override
  public void albumSelectionCancelled() {

  }


}