package com.ngs.image;

import com.ngs.DaemonThreadFactory;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import java.awt.Image;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.LookupOp;
import java.awt.image.RescaleOp;
import java.awt.image.ShortLookupTable;
import java.awt.geom.AffineTransform;

import java.awt.Dimension;
import java.awt.Rectangle;

import com.ngs.image.filter.BrightnessContrastOp;


/**
 * Controller / Model for manipulation of BufferedImages provided by an
 * ImageSource.
 *
 * @author Bryan.Varner
 */
public class ImageModel {
	public static final int FIT_NONE = 0;
	public static final int FIT_WIDTH = 1;
	public static final int FIT_HEIGHT = 2;
	public static final int FIT_BOTH = 3;
	
	BufferedImage image;
	
	Rectangle subClip;
	
	float scalex;
	float scaley;
	
	int rotation;
	
	float contrast;
	float brightness;
	boolean invert;
	
	ImageSource source;
	int page;
	Dimension cachedPageSize;
	
	int fitMode;
	Dimension fitSize;
	float oldScalex;
	float oldScaley;
	
	AffineTransformOp transformOp;
	RescaleOp rescaleOp;
	
	LookupOp lookupOp;
	short[] invertTable;
	
	ArrayList<ImageEventListener> listeners;
	
	PriorityBlockingQueue renderQueue;
	Thread renderThread;
	
	/**
	 * Creates a new ImageModel with a small thread pool for handling 
	 * manipulation commands asynchronously, and no ImageSource.
	 */
	public ImageModel() {
		listeners = new ArrayList<ImageEventListener>();
		
		image = null;
		source = null;
		
		page = 0;
		
		scalex = 1.0f;
		scaley = 1.0f;
		fitSize = null;
		fitMode = FIT_NONE;
		oldScalex = scalex;
		oldScaley = scaley;
		
		rotation = 0;
		
		transformOp = new AffineTransformOp(new AffineTransform(), AffineTransformOp.TYPE_BICUBIC);
		
		contrast = 1.0f;
		brightness = 0f;
		invert = false;
		
		cachedPageSize = new Dimension();
		
		rescaleOp = null;
		
		invertTable = new short[256];
		for (int i = 0; i < 256; i++) {
			invertTable[i] = (short) (255 - i);
		}
		lookupOp = new LookupOp(new ShortLookupTable(0, invertTable), null);
		
		subClip = null;
		
		renderQueue = new PriorityBlockingQueue();
		
		renderThread = new Thread(new RenderThread(renderQueue), "ImagePanel Picasso");
		renderThread.setDaemon(true);
		
		removeSource();
	}
	
	/**
	 * Constructs a new ImageModel for controlling the given ImageSource.
	 */
	public ImageModel(ImageSource source) {
		this();
		setSource(source);
	}
	
	/**
	 * Gets the current BufferedImage to be rendered by a view for this model.
	 */
	public BufferedImage getImage() {
		return image;
	}
	
	/**
	 * Adds an ImageEventListener to inform interested parties that a specific 
	 * type of event has occurred.
	 */
	public void addImageListener(ImageEventListener iel) {
		listeners.add(iel);
	}
	
