package com.cmst.common;

import java.util.ArrayList;

import com.cmst.cache.util.AlbumDetails;

public class AlbumData {
  private static final AlbumData INSTANCE = new AlbumData();

  public ArrayList<AlbumDetails> albumList = new ArrayList<AlbumDetails>();

  // Private constructor prevents instantiation from other classes
  private AlbumData() {

  }

  public static AlbumData getInstance() {
    return INSTANCE;
  }
}
