package com.varnernet.shimaging.source;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;


/**
 * An ImageSource that reads TIFF images from files or streams using ImageIO.
 */
public class TiffImageSource extends ImageIOPageSource {
	RenderingHints hints;
	
	protected TiffImageSource() {
		super();
		hints = new RenderingHints(null);
	}
	
	
	public TiffImageSource(final File f) throws IOException {
		this();
		open(f);
	}
	
	
	public TiffImageSource(final InputStream is, final String name) throws IOException {
		this();
		open(is, name);
	}
	
	
	public void addRenderingHints(final RenderingHints nHints) {
		this.hints.add(nHints);
	}
	
	
	protected final void open(final File f) throws IOException {
		try (InputStream stream = new FileInputStream(f)) {
			open(stream, f.getName());
		}
	}
	
	
	protected final void open(final InputStream is, final String name) throws IOException {
		super.open(is, name);
	}
	
	
	protected void readStream(final InputStream stream) throws IOException {
		try (ImageInputStream input = ImageIO.createImageInputStream(stream)) {
			if (input == null) {
				throw new IOException("Unable to create ImageInputStream for TIFF source.");
			}

			Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("TIFF");
			if (!readers.hasNext()) {
				throw new IOException("No TIFF ImageIO reader is available on the classpath.");
			}

			ImageReader reader = readers.next();
			try {
				reader.setInput(input, false, true);
				int totalPages = reader.getNumImages(true);
				for (int page = 0; page < totalPages; page++) {
					BufferedImage image = reader.read(page);
					cachePage(new ImagePage(page, transformImage(image)));
				}
			} finally {
				reader.dispose();
			}
		}
	}

	protected BufferedImage transformImage(final BufferedImage image) {
		return image;
	}
}