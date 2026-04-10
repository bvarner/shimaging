package com.varnernet.shimaging.source;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.awt.image.BufferedImage;

/**
 * An ImageSource that reads TIFF images, uses an IndexedColorModel with valid 
 * values for Black and White, where White pixels are TRANSPARENT.
 * 
 * This lets you read B&W TIFF images, and do some advanced things with 
 * compositing.
 */
public class TiffTranslucentSource extends TiffImageSource {
	protected TiffTranslucentSource() {
		// no-op
	}
	
	public TiffTranslucentSource(final File f) throws IOException {
		this();
		open(f);
	}
	
	public TiffTranslucentSource(final InputStream stream, final String name) throws IOException {
		this();
		open(stream, name);
	}

	@Override
	protected BufferedImage transformImage(final BufferedImage image) {
		BufferedImage translucent = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int rgb = image.getRGB(x, y) & 0x00FFFFFF;
				if (rgb == 0x00FFFFFF) {
					translucent.setRGB(x, y, rgb);
				} else {
					translucent.setRGB(x, y, 0xFF000000 | rgb);
				}
			}
		}
		return translucent;
	}

}
