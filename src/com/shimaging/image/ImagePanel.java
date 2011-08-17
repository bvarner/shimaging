package com.shimaging.image;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.JOptionPane;

/**
 * A Custom Panel for interacting with an ImageModel.
 *
 * 
 * @author Bryan.Varner
 */
public final class ImagePanel extends JPanel implements ImageEventListener {
	public static final int SCROLL_BLOCK = 0;
	public static final int SCROLL_UNIT = 1;
	public static final int SCROLL_MAX = 2;
	
	public static final int SCROLL_UP = 0;
	public static final int SCROLL_DOWN = 1;
	public static final int SCROLL_LEFT = 2;
	public static final int SCROLL_RIGHT = 3;
	
	
	protected JScrollPane scrollPane;
	protected ImageModel mdlImage;
	
	private ArrayList<ActionListener> listeners;
	
	protected JToolBar toolBar;
	public JButton btnPrev;
	public JButton btnNext;
	public JButton btnIn;
	public JButton btnOut;
	public JToggleButton btnFit;
	public JToggleButton btnFitWidth;
	public JToggleButton btnFitHeight;
	public JButton btnLighten;
	public JButton btnDarken;
	public JButton btnContUp;
	public JButton btnContDn;
	public JButton btnInvert;
	public JButton btnRotCW;
	public JButton btnRotCCW;
	
	protected JMenu mnuImage;
	public JMenuItem prevPage;
	public JMenuItem nextPage;
	public JMenuItem zoomIn;
	public JMenuItem zoomOut;
	public JMenuItem invert;
	public JMenuItem contrastUp;
	public JMenuItem contrastDown;
	public JMenuItem lighter;
	public JMenuItem darker;
	public JMenuItem rotateClockwise;
	public JMenuItem rotateCounterClockwise;
	public JCheckBoxMenuItem fitWidth;
	public JCheckBoxMenuItem fitHeight;
	public JCheckBoxMenuItem fitWindow;
	public JMenuItem preserveAspect;
	
	
	protected ImageCanvas canvas;

