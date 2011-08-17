package com.shimaging.source;

import com.shimaging.ImageSource;

import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.Composite;

/**
 * This subclass of CompositeSource will scale all source layers to the exact
 * dimensions of the destination layer. The scale transform is only re-computed
 * if the scaled size of the source has a deviance +- rescaleThreashold.
 * 
 * @Author Bryan Varner
 */
public class ScalingCompositeSource extends CompositeSource {
	int oldHeight;
	int oldWidth;
	
	int rescaleThreashold;
	
	AffineTransform scale;
	
	
	public ScalingCompositeSource() {
		super();
		init();
	}
	
	
	public ScalingCompositeSource(ImageSource bottom) {
		super(bottom);
		init();
	}
	
	
	public ScalingCompositeSource(ImageSource bottom, Composite mode) {
		super(bottom, mode);
		init();
	}
	
	public ScalingCompositeSource(ImageSource bottom, boolean visible) {
		super(bottom, visible);
		init();
	}
	
	public ScalingCompositeSource(ImageSource bottom, Composite mode, boolean visible) {
		super(bottom, mode, visible);
		init();
	}
	
	/**
	 * Sets up the default threashold (8), and requires the next 
	 * calculateTransform to calculate the scale transform.
	 */
	protected final void init() {
		rescaleThreashold = 8;
		oldHeight = 0;
		oldWidth = 0;
		scale = null;
	}
	
	
	/**
	 * Set the threashold for subsequent rescaling.
	 */
	public void setThreashold(int rescaleThreashold) {
		this.rescaleThreashold = rescaleThreashold;
	}
	
	
	/**
	 * Gets the rescale threashold.
	 */
	public int getThreashold() {
		return rescaleThreashold;
	}
	
	
	/**
	 * Returns a scaleInstance AffineTransform to resize the src to fit the
	 * bounds of the dst, within the rescale threashold.
	 */
	@Override
	protected AffineTransform calculateTransform(BufferedImage src, BufferedImage dst) {
		if (src != null && dst != null) {
			if (scale == null || 
			    Math.abs(dst.getWidth() - oldWidth) > rescaleThreashold ||
			    Math.abs(dst.getHeight() - oldHeight) > rescaleThreashold)
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
