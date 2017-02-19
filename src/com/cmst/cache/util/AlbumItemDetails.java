package com.cmst.cache.util;

import java.io.File;
import java.io.Serializable;

public class AlbumItemDetails implements Serializable {

  /**
	 * 
	 */
  private static final long serialVersionUID = -1590171369945030498L;

  private int itemId;

  private int albmId;

  private String itemRawDate;

  private String tpath;

  private int orientation;

  private File dir;

  private String smallThumbnailFileName;

  private String smallThumbnailUrl;

  private String largeThumbnailUrl;

  private String largeThumbnailFileName;

  private String fileType;
  private String fileName;

  public String getFileType() {
    return fileType;
  }

  public void setFileType(String type) {
    this.fileType = type;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String name) {
    this.fileName = name;
  }

  public int getAlbmId() {
    return albmId;
  }

  public void setAlbmId(int albmId) {
    this.albmId = albmId;
  }

  public File getDir() {
    return dir;
  }

  public void setDir(File dir) {
    this.dir = dir;
  }

  public String getSmallThumbnailFileName() {
    return smallThumbnailFileName;
  }

  public void setSmallThumbnailFileName(String smallThumbnailFileName) {
    this.smallThumbnailFileName = smallThumbnailFileName;
  }

  public String getSmallThumbnailUrl() {
    return smallThumbnailUrl;
  }

  public void setSmallThumbnailUrl(String smallThumbnailUrl) {
    this.smallThumbnailUrl = smallThumbnailUrl;
  }

  public String getLargeThumbnailUrl() {
    return largeThumbnailUrl;
  }

  public void setLargeThumbnailUrl(String largeThumbnailUrl) {
    this.largeThumbnailUrl = largeThumbnailUrl;
  }

  public String getLargeThumbnailFileName() {
    return largeThumbnailFileName;
  }

  public void setLargeThumbnailFileName(String largeThumbnailFileName) {
    this.largeThumbnailFileName = largeThumbnailFileName;
  }

  public int getItemId() {
    return itemId;
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
  }

  public String getItemRawDate() {
    return itemRawDate;
  }

  public void setItemRawDate(String itemRawDate) {
    this.itemRawDate = itemRawDate;
  }

  public String getTpath() {
    return tpath;
  }

  public void setTpath(String tpath) {
    this.tpath = tpath;

    /**
     * save /contents.thmsl.jpg
     * 
     */
    setSmallThumbnailUrl(tpath);

    /**
     * change it to /contents.thml.jpg
     * 
     */
    String largePath = tpath.replace("thms", "thml");
    ;
    setLargeThumbnailUrl(largePath);

    /**
     * save the small file name
     */
    int index = tpath.lastIndexOf("/");
    int length = tpath.length();
    String smallFileName = tpath.substring(index + 1, length);
    setSmallThumbnailFileName(smallFileName);

    /**
     * save the large file name
     */
    int indexl = largePath.lastIndexOf("/");
    int lengthl = largePath.length();
    String largeFileName = largePath.substring(indexl + 1, lengthl);
    setSmallThumbnailFileName(largeFileName);
  }

  public int getOrientation() {
    return orientation;
  }

  public void setOrientation(int orientation) {
    this.orientation = orientation;
  }
  
  
  @Override
  public int hashCode() {
    return 10;
  }

  @Override
  public boolean equals(Object object) {

    if (getClass() != object.getClass()) {
      return false;
    }
    AlbumItemDetails albumDetails = (AlbumItemDetails) object;
    if (itemRawDate == null) {
      if (albumDetails.itemRawDate != null) {
        return false;
      }
    } else if (!itemRawDate.equalsIgnoreCase(albumDetails.itemRawDate)) {
      return false;
    }
    if (tpath == null) {
      if (albumDetails.tpath != null) {
        return false;
      }
    } else if (!tpath.equalsIgnoreCase(albumDetails.tpath)) {
      return false;
    }
    if (orientation != albumDetails.orientation) {
      return false;
    }
    if (itemId != albumDetails.itemId) {
      return false;
    }
    return true;

  }

}
