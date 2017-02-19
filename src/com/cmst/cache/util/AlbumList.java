package com.cmst.cache.util;

import java.io.Serializable;
import java.util.ArrayList;

public class AlbumList implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5148499165592462049L;

	private int res;
	
	private int baseOffset;
	
	private ArrayList<AlbumDetails> albmThmbList;
	
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

	public ArrayList<AlbumDetails> getAlbmThmbList() {
		return albmThmbList;
	}

	public void setAlbmThmbList(ArrayList<AlbumDetails> albmThmbList) {
		this.albmThmbList = albmThmbList;
	}

	
}