	/**
	 * Removes an ImageEventListener used to inform interested parties that a 
	 * specific type of event has occurred.
	 */
	public void removeImageListener(ImageEventListener iel) {
		listeners.remove(iel);
	}
	
	
	/**
	 * Sets the ImageSource for this ImageModel to use when retrieving images
	 */
	public void setSource(ImageSource source) {
		// out with the old
		if (this.source != null) {
			this.source.dispose();
		}
		
		// in with the new.
		this.source = source;
		
		this.page = 0;
		
		updateTransform();
		if (!renderThread.isAlive()) {
			renderThread.start();
		}
	}
	
	
	/**
	 * Gets the ImageSource for this ImageModel
	 */
	public ImageSource getSource() {
		return this.source;
	}
	
	
	/**
	 * Removes the source that's currently in use.
	 */
	public void removeSource() {
		if (source != null) {
			source.dispose();
		}
		
		image = null;
		page = 0;
		cachedPageSize = new Dimension();
		
		setSource(new DummySource());
	}
	
	
	/**
	 * Otherwise, we clear the scale and go to full page rendering mode.
	 */
	public void setClip(Rectangle rect) {
		if (fitMode == FIT_NONE) {
			int proposedPix = rect.width * rect.height;
			int totalPix = cachedPageSize.width * cachedPageSize.height;
			int necessaryPix = (int)(0.60 * totalPix);
			
			if (!rect.equals(subClip) && (proposedPix < necessaryPix)) {
				subClip = rect;
				queueRender();
			} else if (subClip != null && (proposedPix >= necessaryPix)) {
				clearClip();
			}
		} else {
			fitSize = rect.getSize();
			clearClip();
		}
	}
	
	
	/**
	 * Sets the fit mode
	 */
	public void setFitMode(Rectangle rect, int fitMode) {
		if (rect == null || fitMode == FIT_NONE) {
			fitSize = null;
			scalex = oldScalex;
			scaley = oldScaley;
		} else {
			fitSize = rect.getSize();
			// Only store the old scale if we're setting the first time.
			if (this.fitMode == FIT_NONE) {
				oldScalex = scalex;
				oldScaley = scaley;
			}
		}
		this.fitMode = fitMode;
		updateTransform();
	}
	
	
	/**
	 * Gets the fitMode
	 */
	public int getFitMode() {
		return fitMode;
	}
	
	/**
	 * Retrieves the current clip bounds if we in sub-clip mode.
	 */
	public Rectangle getClip() {
		return subClip;
	}
	
	
	/**
	 * Removes the current clip if there is one.
	 */
	public void clearClip() {
		if (subClip != null || fitMode != FIT_NONE) {
			subClip = null;
			updateTransform();
		}
	}
	
	
	/**
	 * Gets the dimension of the current page, scaled by the current scale factor.
	 */
	public Dimension getPageSize() {
		return getPageSize(cachedPageSize);
	}
	
	
	/**
	 * Gets the dimension of the current page, scaled by the current scale factor.
	 * 
	 * Mutates the dimension provided prior to returning.
	 */
	public Dimension getPageSize(Dimension d) {
		if (source != null && source.getImage(page) != null) {
			int width = Math.round(source.getImage(page).getWidth() * scalex);
			int height = Math.round(source.getImage(page).getHeight() * scaley);
			
			if (rotation % 180 == 0) {
				if (d == null) {
					d = new Dimension(width, height);
				} else {
					d.setSize(width, height);
				}
			} else {
				if (d == null) {
					d = new Dimension(height, width);
				} else {
					d.setSize(height, width);
				}
			}
		}
		return d;
	}
	
	
	/**
	 * Handles an error occurring from a source.
	 */
	public void error(String message) {
		if (source != null) {
			source.dispose();
		}
		
		new Thread(new FireError(message)).start();
	}
	
	
	/**
	 * Forwards the request to the ImageSource.
	 */
	public int getImageCount() {
		return source.getImageCount();
	}
	
	/**
	 * Sets the current image to be rendered to the given index into the 
	 * current ImageSource.
	 */
	public void setImagePage(int page) {
		this.page = page;
		
		contrast = 1.0f;
		brightness = 0f;
		invert = false;
		
		rotation = 0;
		
		updateTransform();
		updateRescale();
		
		queueRender();
		fireEvent(new ImageEvent(this, ImageEvent.IMAGE_RESIZE, page));
	}
	
	/**
	 * Gets which image from the given ImageSource is currently being 
	 * made available for render.
	 */
	public int getImagePage() {
		return page;
	}
	
	/**
	 * Moves to the next image.
	 */
	public void nextPage() {
		if (getImagePage() + 1 < source.getImageCount()) {
			setImagePage(this.page + 1);
		}
	}
	
