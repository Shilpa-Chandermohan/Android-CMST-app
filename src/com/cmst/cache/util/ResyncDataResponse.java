package com.cmst.cache.util;

import java.util.ArrayList;

public class ResyncDataResponse {
  int res;
  int imptStat;

  ArrayList<ResyncAlbumData> addalbmLst;
  ArrayList<ResyncAlbumData> modalbmLst;
  ArrayList<ResyncAlbumData> delalbmLst;
  
  public int getRes() {
    return res;
  }

  public void setRes(int res) {
    this.res = res;
  }

  public int getImptStat() {
    return imptStat;
  }

  public void setImptStat(int imptStat) {
    this.imptStat = imptStat;
  }

  

  public ArrayList<ResyncAlbumData> getAddalbmLst() {
    return addalbmLst;
  }

  public void setAddalbmLst(ArrayList<ResyncAlbumData> addalbmLst) {
    this.addalbmLst = addalbmLst;
  }

  public ArrayList<ResyncAlbumData> getModalbmLst() {
    return modalbmLst;
  }

  public void setModalbmLst(ArrayList<ResyncAlbumData> modalbmLst) {
    this.modalbmLst = modalbmLst;
  }

  public ArrayList<ResyncAlbumData> getDelalbmLst() {
    return delalbmLst;
  }

  public void setDelalbmLst(ArrayList<ResyncAlbumData> delalbmLst) {
    this.delalbmLst = delalbmLst;
  }
}
