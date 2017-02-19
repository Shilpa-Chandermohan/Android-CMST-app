package com.cmst.upload;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cmst.cmstapp.R;

public class CustomImageAdapter extends  CursorAdapter implements OnClickListener{

	private static final String TAG=CustomImageAdapter.class.getSimpleName();

	private Context appContext;

	private LayoutInflater inflater;

	private static final int TASK_KEY=-21;

	private static final int VIEW_HOLDER_KEY=-31;

	private static final int PATH_KEY=-45;

	private  ArrayList<String> listOfSelectedItemPaths=new ArrayList<String>();




	public ArrayList<String> getListOfSelectedItemPaths() {
		return listOfSelectedItemPaths;
	}



	public CustomImageAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		this.appContext=context;
		inflater=(LayoutInflater)this.appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}



	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		Log.d(TAG, "newView");
		RelativeLayout frame = (RelativeLayout)inflater.inflate(R.layout.image_box, parent, false);
		ImageView imgView=(ImageView)frame.findViewById(R.id.imageView);
		CheckBox checkBox=(CheckBox)frame.findViewById(R.id.checkBox);
		checkBox.setOnClickListener(this);
		ViewHolder viewHolder=new ViewHolder();
		viewHolder.setImgView(imgView);
		viewHolder.setCheckBox(checkBox);
		frame.setTag(VIEW_HOLDER_KEY, viewHolder);
		return frame;

	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		RelativeLayout tempFrame=(RelativeLayout)view;
		ViewHolder holder=(ViewHolder)tempFrame.getTag(VIEW_HOLDER_KEY);
		ImageView tempImageView=holder.getImgView();
		tempImageView.setImageResource(R.drawable.default_thumb);
		CheckBox checkBox=holder.getCheckBox();
		checkBox.setTag(PATH_KEY, cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
		if(listOfSelectedItemPaths.contains(checkBox.getTag(PATH_KEY)))
		{
			checkBox.setChecked(true);
		}
		else
		{
			checkBox.setChecked(false);
		}
		cancelOldTask(tempImageView);
		BitmapBackgroundWorker worker=new BitmapBackgroundWorker(tempImageView,cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)),cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)),ImageUploadConstants.THUMBNAIL_WIDTH,ImageUploadConstants.THUMBNAIL_HEIGHT);
		tempImageView.setTag(TASK_KEY, worker);
		worker.execute(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
	}

	private void cancelOldTask(ImageView imgView) 
	{

		BitmapBackgroundWorker oldTask=(BitmapBackgroundWorker) imgView.getTag(TASK_KEY);
		if(oldTask!=null)
		{
			oldTask.cancel(true);
		}
	}


	private class ViewHolder{

		private ImageView imgView;

		private CheckBox checkBox;

		public ImageView getImgView() {
			return imgView;
		}

		public void setImgView(ImageView imgView) {
			this.imgView = imgView;
		}

		public CheckBox getCheckBox() {
			return checkBox;
		}

		public void setCheckBox(CheckBox checkBox) {
			this.checkBox = checkBox;
		}

	}


	private class BitmapBackgroundWorker extends AsyncTask<String, Void, Bitmap>
	{
		private WeakReference<ImageView> weakRefOfImageView;

		private int imageWidth;

		private int imageHeight;

		private int outWidth;

		private int outHeight;

		public BitmapBackgroundWorker(ImageView imgView, int imageWidth, int imageHeight, int outWidth, int outHeight) {
			weakRefOfImageView=new WeakReference<ImageView>(imgView);
			this.imageWidth=imageWidth;
			this.imageHeight=imageHeight;
			this.outWidth=outWidth;
			this.outHeight=outHeight;
		}

		@Override
		protected Bitmap doInBackground(String... params) {

			String imageName=params[0];
			return ImageUtil.getBitmap(appContext,imageName, imageWidth, imageHeight, outWidth, outHeight);
		}

		protected void onPostExecute(Bitmap bmp)
		{
			if(this.isCancelled())
			{
				bmp=null;
			}
			if(bmp!=null && weakRefOfImageView!=null)
			{
				if(weakRefOfImageView.get()!=null)
				{
					weakRefOfImageView.get().setImageBitmap(bmp);
				}

			}
		}

	}


	@Override
	public void onClick(View v) {

		CheckBox box=(CheckBox)v;
		if(box.isChecked())
		{
			listOfSelectedItemPaths.add((String) box.getTag(PATH_KEY));
		}
		else
		{
			listOfSelectedItemPaths.remove(box.getTag(PATH_KEY));
		}
	}

}
