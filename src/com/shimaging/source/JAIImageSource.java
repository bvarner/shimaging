package com.shimaging.source;


import java.awt.image.BufferedImage;

import java.io.IOException;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.Interpolation;
import javax.media.jai.operator.ScaleDescriptor;

import java.util.HashMap;

import com.sun.media.jai.codec.SeekableStream;

/**
 * An ImageSource that reads images using the JAI libraries from 
 * SeekableStream objects.
 * 
 * <p>This class provides default implementations of page caching, pre-scaling 
 * of images, and converting JAI PlanarImages to BufferedImages. Classes that
 * wish to make use of some or all of these features should extend this class, 
 * and implement the (using JAI) readStream(SeekableStream) method. In this
 * method, RenderedOp objects should be packaged with the page number 
 * (ImageSource index) into ImagePage objects, and cached. JAI will lazily
 * render (and cache) the Image to a BufferedImage. This class respects that.</p>
 * 
 * @author Bryan Varner
 */
public abstract class JAIImageSource extends DefaultThumbnailSource {
	private HashMap<Integer, ImagePage> imgCache;
	private String fileName;
	
	private float preScale;
	
	/**
	 * Creates a new JAIImageSOurce
	 */
	protected JAIImageSource() {
		fileName = "";
		imgCache = new HashMap<Integer, ImagePage>();
		
		preScale = 1.0f;
	}
	
	/**
	 * Implements ImageSource. Closes the JAIImageSource.
	 */
	public void dispose() {
		close();
	}
	
	/**
	 * Clears the fileName (if one is set), and disposes and flushes any 
	 * cached, rendered objects.
	 */
	public void close() {
		fileName = "";
		
		// Flush the page cache.
		for (ImagePage ip : imgCache.values()) {
			ip.image.dispose();
		}
		imgCache.clear();
	}
	
	/**
	 * If pre-scaling (using JAI) is desired, set the preScale (both x and y axis) here.
	 * @param preScale The multiplier to use when scaling x and y axis.
	 */
	public void setPreScale(float preScale) {
		this.preScale = preScale;
	}
	
	
	/**
	 * Opens the given Stream as the given name.
	 * @param stream The stream to open.
	 * @param name The name of the stream
	 */
	protected void open(SeekableStream stream, String name) {
		close();
		
		this.fileName = name;
		try {
			readStream(stream);
		} catch (IOException ioe) {
			System.out.println(ioe.toString());
			try {
				stream.close();
			} catch (IOException iioe) {}
		}
	}
	
	/**
	 * This readStream should parse the current stream and populate the 
	 * imageCache with new ImagePage objects..
	 */
	protected abstract void readStream(SeekableStream stream) throws IOException;
	
	
	/**
	 * Caches the given ImagePage.
	 */
	protected void cachePage(ImagePage ip) {
		imgCache.put(ip.pageNumber, ip);
	}
	
	
	/**
	 * Implements ImageSource.
	 */
	public BufferedImage getImage(int index) {
		ImagePage ip = imgCache.get(index);
		if (ip != null) {
			RenderedOp image = ip.image;
			if (preScale != 1.0f) {
				ScaleDescriptor scale = new ScaleDescriptor();
				image = scale.create(image, preScale, preScale, 0f, 0f, 
						Interpolation.getInstance(Interpolation.INTERP_BILINEAR), 
						null);
			}
			
			PlanarImage img = image.getCurrentRendering();
			if (img == null) {
				img = image.getRendering();
			}
			return img.getAsBufferedImage();
		}
		return null;
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
		RenderedOp image;
		
		public ImagePage(int page, RenderedOp image) {
			this.pageNumber = page;
			this.image = image;
		}
	}
}