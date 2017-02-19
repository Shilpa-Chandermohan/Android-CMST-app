package com.cmst.upload;

import com.cmst.cache.util.AlbumDetails;

public interface AlbumSelectionListener {

  public void albumClicked(AlbumDetails album);
  
  public void albumSelectionCancelled();

}
