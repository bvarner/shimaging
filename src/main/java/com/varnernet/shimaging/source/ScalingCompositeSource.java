package com.varnernet.shimaging.source;

import com.varnernet.shimaging.ImageSource;

import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.Composite;

/**
 * This subclass of CompositeSource will scale all source layers to the exact
 * dimensions of the destination layer. The scale transform is only re-computed
 * if the scaled size of the source has a deviance +- rescaleThreshold.
 * 
 * @Author Bryan Varner
 */
public class ScalingCompositeSource extends CompositeSource {
	int oldHeight;
	int oldWidth;
	
	int rescaleThreshold;
	
	AffineTransform scale;
	
	
	public ScalingCompositeSource() {
		super();
		init();
	}
	
	
	public ScalingCompositeSource(final ImageSource bottom) {
		super(bottom);
		init();
	}
	
	
	public ScalingCompositeSource(final ImageSource bottom, final Composite mode) {
		super(bottom, mode);
		init();
	}
	
	public ScalingCompositeSource(final ImageSource bottom, final boolean visible) {
		super(bottom, visible);
		init();
	}
	
	public ScalingCompositeSource(final ImageSource bottom, final Composite mode, final boolean visible) {
		super(bottom, mode, visible);
		init();
	}
	
	/**
	 * Sets up the default threshold (8), and requires the next 
	 * calculateTransform to calculate the scale transform.
	 */
	protected final void init() {
		rescaleThreshold = 8;
		oldHeight = 0;
		oldWidth = 0;
		scale = null;
	}
	
	
	/**
	 * Set the threshold for subsequent rescaling.
	 */
	public void setThreshold(final int rescaleThreshold) {
		this.rescaleThreshold = rescaleThreshold;
	}
	
	
	/**
	 * Gets the rescale threshold.
	 */
	public int getThreshold() {
		return rescaleThreshold;
	}
	
	
	/**
	 * Returns a scaleInstance AffineTransform to resize the src to fit the
	 * bounds of the dst, within the rescale threshold.
	 */
	@Override
	protected AffineTransform calculateTransform(final BufferedImage src, final BufferedImage dst) {
		if (src != null && dst != null) {
			if (scale == null || 
			    Math.abs(dst.getWidth() - oldWidth) > rescaleThreshold ||
			    Math.abs(dst.getHeight() - oldHeight) > rescaleThreshold)
			{
				oldWidth = dst.getWidth();
				oldHeight = dst.getHeight();
				scale = AffineTransform.getScaleInstance(
									(double)(dst.getWidth()) / src.getWidth(), 
									(double)(dst.getHeight()) / src.getHeight());
			}
			return scale;
		} else {
			return identity;
		}
	}
	
	
	/**
	 * Gets the current transform
	 */
	public AffineTransform getTransform() {
		if (scale != null) {
			return scale;
		}
		return new AffineTransform();
	}
}
