package com.shimaging;

import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * <p>Defines the interface by which ImageModel obtains Images.</p>
 * 
 * <p>ImageSources may contain multiple images, retrievable by indexing from 
 * zero to the value of <code>getImageCount()</code>. Multi-page TIFF images, 
 * animated GIFs, etc. may be examples of ImageSources that would contain more
 * than one image. There is also a method for getting a smaller image to display
 * as a thumbnail. This feature is currently unused, but it's there.</p>
 * 
 * @author Bryan Varner
 */
public interface ImageSource {
	/**
	 * Retrieve the image at the given index.
	 * <p>If there is no image at the specified index, implementations should 
	 * return <code>null.</code></p>
	 * @param index The image to obtain from this ImageSource.
	 * @return a BufferedImage.
	 */
	public BufferedImage getImage(final int index);
	
	
	/**
	 * Retrieve a thumbnail of the image at the given index.
	 * <p>If there is no image at the specified index, or if we do not support
	 * thumbnails, implementations should return <code>null</code></p>
	 * @param index The image to obtain a thumbnail for from this ImageSource.
	 * @return a BufferedImage.
	 */
	public Image getThumbnail(final int index);
	
	
	/**
	 * Gets the number of Images in this ImageSource.
	 * @return THe number of images in this ImageSource. Normally this will be 1.
	 */
	public int getImageCount();
	
	
	/**
	 * Gets the name of this Image
	 */
	public String getImageName();
	
	/**
	 * Release any cached resources. This method signals that the ImageSource 
	 * will not be used again.
	 */
	public void dispose();
}
