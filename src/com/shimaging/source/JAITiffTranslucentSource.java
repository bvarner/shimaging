package com.shimaging.source;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.awt.RenderingHints;
import java.awt.image.IndexColorModel;

import javax.media.jai.JAI;
import javax.media.jai.ImageLayout;

/**
 * An ImageSource that reads TIFF images, uses an IndexedColorModel with valid 
 * values for Black and White, where White pixels are TRANSPARENT.
 * 
 * This lets you read B&W TIFF images, and do some advanced things with 
 * compositing.
 */
public class JAITiffTranslucentSource extends JAITiffImageSource {
	protected JAITiffTranslucentSource() {
		/* Setup target ImageLayout
		 * Black & White, indexed colormodel. White pixels are transparent.
		 */
		byte[] legalBits = {(byte)255, (byte)0};
		ImageLayout layout = new ImageLayout();
		layout.setColorModel(new IndexColorModel(1, 2, legalBits, legalBits, legalBits, 0));
		addRenderingHints(new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));

//		byte[] legalBits = {(byte)0, (byte)255};
//		ImageLayout layout = new ImageLayout();
//		layout.setColorModel(new IndexColorModel(1, 2, legalBits, legalBits, legalBits, 1));
//		addRenderingHints(new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));
	}
	
	public JAITiffTranslucentSource(File f) throws IOException {
		this();
		open(f);
	}
	
	public JAITiffTranslucentSource(InputStream stream, String name) throws IOException {
		this();
		open(stream, name);
	}
	
}
