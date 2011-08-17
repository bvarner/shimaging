package com.shimaging.image.source;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * This source uses ImageIO to load Images.
 */
public class ImageIOSource extends DefaultThumbnailSource {
	BufferedImage image;
	String name;
	
	public ImageIOSource() {
		image = null;
		name = "";
	}
	
	public ImageIOSource(File f) throws IOException {
		this();
		image = ImageIO.read(f);
		name = f.getName();
	}
	
	public ImageIOSource(URL url) throws IOException {
		this();
		image = ImageIO.read(url);
		name = url.getFile();
	}
	
	public ImageIOSource(ImageInputStream stream) throws IOException {
		this();
		image = ImageIO.read(stream);
	}
	
	public ImageIOSource(InputStream stream) throws IOException {
		this();
		image = ImageIO.read(stream);
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public BufferedImage getImage(int index) {
		if (index > 0) {
			return null;
		}
		return getImage();
	}
	
	
	public String getImageName() {
		return name;
	}
	
	
	public void setImageName(String name) {
		this.name = name;
	}
	
	
	public int getImageCount() {
		if (image != null) {
			return 1;
		}
		return 0;
	}
	
	public void dispose() {
		if (image != null) {
			image.flush();
			image = null;
		}
	}
}
