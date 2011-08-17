package com.shimaging.image.source;

import com.shimaging.image.ImageSource;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;


import java.util.ArrayList;
import java.util.Arrays;

/**
 * A CompositeSource allows for layering of discrete ImageSources into a 
 * single aggregate ImageSource.
 * 
 * <p>The first layer of the CompositeSource becomes the destination for all
 * subsequent layers to render to. Each layer is rendered to the output of all
 * previous layers. Thus, the third layer is composited against the result of 
 * the fisrt and second layers. The fourt layer against the first, second, and 
 * third layers, etc.</p>
 *
 * <p>In the event that you want to composite two multi-page sources that have
 * differing page counts, if either the source or destination image passed to
 * <code>doComposite()</code> is null, the opposing image is returned.
 * This allows us to automate the process of setting the bottom layer as the
 * destination, as well as simply returning the destination if a source is null.
 * So long as ImageSources added as additional layers will return <code>null</code>
 * when asked for a page they do not contain, rendering will simply skip that
 * layer by returning the existing destination.</p>
 * 
 * <p>There is an opportunity for subclasses to compute an AffineTransform to use
 * prior to compositing. For instance, if you want all of source images scaled
 * to respect the bounds of the destination, override <code>calculateTransform()</code>
 * and return an AffineTransform that will perform the function you require.</p>
 *
 * @author Bryan.Varner
 */
public class CompositeSource extends DefaultThumbnailSource {
	protected ArrayList<Compositable> layers;
	
	int lastPage;
	BufferedImage lastComp;
	
	/**
	 * An Identity AffineTransform.
	 */
	protected AffineTransform identity;
	
	/**
	 * Creates a new CompositeSource with no sources to composite.
	 */
	public CompositeSource() {
		layers = new ArrayList<Compositable>();
		
		identity = new AffineTransform();
		
		lastPage = -1;
		lastComp = null;
	}
	
	
	/**
	 * Creates a new CompositeSource with a specified ImageSource as the bottom
	 * layer (a.k.a. The destination for all compositing)
	 * 
	 * @param bottom The lowest level in the composite stack.
	 */
	public CompositeSource(ImageSource bottom) {
		this();
		addLayer(bottom);
	}
	
	
	/**
	 * Creates a new CompositeSource with a specified ImageSource as the bottom
	 * layer (a.k.a. The destination for all compositing)
	 * 
	 * @param bottom The lowest level in the composite stack.
	 * @param mode The mode used to composite the ImageSource.
	 */
	public CompositeSource(ImageSource bottom, Composite mode) {
		this();
		addLayer(bottom, mode);
	}
	
	
	/**
	 * Creates a new CompositeSource with a specified ImageSource as the bottom
	 * layer (a.k.a. The destination for all compositing)
	 * 
	 * @param bottom The lowest level in the composite stack.
	 * @param visible Weather or not the specified layer is visible.
	 */
	public CompositeSource(ImageSource bottom, boolean visible) {
		this();
		addLayer(bottom, visible);
	}
	
	
	/**
	 * Creates a new CompositeSource with a specified ImageSource as the bottom
	 * layer (a.k.a. The destination for all compositing)
	 * 
	 * @param bottom The lowest level in the composite stack.
	 * @param mode The mode used to composite the ImageSource.
	 * @param visible Weather or not the specified layer is visible.
	 */
	public CompositeSource(ImageSource bottom, Composite mode, boolean visible) {
		this();
		addLayer(bottom, mode, visible);
	}
	
	
	/**
	 * Adds the given ImageSource as the top layer, using the specified 
	 * Composite object to render.
	 *
	 * @param layer The ImageSource to add.
	 * @param mode The Composite to use when rendering
	 * @param visible If true, the layers visibility is enabled.
	 */
	public final void addLayer(ImageSource layer, Composite mode, boolean visible) {
		layers.add(new Compositable(layer, mode, visible));
		lastPage = -1;
	}
	
	
	/**
	 * Adds the given ImageSource as the top layer, using the specified 
	 * Composite object to render.
	 *
	 * @param layer The ImageSource to add.
	 * @param mode The Composite to use when rendering
	 */
	public final void addLayer(ImageSource layer, Composite mode) {
		addLayer(layer, mode, true);
	}
	
	
	/**
	 * Adds the given ImageSource as the top layer, using AlphaComposite.SrcOver.
	 *
	 * @param layer The ImageSource to add.
	 * @param visible If true, the layers visibility is enabled.
	 */
	public final void addLayer(ImageSource layer, boolean visible) {
		addLayer(layer, AlphaComposite.SrcOver, visible);
	}
	
