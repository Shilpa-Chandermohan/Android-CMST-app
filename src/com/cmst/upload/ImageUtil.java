package com.cmst.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;


public class ImageUtil {

//	private static final String TAG=ImageUtil.class.getSimpleName();

	

	/**
	 * 
	 * Please don't call this method from UI thread<br/><br/>
	 * 
	 * 
	 * @param path
	 * @param imageWidth
	 * @param imageHeight
	 * @param outWidth
	 * @param outHeight
	 * @return
	 * 	
	 * 		Bitmap Object or null
	 */
	public static final Bitmap getBitmap(Context appContext,String imagePath,int imageWidth, int imageHeight, int outWidth,int outHeight)
	{

		FileInputStream fis=null;
		try 
		{
			File imgFile=new File(imagePath);
			fis=new FileInputStream(imgFile);
			Options options=new Options();
			options.inJustDecodeBounds=false;
			options.inSampleSize=getSampleSize(imageWidth,imageHeight,outWidth,outHeight);
			return BitmapFactory.decodeStream(fis, null, options);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		finally{
			if(fis!=null)
			{
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

	/**
	 * 
	 * @param imageWidth
	 * @param imageHeight
	 * @param outWidth
	 * @param outHeight
	 * @return
	 * 
	 * 		integer
	 */
	private static int getSampleSize(int imageWidth, int imageHeight,
			int outWidth, int outHeight) {

		int sampleSize=1;


		if(outWidth<imageWidth || outHeight<imageHeight)
		{
			int halfImageWidth=imageWidth/2;
			int halfImageHeight=imageHeight/2;

			while(((halfImageWidth/sampleSize)>outWidth)&&((halfImageHeight/sampleSize)>outHeight))
			{
				sampleSize*=2;
			}

		}
		return sampleSize;
	}
	
}
