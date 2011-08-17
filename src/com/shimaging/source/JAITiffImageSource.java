package com.shimaging.source;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;

import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import com.sun.media.jai.codec.TIFFDirectory;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;


/**
 * An ImageSource that reads TIFF images from File or SeekableStreams using JAI.
 */
public class JAITiffImageSource extends JAIImageSource {
	RenderingHints hints;
	
	protected JAITiffImageSource() {
		super();
		hints = new RenderingHints(null);
	}
	
	
	public JAITiffImageSource(final File f) throws IOException {
		this();
		open(f);
	}
	
	
	public JAITiffImageSource(final InputStream is, final String name) throws IOException {
		this();
		open(is, name);
	}
	
	
	public void addRenderingHints(final RenderingHints nHints) {
		this.hints.add(nHints);
	}
	
	
	protected final void open(final File f) throws IOException {
		open(new FileSeekableStream(f), f.getName());
	}
	
	
	protected final void open(final InputStream is, final String name) throws IOException {
		open(new MemoryCacheSeekableStream(is), name);
	}
	
	
	protected void readStream(final SeekableStream stream) throws IOException {
		int totalPages = TIFFDirectory.getNumDirectories(stream);
		long nextOffset = 0;
		
		for (int page = 0; page < totalPages; page++) {
			TIFFDecodeParam decodeParam = new TIFFDecodeParam();
			
			if (nextOffset != 0) {
				decodeParam.setIFDOffset(nextOffset);
			}
			
			ParameterBlock pBlock = new ParameterBlock();
			pBlock.add(stream);
			pBlock.add(decodeParam);
			
			RenderedOp image = JAI.create("tiff", pBlock, hints);
			
			// Cache the image
			cachePage(new ImagePage(page, image));
			
			TIFFDirectory dir = (TIFFDirectory)image.getProperty("tiff_directory");
			nextOffset = dir.getNextIFDOffset();
		}
	}
}