package com.shimaging;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

class IconCache {
	private static final Logger LOGGER = Logger.getLogger(IconCache.class.getName());

	static HashMap<String, ImageIcon> cache = new HashMap<String, ImageIcon>();
	
	public static ImageIcon getIcon(String name) {
		ImageIcon icon = cache.get(name);
		if (icon == null) {
			try {
				icon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(name));
				cache.put(name, icon);
			} catch (Exception ex) {
				LOGGER.log(Level.WARNING, "Unable to load icon: " + name, ex);
			}
		}
		return icon;
	}
}