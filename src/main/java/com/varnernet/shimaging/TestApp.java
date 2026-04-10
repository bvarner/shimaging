package com.varnernet.shimaging;

import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.print.*;

import com.varnernet.shimaging.source.*;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestApp extends JFrame {
	private static final Logger LOGGER = Logger.getLogger(TestApp.class.getName());

	ImageModel model;
	ImagePanel view;

	public TestApp() {
		super("Test Imaging Application!");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create a new model & view.
		model = new ImageModel();
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
					model.setSource(new ImageIOSource(chooser.getSelectedFile()));
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

		pack();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			TestApp ta = new TestApp();
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