	/**
	 * Moves to the previous Image.
	 */
	public void prevPage() {
		if (getImagePage() > 0) {
			setImagePage(this.page - 1);
		}
	}
	
	
	/**
	 * Sets the rotation to the given degrees
	 * 
	 * If the combined rotation is > 360, we automatically 
	 * subtract 360, so that the calculated radians remain within the 0 - 360
	 * degree equivalent range.
	 */
	public void setRotation(int degrees) {
		if (degrees >= 360) {
			rotation = degrees - 360;
		} else if (degrees < 0) {
			rotation = degrees + 360;
		} else {
			rotation = degrees;
		}
		
		updateTransform();
	}
	
	
	/**
	 * Gets the current rotation in degrees
	 */
	public int getRotation() {
		return rotation;
	}
	
	
	/**
	 * Adds the number of degrees to the current rotation.
	 * To rotate clockwise, add 90.
	 * To rotate counter-clockwise, add -90.
	 */
	public void addRotation(int degrees) {
		setRotation(getRotation() + degrees);
	}
	
	
	/**
	 * Sets the scale factor for rendering the current Image.
	 */
	public void setScale(float scalex, float scaley) {
		this.scalex = scalex;
		this.scaley = scaley;
		
		updateTransform();
	}
	
	
	/**
	 * Sets the scale factor for rendering the current Image.
	 */
	public void setScale(float scale) {
		setScale(scale, scale);
	}
	
	
	/**
	 * Scales the Current scale factor by the given amount.
	 */
	public void scaleBy(float factor) {
		setScale(scalex * (1.0f / factor), scaley * (1.0f / factor));
	}
	
	
	/**
	 * Gets the scale.
	 */
	public float getScale() {
		// Return the smaller
		return scalex < scaley ? scalex : scaley;
	}
	
	
	/**
	 * Sets the Brightness.
	 * Default is 0. Negative & positive numbers are acceptable.
	 */
	public void setBrightness(int offset) {
		brightness = (float)offset;
		
		updateRescale();
	}
	
	/**
	 * Adjusts the current brightness value by the given increment.
	 */
	public void adjustBrightness(int by) {
		setBrightness(getBrightness() + by);
	}
	
	
	/**
	 * Gets the current brightness value.
	 */
	public int getBrightness() {
		return (int)brightness;
	}
	
	
	/**
	 * Sets the current contrast value.
	 * the default is 1.0 (float). Valid values are anything > 0.
	 */
	public void setContrast(float contrast) {
		this.contrast = contrast;
		
		updateRescale();
	}
	
	/**
	 * Adds the given increment to the current contrast value.
	 */
	public void adjustContrast(float by) {
		setContrast(getContrast() + by);
	}
	
	/**
	 * Gets the current contrast value.
	 */
	public float getContrast() {
		return contrast;
	}
	
	
	/**
	 * Tells the model to render the image with all colors inverted.
	 */
	public void invert() {
		invert = !invert;
		
		queueRender();
	}
	
	/**
	 * Returns weather or not this instance is going to invert the image.
	 */
	public boolean isInverted() {
		return invert;
	}
	
	
	/**
	 * ImageModel subclasses can override this method to extend the rendering
	 * process. 
	 *
	 * This method is invoked <b>prior</b> to any clipping, rotation, scaling,
	 * or rescaling occurs.
	 */
	public BufferedImage preRender(BufferedImage image) {
		return image;
	}
	
