package com.shimaging.source;


import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;

/**
 * An ImageSource that reads images from InputStreams.
 *
 * <p>This class provides default implementations of page caching, pre-scaling 
 * of images. Classes that wish to make use of some or all of these features
 * should extend this class and implement readStream(InputStream). In this
 * method, BufferedImage objects should be packaged with the page number
 * (ImageSource index) into ImagePage objects and cached.</p>
 *
 * @author Bryan Varner
 */
public abstract class ImageIOPageSource extends DefaultThumbnailSource {
	private HashMap<Integer, ImagePage> imgCache;
	private String fileName;
	
	private float preScale;
	
	/**
	 * Creates a new ImageIOPageSource.
	 */
	protected ImageIOPageSource() {
		fileName = "";
		imgCache = new HashMap<Integer, ImagePage>();
		
		preScale = 1.0f;
	}
	
	/**
	 * Implements ImageSource. Closes the image source.
	 */
	public void dispose() {
		close();
	}
	
	/**
	 * Clears the fileName (if one is set), and disposes and flushes any 
	 * cached objects.
	 */
	public void close() {
		fileName = "";
		
		// Flush the page cache.
		for (ImagePage ip : imgCache.values()) {
			ip.image.flush();
		}
		imgCache.clear();
	}
	
	/**
	 * If pre-scaling is desired, set the preScale (both x and y axis) here.
	 * @param preScale The multiplier to use when scaling x and y axis.
	 */
	public void setPreScale(final float preScale) {
		this.preScale = preScale;
	}
	
	
	/**
	 * Opens the given stream as the given name.
	 * @param stream The stream to open.
	 * @param name The name of the stream
	 */
	protected void open(final InputStream stream, final String name) throws IOException {
		close();
		
		this.fileName = name;
		readStream(stream);
	}
	
	/**
	 * This readStream should parse the current stream and populate the 
	 * imageCache with new ImagePage objects..
	 */
	protected abstract void readStream(final InputStream stream) throws IOException;

	
	/**
	 * Caches the given ImagePage.
	 */
	protected void cachePage(final ImagePage ip) {
		imgCache.put(ip.pageNumber, ip);
	}
	
	
	/**
	 * Implements ImageSource.
	 */
	public BufferedImage getImage(final int index) {
		ImagePage ip = imgCache.get(index);
		if (ip != null) {
			BufferedImage image = ip.image;
			if (preScale != 1.0f) {
				return scale(image, preScale);
			}
			return image;
		}
		return null;
	}

	private static BufferedImage scale(final BufferedImage source, final float scale) {
		int targetWidth = Math.max(1, Math.round(source.getWidth() * scale));
		int targetHeight = Math.max(1, Math.round(source.getHeight() * scale));
		BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = scaled.createGraphics();
		try {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(source, 0, 0, targetWidth, targetHeight, null);
		} finally {
			g2.dispose();
		}
		return scaled;
	}


	/**
	 * Implements ImageSource. Gets the number of cached Images.
	 */
	public int getImageCount() {
		return imgCache.size();
	}
	
	
	/**
	 * Implements ImageSource.
	 */
	public String getImageName() {
		return fileName;
	}
	
	/**
	 * Internal class that represents a single images in a multi-image document.
	 */
	protected static class ImagePage {
		int        pageNumber;
		BufferedImage image;

		public ImagePage(final int page, final BufferedImage image) {
			this.pageNumber = page;
			this.image = image;
		}
	}
}