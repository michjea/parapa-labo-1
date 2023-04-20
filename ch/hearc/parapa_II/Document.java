package ch.hearc.parapa_II;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Document {

	// Concurrent content
	private String content;
	private ReadWriteLock lock;

	// Other variables
	private String name;

	/**
	 * Constructor
	 * 
	 * @param name Name of the document
	 */
	public Document(String name) {
		this.name = name;

		lock = new ReentrantReadWriteLock();

		content = "No data";
	}

	/**
	 * Get document name
	 * 
	 * @return the name of the document
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get document content, accessed by readers
	 * 
	 * @return the content of the document
	 */
	public String readContent() {
		return content;
	}

	/**
	 * Set the document's content, accessed by writers
	 * 
	 * @param newContent New content of the document
	 */
	public void setContent(String newContent) {
		content = newContent;
	}

	public ReadWriteLock getLock() {
		return lock;
	}
}
