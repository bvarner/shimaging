package com.shimaging.image.source;

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
	
	public PagelessImageIOSource(File f) throws IOException {
		this();
		image = ImageIO.read(f);
		name = f.getName();
	}
	
	public PagelessImageIOSource(URL url) throws IOException {
		this();
		image = ImageIO.read(url);
		name = url.getFile();
	}
	
	public PagelessImageIOSource(ImageInputStream stream) throws IOException {
		this();
		image = ImageIO.read(stream);
	}
	
	public PagelessImageIOSource(InputStream stream) throws IOException {
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
