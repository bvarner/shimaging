package com.shimaging.source;

import com.shimaging.ImageSource;

import java.awt.Image;
import java.awt.image.BufferedImage;


/**
 * Implements a default method of behavior for ImageSources to generate Thumbnails.
 * 
 * By default, the thumbnail size is 100 x 200. This can be changed by the
 * alternate constructor.
 * 
 * @author Bryan.Varner
 */
public abstract class DefaultThumbnailSource implements ImageSource {
	int thumbWidth;
	int thumbHeight;
	
	/**
	 * Constructs a DefaultThumbnailSource that generates Thumbnails 
	 * 100x200 pixels.
	 */
	protected DefaultThumbnailSource() {
		this(100, 200);
	}
	
	
	/**
	 * Constructs a DefaultThumbnailSource that generates Thumbnails with 
	 * the specified dimensions
	 *
	 * @param thumbWidth the width of generated thumbnails
	 * @param thumbHeight the height of generated thumbnails
	 */
	protected DefaultThumbnailSource(int thumbWidth, int thumbHeight) {
		this.thumbWidth = thumbWidth;
		this.thumbHeight = thumbHeight;
	}
	
	/**
	 * Implements ImageSource
	 */
	public abstract BufferedImage getImage(int index);
	
	/**
	 * Returns a fast-scaled instance of getImage(index) the size of 
	 * thumbWidth x thumbHeight.
	 * @param index The index of the image to retrieve.
	 */
	public Image getThumbnail(int index) {
		return getImage(index).getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_FAST);
	}
	
	/**
	 * Implements ImageSource
	 */
	public abstract int getImageCount();
	
	/**
	 * Implements ImageSource
	 */
	public abstract void dispose();
	
	
	/**
	 * Implements ImageSource
	 */
	public abstract String getImageName();
}
