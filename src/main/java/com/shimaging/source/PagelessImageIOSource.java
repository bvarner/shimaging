package com.shimaging.source;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * This Pageless source uses ImageIO to load Images.
 */
public class PagelessImageIOSource extends PagelessSource {
	BufferedImage image;
	String name;
	
	PagelessImageIOSource() {
		super(1);
		image = null;
		name = "";
	}
	
	public PagelessImageIOSource(final File f) throws IOException {
		this();
		image = ImageIO.read(f);
		name = f.getName();
	}
	
	public PagelessImageIOSource(final URL url) throws IOException {
		this();
		image = ImageIO.read(url);
		name = url.getFile();
	}
	
	public PagelessImageIOSource(final ImageInputStream stream) throws IOException {
		this();
		image = ImageIO.read(stream);
	}
	
	public PagelessImageIOSource(final InputStream stream) throws IOException {
		this();
		image = ImageIO.read(stream);
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public String getImageName() {
		return name;
	}
	
	public void dispose() {
		image.flush();
		image = null;
	}
}
