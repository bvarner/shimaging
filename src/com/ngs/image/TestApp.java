package com.ngs.image;

import java.awt.*;
import java.awt.event.*;
import java.awt.AlphaComposite;

import com.ngs.image.source.*;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import java.io.*;

public class TestApp extends JFrame {
	ImageSource source;
	ImageModel model;
	ImagePanel view;
	
	public TestApp() {
		super("Test Imaging Application!");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			// Setup a composite source
			CompositeSource comp = new ScalingCompositeSource();
			
			// Add the first layer.
			comp.addLayer(new JAITiffTranslucentSource(new File("TestImages/150812000400.tif")));
			
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
			mnuBar.add(view.getMenu());
			setJMenuBar(mnuBar);
		} catch (IOException ioe) {
			System.err.println(ioe.toString());
		}
		
		pack();
	}
	
	public void show() {
		super.show();
	}
	
	public static void main(String[] args) {
		TestApp ta = new TestApp();
		ta.show();
	}
}

