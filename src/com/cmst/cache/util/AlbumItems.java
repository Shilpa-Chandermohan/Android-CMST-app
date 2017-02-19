package com.cmst.cache.util;

import java.io.Serializable;
import java.util.ArrayList;

public class AlbumItems implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 489860268287125811L;

	private int res;
	
	private int baseOffset;
	
	private ArrayList<AlbumItemDetails> itemThmbList;
	
	public int getRes() {
		return res;
	}
	public void setRes(int res) {
		this.res = res;
	}
	public int getBaseOffset() {
		return baseOffset;
	}
	public void setBaseOffset(int baseOffset) {
		this.baseOffset = baseOffset;
	}
	public ArrayList<AlbumItemDetails> getListOfItems() {
		return itemThmbList;
	}
	public void setListOfItems(ArrayList<AlbumItemDetails> listOfItems) {
		this.itemThmbList = listOfItems;
	}
	
}