	public ImagePanel() {
		super(new BorderLayout());
		
		setFocusable(true);
		
		listeners = new ArrayList<ActionListener>();
		
		canvas = createImageCanvas();
		
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.getViewport().addChangeListener(canvas);
		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		scrollPane.setViewportView(canvas);
		
		btnPrev = new JButton(IconCache.getIcon("com/ngs/image/icons/prev_page_16.png"));
		btnPrev.setFocusable(false);
		btnPrev.setActionCommand("Previous Page");
		btnPrev.setToolTipText("Previous Page");
		btnNext = new JButton(IconCache.getIcon("com/ngs/image/icons/next_page_16.png"));
		btnNext.setFocusable(false);
		btnNext.setActionCommand("Next Page");
		btnNext.setToolTipText("Next Page");
		btnIn = new JButton(IconCache.getIcon("com/ngs/image/icons/zoom_in_16.png"));
		btnIn.setFocusable(false);
		btnIn.setActionCommand("Zoom In");
		btnIn.setToolTipText("Zoom In");
		btnOut = new JButton(IconCache.getIcon("com/ngs/image/icons/zoom_out_16.png"));
		btnOut.setFocusable(false);
		btnOut.setActionCommand("Zoom Out");
		btnOut.setToolTipText("Zoom Out");
		btnFit = new JToggleButton(IconCache.getIcon("com/ngs/image/icons/zoom_page_16.png"));
		btnFit.setFocusable(false);
		btnFit.setActionCommand("Fit To Window");
		btnFit.setToolTipText("Fit To Window");
		btnFitWidth = new JToggleButton(IconCache.getIcon("com/ngs/image/icons/zoom_pagewidth_16.png"));
		btnFitWidth.setFocusable(false);
		btnFitWidth.setActionCommand("Fit To Width");
		btnFitWidth.setToolTipText("Fit To Width");
		btnFitHeight = new JToggleButton(IconCache.getIcon("com/ngs/image/icons/zoom_pageheight_16.png"));
		btnFitHeight.setFocusable(false);
		btnFitHeight.setActionCommand("Fit To Height");
		btnFitHeight.setToolTipText("Fit To Height");
		btnLighten = new JButton(IconCache.getIcon("com/ngs/image/icons/lighten_16.png"));
		btnLighten.setFocusable(false);
		btnLighten.setActionCommand("Brighter");
		btnLighten.setToolTipText("Brighter");
		btnDarken = new JButton(IconCache.getIcon("com/ngs/image/icons/darken_16.png"));
		btnDarken.setFocusable(false);
		btnDarken.setActionCommand("Darker");
		btnDarken.setToolTipText("Darker");
		btnContUp = new JButton(IconCache.getIcon("com/ngs/image/icons/contrast_lighter_16.png"));
		btnContUp.setFocusable(false);
		btnContUp.setActionCommand("More Contrast");
		btnContUp.setToolTipText("More Contrast");
		btnContDn = new JButton(IconCache.getIcon("com/ngs/image/icons/contrast_darker_16.png"));
		btnContDn.setFocusable(false);
		btnContDn.setActionCommand("Less Contrast");
		btnContDn.setToolTipText("Less Contrast");
		btnInvert = new JButton(IconCache.getIcon("com/ngs/image/icons/invert_16.png"));
		btnInvert.setFocusable(false);
		btnInvert.setActionCommand("Invert Colors");
		btnInvert.setToolTipText("Invert Colors");
		btnRotCW = new JButton(IconCache.getIcon("com/ngs/image/icons/rorate_cw_16.png"));
		btnRotCW.setFocusable(false);
		btnRotCW.setActionCommand("Rotate Clockwise");
		btnRotCW.setToolTipText("Rotate Clockwise");
		btnRotCCW = new JButton(IconCache.getIcon("com/ngs/image/icons/rorate_ccw_16.png"));
		btnRotCCW.setFocusable(false);
		btnRotCCW.setActionCommand("Rotate Counter-Clockwise");
		btnRotCCW.setToolTipText("Rotate Counter-Clockwise");
		
		toolBar = new JToolBar();
		toolBar.setFocusable(false);
		toolBar.add(btnPrev);
		toolBar.add(btnNext);
		
		toolBar.add(btnIn);
		toolBar.add(btnOut);
		toolBar.add(btnFit);
		toolBar.add(btnFitWidth);
		toolBar.add(btnFitHeight);
		
		toolBar.add(btnLighten);
		toolBar.add(btnDarken);
		
		toolBar.add(btnContUp);
		toolBar.add(btnContDn);
		
		toolBar.add(btnInvert);
		
		toolBar.add(btnRotCCW);
		toolBar.add(btnRotCW);
		
		add(toolBar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		
		ImagePanelActionListener actionListener = new ImagePanelActionListener();

		btnPrev.addActionListener(actionListener);
		btnNext.addActionListener(actionListener);
		btnIn.addActionListener(actionListener);
		btnOut.addActionListener(actionListener);
		btnFit.addActionListener(actionListener);
		btnFitWidth.addActionListener(actionListener);
		btnFitHeight.addActionListener(actionListener);
		btnLighten.addActionListener(actionListener);
		btnDarken.addActionListener(actionListener);
		btnContUp.addActionListener(actionListener);
		btnContDn.addActionListener(actionListener);
		btnInvert.addActionListener(actionListener);
		btnRotCW.addActionListener(actionListener);
		btnRotCCW.addActionListener(actionListener);
		
		
		mnuImage = new JMenu("Image");
		mnuImage.setMnemonic(KeyEvent.VK_I);
		
		prevPage = createMenuItem("Previous Page", KeyEvent.VK_P,
								InputEvent.ALT_MASK, actionListener);
		nextPage = createMenuItem("Next Page", KeyEvent.VK_N,
								InputEvent.ALT_MASK, actionListener);
		
		mnuImage.add(prevPage);
		mnuImage.add(nextPage);
		mnuImage.addSeparator();
		
		zoomIn = createMenuItem("Zoom In", KeyEvent.VK_EQUALS, 
		                            InputEvent.ALT_MASK, actionListener);
		zoomOut = createMenuItem("Zoom Out", KeyEvent.VK_MINUS,
		                            InputEvent.ALT_MASK, actionListener);
		mnuImage.add(zoomIn);
		mnuImage.add(zoomOut);
		mnuImage.addSeparator();
		
		invert = createMenuItem("Invert Colors", actionListener);
		contrastUp = createMenuItem("More Contrast", actionListener);
		contrastDown = createMenuItem("Less Contrast", actionListener);
		lighter = createMenuItem("Brighter", actionListener);
		darker = createMenuItem("Darker", actionListener);
		rotateClockwise = createMenuItem("Rotate Clockwise", KeyEvent.VK_X, 
		                             InputEvent.ALT_MASK, actionListener);
		rotateCounterClockwise = createMenuItem("Rotate Counter-Clockwise",
								KeyEvent.VK_X, InputEvent.CTRL_MASK, actionListener);
		mnuImage.add(invert);
		mnuImage.add(contrastUp);
		mnuImage.add(contrastDown);
		mnuImage.add(lighter);
		mnuImage.add(darker);
		mnuImage.add(rotateClockwise);
		mnuImage.add(rotateCounterClockwise);
		mnuImage.addSeparator();
		fitWidth = new JCheckBoxMenuItem("Fit To Width");
		fitWidth.addActionListener(actionListener);
		mnuImage.add(fitWidth);
		
		fitHeight = new JCheckBoxMenuItem("Fit To Height");
		fitHeight.addActionListener(actionListener);
		mnuImage.add(fitHeight);
		
		fitWindow = new JCheckBoxMenuItem("Fit To Window");
		fitWindow.addActionListener(actionListener);
		mnuImage.add(fitWindow);
		
		preserveAspect = new JCheckBoxMenuItem("Preserve Aspect Ratio");
		preserveAspect.addActionListener(actionListener);
		mnuImage.add(preserveAspect);
	}
	
	
	public JMenu getMenu() {
		return mnuImage;
	}
	
	
	/**
	 * Creates a new ImagePanel that uses the given ImageModel.
	 */
	public ImagePanel(ImageModel model) {
		this();
		
		setModel(model);
	}
	
	
	/**
	 * Allows sub-classes of ImagePanel to supply custom ImageCanvases to use
	 * as the display for Images.
	 */
	protected ImageCanvas createImageCanvas() {
		return new ImageCanvas();
	}
	
	
	/**
	 * Make sure that if we don't have a model, we're at least 400 pixels tall
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		if (getModel() == null) {
			d.setSize(d.width, 400);
		}
		return d;
	}
	
	
	/**
	 * Adds an ActionListener.
	 */
	public void addActionListener(ActionListener al) {
		listeners.add(al);
	}
	
	/**
	 * Removes an ActionListener.
	 */
	public void removeActionListener(ActionListener al) {
		listeners.remove(al);
	}
	
	
	/**
	 * Updates the toolbar buttons to accurately reflect the current ImageModel
	 * state.
	 */
	public void updateToolbar() {
		if (mdlImage != null) {
			btnNext.setEnabled(mdlImage.getImagePage() + 1 < mdlImage.getImageCount());
			btnPrev.setEnabled(mdlImage.getImagePage() > 0);
			btnIn.setEnabled(mdlImage.getImage() != null && mdlImage.getScale() < 16);
			btnOut.setEnabled(mdlImage.getImage() != null && mdlImage.getScale() > 0.125);
			
			btnFit.setEnabled(mdlImage.getImage() != null);
			btnFit.setSelected(mdlImage.getFitMode() == ImageModel.FIT_BOTH);
			btnFitWidth.setEnabled(mdlImage.getImage() != null);
			btnFitWidth.setSelected(mdlImage.getFitMode() == ImageModel.FIT_WIDTH);
			btnFitHeight.setEnabled(mdlImage.getImage() != null);
			btnFitHeight.setSelected(mdlImage.getFitMode() == ImageModel.FIT_HEIGHT);
			
			btnLighten.setEnabled(mdlImage.getImage() != null);
			btnDarken.setEnabled(mdlImage.getImage() != null);
			btnContUp.setEnabled(mdlImage.getImage() != null);
			btnContDn.setEnabled(mdlImage.getImage() != null);
			btnInvert.setEnabled(mdlImage.getImage() != null);
			btnRotCW.setEnabled(mdlImage.getImage() != null);
			btnRotCCW.setEnabled(mdlImage.getImage() != null);
			
			nextPage.setEnabled(mdlImage.getImagePage() + 1 < mdlImage.getImageCount());
			prevPage.setEnabled(mdlImage.getImagePage() > 0);
			zoomIn.setEnabled(mdlImage.getImage() != null);
			zoomOut.setEnabled(mdlImage.getImage() != null);
			invert.setEnabled(mdlImage.getImage() != null);
			contrastUp.setEnabled(mdlImage.getImage() != null);
			contrastDown.setEnabled(mdlImage.getImage() != null);
			lighter.setEnabled(mdlImage.getImage() != null);
			darker.setEnabled(mdlImage.getImage() != null);
			rotateClockwise.setEnabled(mdlImage.getImage() != null);
			rotateCounterClockwise.setEnabled(mdlImage.getImage() != null);
			fitWindow.setEnabled(mdlImage.getImage() != null);
			fitWindow.setSelected(mdlImage.getFitMode() == ImageModel.FIT_BOTH);
			fitWidth.setEnabled(mdlImage.getImage() != null);
			fitWidth.setSelected(mdlImage.getFitMode() == ImageModel.FIT_WIDTH);
			fitHeight.setEnabled(mdlImage.getImage() != null);
			fitHeight.setSelected(mdlImage.getFitMode() == ImageModel.FIT_HEIGHT);
			preserveAspect.setEnabled(mdlImage.getImage() != null);
			preserveAspect.setSelected(mdlImage.getPreserveAspectRatio());
			
			updateMenus();
		} else {
			System.out.println("Model NULL");
			btnNext.setEnabled(false);
			btnPrev.setEnabled(false);
			btnIn.setEnabled(false);
			btnOut.setEnabled(false);
			btnFit.setEnabled(false);
			btnFitWidth.setEnabled(false);
			btnFitHeight.setEnabled(false);
			btnLighten.setEnabled(false);
			btnDarken.setEnabled(false);
			btnContUp.setEnabled(false);
			btnContDn.setEnabled(false);
			btnInvert.setEnabled(false);
			btnRotCW.setEnabled(false);
			btnRotCCW.setEnabled(false);
			
			nextPage.setEnabled(false);
			prevPage.setEnabled(false);
			zoomIn.setEnabled(false);
			zoomOut.setEnabled(false);
			invert.setEnabled(false);
			contrastUp.setEnabled(false);
			contrastDown.setEnabled(false);
			lighter.setEnabled(false);
			darker.setEnabled(false);
			rotateClockwise.setEnabled(false);
			rotateCounterClockwise.setEnabled(false);
		}
		updateMenus();
	}
	
	public void updateMenus() {
		if (mdlImage != null) {
			fitWindow.setSelected(btnFit.isSelected());
			fitWidth.setSelected(btnFitWidth.isSelected());
			fitHeight.setSelected(btnFitHeight.isSelected());
			preserveAspect.setSelected(mdlImage.getPreserveAspectRatio());
		} else {
			fitWindow.setEnabled(false);
			fitWidth.setEnabled(false);
			fitHeight.setEnabled(false);
			preserveAspect.setEnabled(false);
		}
	}
	
	/**
	 * Sets the ImageModel to render.
	 */
	public void setModel(ImageModel model) {
		mdlImage = model;
		mdlImage.addImageListener(this);
		mdlImage.render();
		canvas.setModel(mdlImage);
		canvas.revalidate();
		updateToolbar();
	}
	
	
	/**
	 * Gets the current ImageModel being rendered.
	 */
	public ImageModel getModel() {
		return mdlImage;
	}
	
	
	/**
	 * Gets the ImageCanvas used to render the ImageModel.
	 */
	public ImageCanvas getCanvas() {
		return canvas;
	}
	
	
	/**
	 * Programmatically scrolls based upon the given direction and speed.
	 */
	public void scroll(int direction, int speed) {
		if (direction == SCROLL_UP) {
			int newValue = scrollPane.getVerticalScrollBar().getValue();
			if (speed == SCROLL_BLOCK) {
				newValue -= scrollPane.getVerticalScrollBar().getBlockIncrement(-1);
			} else if (speed == SCROLL_UNIT) {
				newValue -= scrollPane.getVerticalScrollBar().getUnitIncrement(-1);				
			} else { // SCROLL_MAX
				newValue = scrollPane.getVerticalScrollBar().getMinimum();
			}
			scrollPane.getVerticalScrollBar().setValue(newValue);
		} else if (direction == SCROLL_DOWN) {
			int newValue = scrollPane.getVerticalScrollBar().getValue();
			if (speed == SCROLL_BLOCK) {
				newValue += scrollPane.getVerticalScrollBar().getBlockIncrement(1);
			} else if (speed == SCROLL_UNIT) {
				newValue += scrollPane.getVerticalScrollBar().getUnitIncrement(1);				
			} else { // SCROLL_MAX
				newValue = scrollPane.getVerticalScrollBar().getMaximum();
			}
			scrollPane.getVerticalScrollBar().setValue(newValue);
		} else if (direction == SCROLL_LEFT) {
			int newValue = scrollPane.getHorizontalScrollBar().getValue();
			if (speed == SCROLL_BLOCK) {
				newValue -= scrollPane.getHorizontalScrollBar().getBlockIncrement(-1);
			} else if (speed == SCROLL_UNIT) {
				newValue -= scrollPane.getHorizontalScrollBar().getUnitIncrement(-1);				
			} else { // SCROLL_MAX
				newValue = scrollPane.getHorizontalScrollBar().getMinimum();
			}
			scrollPane.getHorizontalScrollBar().setValue(newValue);
		} else if (direction == SCROLL_RIGHT) {
			int newValue = scrollPane.getHorizontalScrollBar().getValue();
			if (speed == SCROLL_BLOCK) {
				newValue += scrollPane.getHorizontalScrollBar().getBlockIncrement(1);
			} else if (speed == SCROLL_UNIT) {
				newValue += scrollPane.getHorizontalScrollBar().getUnitIncrement(1);				
			} else { // SCROLL_MAX
				newValue = scrollPane.getHorizontalScrollBar().getMaximum();
			}
			scrollPane.getHorizontalScrollBar().setValue(newValue);
		}
	}
	
	
	/**
	 * Implements the ImageEventListener.
	 */
	public void imageChanged(ImageEvent ie) {
		updateToolbar();
		
		if (ie.getType() == ImageEvent.IMAGE_RESIZE) {
			canvas.revalidate();
		} else {
			canvas.repaint();
		}
	}
	
	@Override
	public void removeNotify() {
		super.removeNotify();
		getModel().haltRender();
	}
	
	/**
	 * Implements the ImageEventListener.
	 * Any errors being emmited by the ImageModel are displayed in JOptionPane
	 * dialog boxes.
	 */
	public void imageError(ImageEvent ie) {
		JOptionPane.showMessageDialog(null, ie.getMessage(),
								"Image Error",
								JOptionPane.WARNING_MESSAGE);
	}
	
	
	/**
	 * Utility method to create a menu item
	 */
	private JMenuItem createMenuItem(String text, ActionListener listener) {
		return createMenuItem(text, 0, 0, 0, listener);
	}
	
	
	/**
	 * Utility method to create a menu item
	 */
	private JMenuItem createMenuItem(String text, int keyCode, 
							   int modifiers, ActionListener listener) 
	{
		return createMenuItem(text, 0, keyCode, modifiers, listener);
	}
	
	
	/**
	 * Utility method to create a menu item
	 */
	private JMenuItem createMenuItem(String text, int mnemonic, 
							   int keyCode, int modifiers,
							   ActionListener listener) 
	{
		JMenuItem item = new JMenuItem(text);
		if (keyCode != 0 && modifiers != 0) {
			item.setAccelerator(KeyStroke.getKeyStroke(keyCode, 
											   modifiers));
		}
		if (mnemonic != 0) {
			item.setMnemonic(mnemonic);
		}
		
		item.addActionListener(listener);
		return item;
	}

	/**
	 * Implements ActionListener.
	 *
	 * All the buttons / functions on the embedded Toolbar are
	 * processed first by the ImagePanel, then dispatched to any registered
	 * listeners.
	 */
	private class ImagePanelActionListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			if (ae.getActionCommand().equals("Previous Page")) {
				mdlImage.prevPage();
			} else if (ae.getActionCommand().equals("Next Page")) {
				mdlImage.nextPage();
			} else if (ae.getActionCommand().equals("Zoom In")) {
				if (mdlImage.getFitMode() != ImageModel.FIT_NONE) {
					mdlImage.setFitMode(null, ImageModel.FIT_NONE);
				}
				btnFit.setSelected(false);
				btnFitWidth.setSelected(false);
				btnFitHeight.setSelected(false);
				mdlImage.scaleBy((2.0f / 3.0f));
			} else if (ae.getActionCommand().equals("Zoom Out")) {
				if (mdlImage.getFitMode() != ImageModel.FIT_NONE) {
					mdlImage.setFitMode(null, ImageModel.FIT_NONE);
				}
				btnFit.setSelected(false);
				btnFitWidth.setSelected(false);
				btnFitHeight.setSelected(false);
				mdlImage.scaleBy(1.5f);
			} else if (ae.getActionCommand().equals("Preserve Aspect Ratio")) {
				mdlImage.setPreserveAspectRatio(!mdlImage.getPreserveAspectRatio());
				updateMenus();
			} else if (ae.getActionCommand().equals("Fit To Window")) {
				if (ae.getSource() == fitWindow) {
					btnFit.setSelected(!btnFit.isSelected());
					updateMenus();
				}

				if (btnFit.isSelected()) {
					btnFitWidth.setSelected(false);
					btnFitHeight.setSelected(false);
					mdlImage.setFitMode(scrollPane.getViewport().getViewRect(), ImageModel.FIT_BOTH);
				} else {
					mdlImage.setFitMode(null, ImageModel.FIT_NONE);
				}
			} else if (ae.getActionCommand().equals("Fit To Width")) {
				if (ae.getSource() == fitWidth) {
					btnFitWidth.setSelected(!btnFitWidth.isSelected());
					updateMenus();
				}

				if (btnFitWidth.isSelected()) {
					btnFit.setSelected(false);
					btnFitHeight.setSelected(false);
					mdlImage.setFitMode(scrollPane.getViewport().getViewRect(), ImageModel.FIT_WIDTH);
				} else {
					mdlImage.setFitMode(null, ImageModel.FIT_NONE);
				}
			} else if (ae.getActionCommand().equals("Fit To Height")) {
				if (ae.getSource() == fitHeight) {
					btnFitHeight.setSelected(!btnFitHeight.isSelected());
					updateMenus();
				}

				if (btnFitHeight.isSelected()) {
					btnFit.setSelected(false);
					btnFitWidth.setSelected(false);
					mdlImage.setFitMode(scrollPane.getViewport().getViewRect(), ImageModel.FIT_HEIGHT);
				} else {
					mdlImage.setFitMode(null, ImageModel.FIT_NONE);
				}
			} else if (ae.getActionCommand().equals("Brighter")) {
				mdlImage.adjustBrightness(25);
			} else if (ae.getActionCommand().equals("Darker")) {
				mdlImage.adjustBrightness(-25);
			} else if (ae.getActionCommand().equals("More Contrast")) {
				mdlImage.adjustContrast(0.1f);
			} else if (ae.getActionCommand().equals("Less Contrast")) {
				mdlImage.adjustContrast(-0.1f);
			} else if (ae.getActionCommand().equals("Invert Colors")) {
				mdlImage.invert();
			} else if (ae.getActionCommand().equals("Rotate Clockwise")) {
				mdlImage.addRotation(90);
			} else if (ae.getActionCommand().equals("Rotate Counter-Clockwise")) {
				mdlImage.addRotation(-90);
			}

			// Forward all events on to the registered listeners.
			for (ActionListener al : listeners) {
				al.actionPerformed(ae);
			}
		}
	}
}
