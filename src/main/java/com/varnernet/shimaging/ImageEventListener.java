package com.varnernet.shimaging;

/** 
 * Interface that objects interested in receiving Image-level events should
 * implement.
 */
public interface ImageEventListener {
	/**
	 * The Image state has changed, and the view should be updated.
	 */
	public void imageChanged(final ImageEvent ie);
	
	
	/**
	 * The Image Source, or Model had an error, and the user should be 
	 * notified.
	 */
	public void imageError(final ImageEvent ie);
}