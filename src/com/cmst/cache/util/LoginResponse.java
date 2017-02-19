package com.cmst.cache.util;

public class LoginResponse {

	private int res;
	
	private long sessId;

	public int getRes() {
		return res;
	}

	public void setRes(int res) {
		this.res = res;
	}

	public long getSessId() {
		return sessId;
	}

	public void setSessId(long sessId) {
		this.sessId = sessId;
	}

	@Override
	public String toString() {
		return "LoginResponse [res=" + res + ", sessId=" + sessId + "]";
	}
	
	
}
