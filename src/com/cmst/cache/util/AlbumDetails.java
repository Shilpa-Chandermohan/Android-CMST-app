package com.cmst.cache.util;

import java.io.File;
import java.io.Serializable;

public class AlbumDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 139780178210035177L;

	private String albmDate;

	private int albmId;

	private String albmName;

	private String albmRawDate;

	private String tpath;

	private int orientation;

	private int colorPickR;

	private int colorPickG;

	private int colorPickB;

	private File dir;

	private String smallThumbnailFileName;

	private String smallThumbnailUrl;

	private String largeThumbnailUrl;

	private String largeThumbnailFileName;

	@Override
	public String toString() {
		return "AlbumDetails [albmDate=" + albmDate + ", albmId=" + albmId
				+ ", albmName=" + albmName + ", albmRawDate=" + albmRawDate
				+ ", tpath=" + tpath + ", orientation=" + orientation
				+ ", colorPickR=" + colorPickR + ", colorPickG=" + colorPickG
				+ ", colorPickB=" + colorPickB + ", dir=" + dir
				+ ", smallThumbnailFileName=" + smallThumbnailFileName
				+ ", smallThumbnailUrl=" + smallThumbnailUrl
				+ ", largeThumbnailUrl=" + largeThumbnailUrl
				+ ", largeThumbnailFileName=" + largeThumbnailFileName + "]";
	}

	@Override
	public boolean equals(Object object) {
		if (getClass() != object.getClass()) {
			return false;
		}
		AlbumDetails albumDetails = (AlbumDetails) object;
		if (albmDate == null) {
			if (albumDetails.albmDate != null) {
				return false;
			}
		} else if (!albmDate.equalsIgnoreCase(albumDetails.albmDate)){
			return false;
		} 
		if(albmId != albumDetails.getAlbmId()) {
		  return false;
		}
		if (albmName == null) {
			if (albumDetails.albmName != null){
				return false;
			}
		} else if (!albmName.equalsIgnoreCase(albumDetails.albmName)){
			return false;
		}
		if (albmRawDate == null) {
			if (albumDetails.albmRawDate != null){
				return false;
			}
		} else if (!albmRawDate.equalsIgnoreCase(albumDetails.albmRawDate)){
			return false;
		}
		if (tpath == null) {
			if (albumDetails.tpath != null){
				return false;
			}
		} else if (!tpath.equalsIgnoreCase(albumDetails.tpath)){
			return false;
		}
		if (orientation != albumDetails.orientation){
			return false;
		}
		if (colorPickB != albumDetails.colorPickB){
			return false;
		}
		if (colorPickG != albumDetails.colorPickG){
			return false;
		}
		if (colorPickR != albumDetails.colorPickR){
			return false;
		}
		return true;

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

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public int getColorPickR() {
		return colorPickR;
	}

	public void setColorPickR(int colorPickR) {
		this.colorPickR = colorPickR;
	}

	public int getColorPickG() {
		return colorPickG;
	}

	public void setColorPickG(int colorPickG) {
		this.colorPickG = colorPickG;
	}

	public int getColorPickB() {
		return colorPickB;
	}

	public void setColorPickB(int colorPickB) {
		this.colorPickB = colorPickB;
	}

	public String getAlbmDate() {
		return albmDate;
	}

	public void setAlbmDate(String albmDate) {
		this.albmDate = albmDate;
	}

	public int getAlbmId() {
		return albmId;
	}

	public void setAlbmId(int albmId) {
		this.albmId = albmId;
	}

	public String getAlbmName() {
		return albmName;
	}

	public void setAlbmName(String albmName) {
		this.albmName = albmName;
	}

	public String getAlbmRawDate() {
		return albmRawDate;
	}

	public void setAlbmRawDate(String albmRawDate) {
		this.albmRawDate = albmRawDate;
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

}