	/**
	 * Adds the given ImageSource as the top layer, using a SRC_OVER
	 * AlphaComposite (the default for all Java2D Graphics2D renderings).
	 *
	 * @param layer The IMageSource to add
	 */
	public final void addLayer(ImageSource layer) {
		this.addLayer(layer, AlphaComposite.SrcOver);
	}
	
	
	public void removeLayer(int layer) {
		Compositable c = layers.remove(layer);
		c.source.dispose();
		lastPage = -1;
	}
	
	
	/**
	 * Removes all layers, and disposes of them.
	 */
	public void removeAllLayers() {
		for (Compositable c : layers) {
			c.source.dispose();
		}
		layers.clear();
		lastPage = -1;
	}
	
	
	public int getNumLayers() {
		return layers.size();
	}
	
	
	public Compositable getLayer(int layer) {
		return layers.get(layer);
	}
	
	
	public void setLayerVisibility(int layer, boolean visible) {
		layers.get(layer).enabled = visible;
		lastPage = -1;
	}
	
	
	public boolean isLayerVisible(int layer) {
		return layers.get(layer).enabled;
	}
	
	
	/**
	 * Renders and retrieves the image at the current index, by compositing
	 * all added layers with the specified Composite methods.
	 *
	 * @param index The index of the image in the ImageSource to composite.
	 * @return A BufferedImage composition of all layers in this CompositeSource.
	 */
	public synchronized BufferedImage getImage(int index) {
		if (layers.size() > 0) {
			if (index != lastPage) {
				int layer = 0;
				flushComposition();
				
				for (Compositable c : layers) {
					boolean compositeLayer = c.enabled;
					
					if (compositeLayer && c.enabledPages != null) {
						Arrays.sort(c.enabledPages);
						compositeLayer = Arrays.binarySearch(c.enabledPages, index) >= 0;
					}
						
					if (compositeLayer) {
						BufferedImage src = c.source.getImage(index);
						
						// Calculate the transform, then pass that along
						// to the doComposite, where we actually 
						// paint into the bottom layer's BufferedImage.
						lastComp = doComposite(src, lastComp, c.mode, 
									calculateTransform(src, lastComp));
					}
					layer++;
				}
				lastPage = index;
			}
		} else {
			flushComposition();
		}
		return lastComp;
	}
	
	
	private void flushComposition() {
		if (lastComp != null) {
			lastComp.flush();
			lastComp = null;
		}
	}
	
	/**
	 * Hook function that allows subclasses to calculate AffineTransforms to be
	 * applied during the composite rendering. This method is called immediately
	 * prior to the composite of the src and dst being rendered.
	 *
	 * @param src The source image about to be composited.
	 * @param dst The destination image about to be composited.
	 * @return An AffineTransform to use. This implementation returns <code>identity</code>
	 */
	protected AffineTransform calculateTransform(BufferedImage src, BufferedImage dst) {
		return identity;
	}
	
	
	/**
	 * Composite <code>src</code> into <code>dst</code> using <code>mode</code>
	 * and <code>transform.</code>
	 * 
	 * @param src The source image to composite from
	 * @param dst The destination image to composite into
	 * @param mode The Composite mode to employ.
	 * @param transform An AffineTransform that should be applied to src as it
	 * is composited into dst.
	 */
	protected BufferedImage doComposite(BufferedImage src, BufferedImage dst, Composite mode, 
	                                    AffineTransform transform)
	{
		if (src == null) {
			return dst;
		}
		
		if (dst == null) {
			dst = new BufferedImage(src.getWidth(), src.getHeight(),
									BufferedImage.TYPE_INT_ARGB);
		}
		
		
		if (dst != null) {
			Graphics2D gfx = (Graphics2D)dst.getGraphics();
			
			gfx.setComposite(mode);
			gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
							 RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			gfx.drawRenderedImage(src, transform);
			gfx.dispose();
		} else {
			dst = src;
		}
		
		return dst;
	}
	
	/**
	 * Returns the maximum number of images from any single Layer in this 
	 * CompositeSource's stack.
	 * 
	 * @return The maximum number of images from any single layer.
	 */
	public int getImageCount() {
		int max = -1;
		for (Compositable c : layers) {
			if (c.enabled && c.source.getImageCount() > max) {
				max = c.source.getImageCount();
			}
		}
		return max;
	}
	
	/**
	 * Releases all cached resources, and invokes dispose() on all 
	 * ImageSource layers.
	 */
	public synchronized void dispose() {
		for (Compositable c : layers) {
			c.source.dispose();
		}
		layers.clear();
		flushComposition();
	}
	
	
	public String getImageName() {
		if (layers.size() == 0) {
			return "";
		}
		return layers.get(0).source.getImageName();
	}
	
	public static class Compositable {
		public Composite mode;
		public ImageSource source;
		public Boolean enabled;
		public int[] enabledPages;
		
		Compositable(ImageSource source, Composite mode, boolean visible) {
			this.mode = mode;
			this.source = source;
			this.enabled = visible;
			this.enabledPages = null;
		}
	}
}