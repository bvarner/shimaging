package com.varnernet.shimaging.filter;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;


/**
 * <p>Java-Only implementation that handles Brightness, Contrast, and Invert
 * via lookup-table manipulation of ColorModels. This is included for
 * completness, and is probably most beneficial to those individuals looking to
 * learn about lower-level Image handling & manipulation.</p>
 * 
 * <p>This class is unused by the core Shimaging ImagePanel, but may be usefull
 * in some cases. Shimaging uses RescaleOp & LookupOp to implement it's contrast, 
 * brightness, and invert operations. These J2SE standard filters are often 
 * accelerated by ImageLib native implementations.</p>
 */ 
public class BrightnessContrastOp implements BufferedImageOp, RasterOp {
	int brightness;
	float contrast;
	boolean invert;
	RenderingHints hints;
	
	/**
	 * Creates a new Brightness & Contrast Operation.
	 */
	public BrightnessContrastOp(final RenderingHints hints) {
		this(0, 1.0f, hints);
	}
	
	/**
	 * Creates a new Brightness & Contrast Operation with the given 
	 * brightness, contrast, and rendering hints.
	 */
	public BrightnessContrastOp(final int brightness, final float contrast, final RenderingHints hints) {
		this.brightness = brightness;
		this.contrast = contrast;
		this.hints = hints;
		this.invert = false;
	}
	
	/**
	 * Sets the brightness used when filtering.
	 */
	public void setBrightness(final int brightness) {
		this.brightness = brightness;
	}
	
	/**
	 * Sets the contrast used when filtering.
	 */
	public void setContrast(final float contrast) {
		if (contrast > 0.0f) {
			this.contrast = contrast;
		}
	}
	
	public int getBrightness() {
		return this.brightness;
	}
	
	public float getContrast() {
		return this.contrast;
	}
	
	public boolean getInvert() {
		return invert;
	}
	
	public void setInvert(final boolean invert) {
		this.invert = invert;
	}
	
	
	public BufferedImage createCompatibleDestImage(final BufferedImage src, final ColorModel destCM) {
		BufferedImage image;

		int w = src.getWidth();
		int h = src.getHeight();

		if (destCM == null) {
			ColorModel cm = src.getColorModel();

			image = new BufferedImage(cm,
								cm.createCompatibleWritableRaster(w, h),
								cm.isAlphaPremultiplied(),
								null);
		} else {
			image = new BufferedImage(destCM,
							destCM.createCompatibleWritableRaster(w,
							h),
							destCM.isAlphaPremultiplied(),
							null);
		}

		return image;
	}
	
	
	public WritableRaster createCompatibleDestRaster(final Raster src) {
		return src.createCompatibleWritableRaster();
	}
	
	
	public Rectangle2D getBounds2D(final BufferedImage src) {
		return getBounds2D(src.getRaster());
	}
	
	public Rectangle2D getBounds2D(final Raster src) {
		return src.getBounds();
	}
	
	public Point2D getPoint2D(final Point2D srcPt, Point2D dstPt) {
		if (dstPt == null) {
			dstPt = new Point2D.Float();
		}
		dstPt.setLocation(srcPt);
		
		return dstPt;
	}
	
	public RenderingHints getRenderingHints() {
		return hints;
	}
	
