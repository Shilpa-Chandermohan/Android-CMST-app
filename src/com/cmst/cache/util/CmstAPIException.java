package com.cmst.cache.util;

public class CmstAPIException extends Exception {
	private static String message = "SyncAbortedException";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public CmstAPIException() {
		super(message);
	}

}
