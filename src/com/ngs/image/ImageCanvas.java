package com.ngs.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Used by the ImagePanel to render an ImageModel.
 * 
 * ImageCanvas is aware of changes to the JViewport of the JScrollPane that 
 * contains it, and will set sub-clipping regions on the ImageModel for 
 * images larger than the size of the canvas.
 */
public class ImageCanvas extends JPanel implements Scrollable, ChangeListener {
	protected ImageModel model;
	
	private Dimension imageSize;
	/**
	 * This is set to the size of a JViewport if one is registered to send us ChangeEvents.
	 */
	protected Rectangle viewRect;
	
	private boolean allowSubClip;
	
	
	public ImageCanvas() {
		super();
		this.model = null;
		imageSize = null;
		viewRect = new Rectangle();
		
		allowSubClip = true;
	}
	
	
	/**
	 * Set the Model this canvas is responsible for rendering.
	 * 
	 * @param model The ImageModel to render.
	 */
	public void setModel(ImageModel model) {
		this.model = model;
	}
	
	/**
	 * If true, then if we are added as a ChangeListener to a JViewport, 
	 * we will invoke setClip() on the ImageModel to set the clipping region
	 * to that of JViewport. Ideally, we would only receive ChangeEvents for
	 * a JViewport which is our immediate parent. Override to disable this
	 * behavior, or make it otherwise conditional.
	 * 
	 * @return true
	 */
	protected boolean allowClippedMode() {
		return allowSubClip;
	}
	
	
	/**
	 * Programmatically enables / disables subclipping.
	 */
	public void setAllowSubClipping(boolean b) {
		// If we're allowing clipping and we're making it so we don't, then
		// we need to immediately clear the clip on the model.
		if (allowSubClip && !b) {
			model.clearClip();
		}
		this.allowSubClip = b;
	}
	
	/**
	 * Implements Scrollable
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}
	
	
	/**
	 * Implements Scrollable
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL) {
			return visibleRect.height;
		} else {
			return visibleRect.width;
		}
	}
	
	
	/**
	 * Implements Scrollable
	 */
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
	
	
	/**
	 * Implements Scrollable
	 */
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}
	
	
	/**
	 * Implements Scrollable
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL) {
			return visibleRect.height / 5;
		} else {
			return visibleRect.width / 5;
		}
	}
	
	
	/**
	 * Implements ChangeListener
	 */
	public void stateChanged(ChangeEvent ce) {
		// If the source was a JViewport, and we allowClippedMode(),
		// Set the Clip to that of the Viewport's bounds.
		JViewport viewport = (JViewport)ce.getSource();
		if (model != null && allowClippedMode()) {
			model.setClip(viewport.getViewRect());
		}
		viewRect = viewport.getViewRect();
	}
	
	
	/* Overridden JPanel & JComponent methods */
	/**
	 * Returns the pageSize of the current ImageModel, or the preferredSize of
	 * super()
	 */
	public Dimension getPreferredSize() {
		Dimension d = null;
		if (model != null) {
			d = model.getPageSize();
		}
		
		if (d == null) {
			d = super.getPreferredSize();
		}
		return d;
	}
	
	public void paintBehindImage(Graphics g) {
	}
	
	
	public void paintOverImage(Graphics g) {
	}
	
	
	/**
	 * Used to translate the given Rectangle (in source image coordinates) 
	 * to the current ImageModel's transform.
	 */
	private Rectangle translateSourceRect(Rectangle rect) {
		if (model != null && model.getImage() != null) {
			//System.out.println("   Performing Rect Translation.");
			
			// Translate the starting point.
			Point2D start = model.getTransform().transform(rect.getLocation(), null);
			//System.out.println("   X Start Point: " + start);
			
			
			// Create the end point based on the start offset by the source
			// width & height
			Point end = new Point(rect.getLocation());
			end.translate(rect.width, rect.height);
			//System.out.println("   O End Point: " + end);
			
			// Translate the end point
			Point2D extent = model.getTransform().transform(end, null);
			//System.out.println("   X Extent Point: " + extent);
			
			rect = new Rectangle((int)start.getX(), (int)start.getY(),
			                     (int)(extent.getX() - start.getX()), 
			                     (int)(extent.getY() - start.getY()));
			//System.out.println("   X Rect: " + rect);
		}
		return rect;
	}
	
	
	/**
	 * Overrides the default scrollRectToVisible from JComponent.
	 * We first apply the active transform from the imageModel (if any) and
	 * pass the resulting Rect to the super-classes implementation.
	 */
	public void scrollRectToVisible(Rectangle rect) {
		Rectangle r = translateSourceRect(rect);
		super.scrollRectToVisible(r);
	}
	
	/**
	 * Paints the current Image of the ImageModel.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (model != null && model.getImage() != null) {
			Graphics2D g2d = (Graphics2D)g;
			if (model.isInverted()) {
				g2d.setColor(Color.BLACK);
			} else {
				g2d.setColor(Color.WHITE);
			}
			
			BufferedImage image = model.getImage();
			Rectangle clip = model.getClip();
			
			if (clip != null) {
				g2d.fillRect(clip.x, clip.y, image.getWidth(), image.getHeight());
				
				paintBehindImage(g);
				
				g2d.drawImage(model.getImage(), clip.x, clip.y, null);
// draw a red border when using clipping regions.
//				g2d.setColor(Color.RED);
//				g2d.draw(new Rectangle(clip.x, clip.y, image.getWidth() - 1, image.getHeight() - 1));
			} else {
				g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
				
				paintBehindImage(g);
				
				g2d.drawImage(model.getImage(), 0, 0, null);
// draw a green border when NOT using clipping regions.
//				g2d.setColor(Color.GREEN);
//				g2d.draw(new Rectangle(0, 0, image.getWidth() - 1, image.getHeight() - 1));
			}
			
			paintOverImage(g);
		}
	}
}