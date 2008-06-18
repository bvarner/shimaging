package com.ngs.image;

import java.awt.AWTEvent;

public class ImageEvent extends AWTEvent {
	public static final int IMAGE_INVALID = 2 + AWTEvent.RESERVED_ID_MAX;
	public static final int IMAGE_ERROR = 4 + AWTEvent.RESERVED_ID_MAX;
	public static final int IMAGE_RESIZE = 6 + AWTEvent.RESERVED_ID_MAX;
	
	private int page;
	private String message;
	
	/**
	 * Creates a new event with the specified source, type, page number,
	 * and message.
	 * 
	 * @param source The object originating the event
	 * @param ID The IMAGE_xxxx type of the event
	 * @param page The page which the event type referrs to
	 * @param message The Message to go along with the event.
	 */
	public ImageEvent(Object source, int ID, int page, String message) {
		super(source, ID);
		this.page = page;
		this.message = message;
	}
	
	
	/**
	 * Creates a new event with the specified source, type, and page number.
	 * 
	 * @param source The object originating the event
	 * @param ID The IMAGE_xxxx type of the event
	 * @param page The page which the event type referrs to
	 */
	public ImageEvent(Object source, int ID, int page) {
		this(source, ID, page, "");
	}
	
	
	/**
	 * Creates a new event with the specified source, ID, and Message.
	 *
	 * @param source The object originating the event.
	 * @param ID the IMAGE_xxxx type of the event.
	 * @param message The message to go along with the event.
	 */
	public ImageEvent(Object source, int ID, String message) {
		this(source, ID, 0, message);
	}
	
	/**
	 * Creates a new event with the specified source and type
	 * This constructor is for events which do NOT have associated pages 
	 * such as IMAGE_INVALID or IMAGE_ERROR. If a ImageEvent is 
	 * created with this construstor, the page will always be zero.
	 * 
	 * @param source The object originating the event
	 * @param ID The IMAGE_xxxx type of the event
	 */
	public ImageEvent(Object source, int ID) {
		this(source, ID, 0);
	}
	
	/**
	 * Gets the page this Event is describing.
	 */
	public int getPage() {
		return page;
	}
	
	/**
	 * Gets the message for this event
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Gets the type of event.
	 */
	public int getType() {
		return getID();
	}
	
	
	public String toString() {
		return "ImageEvent source[" + source + "] id[" + id + "] page[" + page + "]";
	}
	
}