	/**
	 * ImageModel subclasses can override this method to extend the rendering
	 * process. Note that all clipping, rotation, and rescaling occurs -before-
	 * this method is called.
	 */
	public BufferedImage extendRender(BufferedImage image) {
		return image;
	}
	
	
	/**
	 * Updates the current image rotation / scale transform.
	 */
	private void updateTransform() {
		BufferedImage img = preRender(source.getImage(page));
		
		
		if (rotation == 0 || rotation == 180) {
			if ((fitMode & FIT_WIDTH) == FIT_WIDTH) {
				scalex = (float)(fitSize.width) / img.getWidth();
			}
			if ((fitMode & FIT_HEIGHT) == FIT_HEIGHT) {
				scaley = (float)(fitSize.height) / img.getHeight();
			}
		} else {
			if ((fitMode & FIT_WIDTH) == FIT_WIDTH) {
				scaley = (float)(fitSize.width) / img.getHeight();
			}
			if ((fitMode & FIT_HEIGHT) == FIT_HEIGHT) {
				scalex = (float)(fitSize.height) / img.getWidth();
			}
		}
		
		
		updateTransform(img);
		
		queueRender();
		
		fireEvent(new ImageEvent(this, ImageEvent.IMAGE_RESIZE, page));
	}
	
	
	public AffineTransform getTransform() {
		return transformOp.getTransform();
	}
	
	
	/**
	 * Updates the scale / rotate filter operation.
	 */
	private void updateTransform(BufferedImage img) {
		AffineTransform transform = new AffineTransform();
		
		
		// Determine the rotation point based on the target rotation.
		if (rotation != 0) {;
			double width = img.getWidth() * scalex;
			double height = img.getHeight() * scaley;
			
			double x = width / 2;
			double y = height / 2;
			
			switch (rotation) {
				case 90: {
					x = ((double)(height / 2));
					y = ((double)(height / 2));
					break;
				}
				case 270: {
					x = ((double)(width / 2));
					y = ((double)(width / 2));
					break;
				}
			}
			
			transform.rotate(Math.toRadians(rotation), x, y);
		}
		
		if (scalex != 1.0f || scaley != 1.0f) {
			if (scalex > 0 && scaley > 0) {
				transform.scale(scalex, scaley);
			} else {
				transform.scale(0.001f, 0.001f);
			}
		}
		
		
		transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
	}
	
	
	/**
	 * Updates the rescale operation based upon the current state.
	 */
	private void updateRescale() {
		// Find the means of all color bands in the image.
		if (brightness != 0f || contrast != 1.0f) {
			int numBands = image.getRaster().getNumBands();
			
			// Create the factors & offsets, and lets' have a good time.
			float[] scaleFactors = new float[numBands];
			float[] offsets = new float[numBands];
			for (int i = 0; i < numBands; i++) {
				scaleFactors[i] = contrast;
				offsets[i] = brightness;
			}
			
			rescaleOp = new RescaleOp(scaleFactors, offsets, null);
		} else {
			rescaleOp = null;
		}
		
		queueRender();
	}
	
	
	/**
	 * Places a new entry into the render Queue.
	 */
	public void queueRender() {
		renderQueue.offer(Long.valueOf(System.currentTimeMillis()));
	}
	
	
	/**
	 * Immediately re-render and fire an event when done. This is a blocking
	 * operation, and should -not- be executed in the AWT Event thread.
	 */
	public void render() {
		BufferedImage processedImage = preRender(source.getImage(page));
		
		/* Sub-Clip Processing */
		if (subClip != null) {
			int width = subClip.width;
			int height = subClip.height;
			int x = subClip.x;
			int y = subClip.y;
			
			
			// Scale the rectangle by the multiplicitive reciprocal
			if (scalex != 1.0f) {
				x = Math.round(x * (1.0f / scalex));
				width = Math.round(width * (1.0f / scalex));
			}
			if (scaley != 1.0f) {
				y = Math.round(y * (1.0f / scaley));
				height = Math.round(height * (1.0f / scaley));
			}
			
			
			// Account for rotation in processing the subimage clipping.
			if (rotation == 90) {
				x = y;
				
				int swap = height;
				height = width;
				width = swap;
				
				y = processedImage.getHeight() - subClip.x - height;
			} else if (rotation == 180) {
				y = processedImage.getHeight() - height - y;
				x = processedImage.getWidth() - width - x;
			} else if (rotation == 270) {
				y = x;
				
				int swap = height;
				height = width;
				width = swap;
				
				x = processedImage.getWidth() - subClip.y - width;
			}
			
			
			// Ensure that our clip is within the bounds of the raster.
			if (x < 0) {
				width = width + x;
				x = 0;
			}
			if (y < 0) {
				height = height + y;
				y = 0;
			}
			if (x + width > processedImage.getWidth()) {
				width = processedImage.getWidth() - x;
			}
			if (y + height > processedImage.getHeight()) {
				height = processedImage.getHeight() - y;
			}
			
			
			processedImage = processedImage.getSubimage(x, y, width, height);
			
			// Update the rotation
			updateTransform(processedImage);
		}
		
		
		/* If we do any transforming, do it here.
		 * Note that this also executes for rescaling. The transformOp 
		 * creates a non-IndexColorModel compatible image, which allows us to
		 * rescale the resultant image.
		 *
		 * TODO: It would probably be better to interrogate the ColorModel of the 
		 * image, and only filter for rescale if we're using an IndexColorModel.
		 */
		if (rescaleOp != null || !transformOp.getTransform().isIdentity()) {
			processedImage = transformOp.filter(processedImage, null);
		}
		
		/* Handle any Rescaling.
		 */
		if (rescaleOp != null) {
			processedImage = rescaleOp.filter(processedImage, processedImage);
		}
		
		/* Handle inversion!
		 */
		if (invert) {
			processedImage = lookupOp.filter(processedImage, processedImage);
		}
		
		
		/* Update the rendered image for painting.
		 */
		if (image != null) {
			image.flush();
			image = null;
		}
		image = extendRender(processedImage);
		
		// Fire an INVALID if there are no additional Renders pending.
		if (renderQueue.size() == 0) {
			fireEvent(new ImageEvent(this, ImageEvent.IMAGE_INVALID, page));
		}
	}
	
	
	/**
	 * Fires events to the registered listeners
	 */
	void fireEvent(ImageEvent ie) {
		for (ImageEventListener listener : listeners) {
			switch (ie.getType()) {
				case ImageEvent.IMAGE_INVALID:
				case ImageEvent.IMAGE_RESIZE:
					listener.imageChanged(ie);
					break;
				case ImageEvent.IMAGE_ERROR:
					listener.imageError(ie);
					break;
			}
		}
	}
	
