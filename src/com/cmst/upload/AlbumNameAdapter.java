package com.cmst.upload;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cmst.cache.util.AlbumDetails;
import com.cmst.cmstapp.R;

public class AlbumNameAdapter extends BaseAdapter {

	private ArrayList<AlbumDetails> albumList;

	private LayoutInflater inflater;

	private static final int HOLDER_KEY=-21;


	public AlbumNameAdapter(LayoutInflater inflater) {
		this.inflater=inflater;
	}

	public ArrayList<AlbumDetails> getAlbumList() {
		return albumList;
	}

	public void setAlbumList(ArrayList<AlbumDetails> albumList) {
		this.albumList = albumList;
		notifyDataSetChanged();
	}

	/**
	 * returns number of albums.
	 */
	@Override
	public int getCount() {

		if(albumList!=null)
		{
			return albumList.size();
		}
		return 0;
	}

	/**
	 * returns AlbumItemDetails object or null (please handle) at a particular position.
	 */
	@Override
	public AlbumDetails getItem(int position) {
		if(albumList!=null)
		{
			return albumList.get(position);
		}
		return null;
	}


	@Override
	public long getItemId(int position) {
		return position;
	}




	/**
	 * 
	 *  Re-cycling is done. 
	 *  
	 *  
	 *  
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) 
		{

			View myView = inflater.inflate(R.layout.list__name_item, parent,false);
			TextView albumName = (TextView)myView.findViewById(R.id.albumNameItem);
			albumName.setText(albumList.get(position).getAlbmName());
			ViewHolder holder=new ViewHolder();
			holder.setAlbumName(albumName);
			myView.setTag(HOLDER_KEY, holder);
			return myView;
		}

		ViewHolder tempHolder = (ViewHolder)convertView.getTag(HOLDER_KEY);
		tempHolder.getAlbumName().setText(albumList.get(position).getAlbmName());
		return convertView;
	}





	private class ViewHolder
	{

		private TextView albumName;


		public TextView getAlbumName() {
			return albumName;
		}
		public void setAlbumName(TextView albumName) {
			this.albumName = albumName;
		}
	}

	public void clear() {
		albumList.clear();
		notifyDataSetChanged();
	}
}