	public BufferedImage filter(final BufferedImage src, BufferedImage dst) {
		ColorModel srcCM = src.getColorModel();
		ColorModel dstCM;
		if (srcCM instanceof IndexColorModel) {
			throw new IllegalArgumentException("LookupOp cannot be "+
										"performed on an indexed image");
		}
		
		boolean needToConvert = false;
		
		int width = src.getWidth();
		int height = src.getHeight();
		
		if (dst == null) {
			dst = createCompatibleDestImage(src, null);
			dstCM = srcCM;
		} else {
			if (width != dst.getWidth()) {
				throw new IllegalArgumentException("Src width (" + width + 
						") not equal to dst width (" + dst.getWidth() + ")");
			}
			if (height != dst.getHeight()) {
				throw new IllegalArgumentException("Src height (" + height + 
						") not equal to dst height (" + dst.getHeight() + ")");
			}
			
			dstCM = dst.getColorModel();
			if (srcCM.getColorSpace().getType() != 
			    dstCM.getColorSpace().getType())
			{
				needToConvert = true;
				dst = createCompatibleDestImage(src, null);
			}
		}
		
		BufferedImage origDst = dst;
		
		WritableRaster srcRaster = src.getRaster();
		WritableRaster dstRaster = dst.getRaster();
		
		filter(srcRaster, dstRaster);
		
		if (needToConvert) {
			// ColorModels are not the same
			ColorConvertOp ccop = new ColorConvertOp(hints);
			ccop.filter(dst, origDst);
		}
		
		return origDst;
	}
	
	public WritableRaster filter(final Raster src, WritableRaster dst) {
		int numBands  = src.getNumBands();
		int dstLength = dst.getNumBands();
		int height    = src.getHeight();
		int width     = src.getWidth();
		int srcPix[]  = new int[numBands];
		
		if (dst == null) {
			dst = createCompatibleDestRaster(src);
		} else if (height != dst.getHeight() || width != dst.getWidth()) {
			throw new IllegalArgumentException(
				"Width or height of Rasters do not match");
		}
		
		dstLength = dst.getNumBands();
		
		if (numBands != dstLength) {
			throw new IllegalArgumentException(
				"Number of channels in the src (" + numBands +
				") does not match number of channels"
				+ " in the destination (" + dstLength + ")");
		}
		
		if (brightness != 0 || contrast != 1.0f || invert) {
			int mean[]  = new int[numBands];
			
			int sminX = src.getMinX();
			int sY = src.getMinY();
			int dminX = dst.getMinX();
			int dY = dst.getMinY();
			for (int y = 0; y < height; y++, sY++, dY++) {
				int sX = sminX;
				int dX = dminX;
				for (int x = 0; x < width; x++, sX++, dX++) {
					// Find the data for all the bands at this x, y position.
					src.getPixel(sX, sY, srcPix);
					
					// Add to the totals
					for (int i = 0; i < numBands; i++) {
						mean[i] += srcPix[i];
					}
				}
			}
			
			// Calculate the mean for the totals.
			for (int i = 0; i < numBands; i++) {
				mean[i] = mean[i] / (width * height);
			}
			
			int[][] changelut = new int[numBands][256];
			for (int band = 0; band < changelut.length; band++) {
				for (int level = 0; level < 256; level++) {
					changelut[band][level] = clamp(mean[band] +
							((level - mean[band]) * contrast) + 
							brightness);
				}
				if (invert) {
					for (int level = 0; level < 128; level++) {
						int swap = changelut[band][level];
						changelut[band][level] = changelut[band][255 - level];
						changelut[band][255 - level] = swap;
					}
				}
			}
			
			sminX = src.getMinX();
			sY = src.getMinY();
			dminX = dst.getMinX();
			dY = dst.getMinY();
			
			for (int y = 0; y < height; y++, sY++, dY++) {
				int sX = sminX;
				int dX = dminX;
				for (int x = 0; x < width; x++, sX++, dX++) {
					// Find the data for tall the bands at this x, y position.
					src.getPixel(sX, sY, srcPix);
					
					// Set the source[band] = to the lookup value.
					for (int band = 0; band < numBands; band++) {
						// do not apply to the alpha band!
						if (band < 3 || contrast != 1.0f) {
							srcPix[band] = changelut[band][srcPix[band]];
						}
					}
					
					dst.setPixel(dX, dY, srcPix);
				}
			}
		} else {
			dst.setDataElements(0, 0, src);
		}
		
		return dst;
	}
	
	/**
	 * Limits byte values to between 255 and 0
	 */
	private int clamp(final float v) {
		if (v > 255) {
			return (int)255;
		} else if (v < 0) {
			return (int)0;
		}
		return (int)v;
	}
	
}