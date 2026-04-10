package com.shimaging;

import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.AlphaComposite;

import com.shimaging.source.*;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import java.io.*;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CompositeTiffTestApp extends JFrame {
	private static final Logger LOGGER = Logger.getLogger(CompositeTiffTestApp.class.getName());

	ImageSource source;
	ImageModel model;
	ImagePanel view;
	
	public CompositeTiffTestApp() {
		super("Test Imaging Application!");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			// Setup a composite source
			CompositeSource comp = new ScalingCompositeSource();
			
			// Add the first layer.
			comp.addLayer(new TiffTranslucentSource(new File("TestImages/150812000400.tif")));

			// Setup a pageless PNG source.
			PagelessSource ps = new PagelessImageIOSource(new File("TestImages/CMS1500.png"));
			// Tell it to fake as many pages as the composite source currently has.
			ps.setFakePages(comp.getImageCount());
			// Add the pageless PNG as a layer, with DST_OVER compositing.
			comp.addLayer(ps, AlphaComposite.DstOver);
			
			// Set our source as the composite.
			source = comp;
			
			// Create a new model & view.
			model = new ImageModel(source);
			view = new ImagePanel(model);
			setContentPane(view);
			
			JMenuBar mnuBar = new JMenuBar();
			
			JMenu mnuFile = new JMenu("File");
			JMenuItem mnuFileClose = new JMenuItem("Close");
			mnuFileClose.addActionListener(ae -> model.removeSource());
			JMenuItem mnuFileOpen = new JMenuItem("Open");
			mnuFileOpen.addActionListener(ae -> {
				JFileChooser chooser = new JFileChooser();
				int choice = chooser.showOpenDialog(null);
				if (choice == JFileChooser.APPROVE_OPTION) {
					model.removeSource();
					try {
						source = new ImageIOSource(chooser.getSelectedFile());
						model.setSource(source);
					} catch (Exception ex) {
						LOGGER.log(Level.WARNING, "Failed to open selected image.", ex);
					}
				}
			});
			
			JMenuItem mnuFilePrint = new JMenuItem("Print");
			mnuFilePrint.addActionListener(ae -> new PrintThread(model).start());

			JMenuItem mnuFileExit = new JMenuItem("Exit");
			mnuFileExit.addActionListener(ae -> System.exit(0));

			mnuFile.add(mnuFileOpen);
			mnuFile.add(mnuFileClose);
			mnuFile.addSeparator();
			mnuFile.add(mnuFilePrint);
			mnuFile.addSeparator();
			mnuFile.add(mnuFileExit);
			
			
			mnuBar.add(mnuFile);
			mnuBar.add(view.getMenu());
			
			JMenu mnuTest = new JMenu("Test Functions");
			JMenuItem mnuScrollTo = new JMenuItem("Scroll To Rect");
			mnuScrollTo.addActionListener(ae -> {
				String rectDef = JOptionPane.showInputDialog("Enter Rectangle dimensions as \"x, y, width, height\"");
				if (rectDef == null) {
					return;
				}
				StringTokenizer st = new StringTokenizer(rectDef, ",", false);
				if (st.countTokens() == 4) {
					try {
						int x = Integer.parseInt(st.nextToken().trim());
						int y = Integer.parseInt(st.nextToken().trim());
						int width = Integer.parseInt(st.nextToken().trim());
						int height = Integer.parseInt(st.nextToken().trim());

						Rectangle r = new Rectangle(x, y, width, height);
						view.getCanvas().scrollRectToVisible(r);
					} catch (Exception ex) {
						LOGGER.log(Level.FINE, "Invalid rectangle input: " + rectDef, ex);
					}
				}
			});
			
			mnuTest.add(mnuScrollTo);
			mnuBar.add(mnuTest);
			
			setJMenuBar(mnuBar);
		} catch (IOException ioe) {
			LOGGER.log(Level.WARNING, "Failed to initialize composite test app.", ioe);
		}
		
		pack();
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			CompositeTiffTestApp ta = new CompositeTiffTestApp();
			ta.setVisible(true);
		});
	}
	
	private class PrintThread extends Thread {
		ImageModel model;
		
		PrintThread(ImageModel model) {
			super("Printing");
			this.model = model;
		}
		
		@Override
		public void run() {
			PrinterJob job = PrinterJob.getPrinterJob();
			PageFormat printFormat = job.pageDialog(new PageFormat());
			
			job.setJobName("Shimaging: " + model.getSource().getImageName());
			
			Book b = new Book();
			b.append(model, printFormat, model.getImageCount());
			job.setPageable(b);
			
			boolean doPrint = job.printDialog();
			
			if (doPrint) {
				try {
					job.print();
				} catch (PrinterException e) {
				/* The job did not successfully complete */
				}
			}
		}
	}
}

