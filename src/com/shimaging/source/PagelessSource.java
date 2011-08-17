package com.shimaging.source;

import java.awt.image.BufferedImage;

/**
 * A PagelessSource is an ImageSource that "fakes" having more than one page.
 * The number of pages 'faked' can be set during or post-construction.
 */
public abstract class PagelessSource extends DefaultThumbnailSource {
	int pages = 0;
	
	protected PagelessSource(final int fakePages) {
		pages = fakePages;
	}
	
	
	public void setFakePages(final int fakePages) {
		this.pages = fakePages;
	}
	
	public BufferedImage getImage(final int index) {
		return getImage();
	}
	
	public int getImageCount() {
		return pages;
	}
	
	
	public abstract void dispose();
	
	public abstract BufferedImage getImage();
	
	public abstract String getImageName();
}