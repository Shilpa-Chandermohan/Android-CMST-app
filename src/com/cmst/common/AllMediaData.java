package com.cmst.common;

import java.util.ArrayList;

import com.cmst.cache.util.AlbumItemDetails;

public class AllMediaData {
  private static final AllMediaData INSTANCE = new AllMediaData();

  public ArrayList<AlbumItemDetails> allMediaList = new ArrayList<AlbumItemDetails>();

  // Private constructor prevents instantiation from other classes
  private AllMediaData() {

  }

  public static AllMediaData getInstance() {
    return INSTANCE;
  }
}