	/**
	 * Implements a no-op ImageSource.
	 */
	private static class DummySource extends ImageSource {
		public BufferedImage getImage(int index) {
			return null;
		}
		
		public BufferedImage getImage(int index, Rectangle rect) {
			return null;
		}
		
		public BufferedImage getThumbnail(int index) {
			return null;
		}
		
		public int getImageCount() {
			return 0;
		}
		
		public String getImageName() {
			return "";
		}
		
		public void dispose() {
			return;
		}
	}
	
	/**
	 * A Runnable that executes to fire error messages
	 */
	private class FireError implements Runnable {
		String message;
		
		public FireError(String message) {
			this.message = message;
		}
		
		public void run() {
			fireEvent(new ImageEvent(this, ImageEvent.IMAGE_ERROR, message));
		}
	}
	
	
	/**
	 * Executes the render method asynchronouously.
	 */
	private class RenderThread implements Runnable {
		BlockingQueue queue;
		
		public RenderThread(BlockingQueue queue) {
			this.queue = queue;
		}
		
		public void run() {
			while(true) {
				try {
					Long headTime = (Long)queue.take();
					
					// Wiat up to 10 ms for another entry.
					if (queue.peek() != null) {
						Thread.currentThread().sleep(10);
					}
					
					while (queue.peek() != null) {
						headTime = (Long)queue.poll();
					}
					
					render();
				} catch (Exception ex) {
				} catch (OutOfMemoryError oome) {
					System.gc();
				}
			}
		}
	}
}