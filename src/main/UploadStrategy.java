package main;

/**
 * Defines the available strategies that can be used to prepare the file for
 * uploading.
 * 
 * @author Roberts Ziedins
 *
 */
public enum UploadStrategy {
	/**
	 * Placeholder / strategy not set.
	 */
	NONE,
	/**
	 * Represents a strategy to prepare the file as an PNG format image.
	 */
	PASTE_IMAGE,
	/**
	 * Represents a strategy to prepare the file as a TXT file format file.
	 */
	PASTE_TEXT,
	/**
	 * Represents a strategy to prepare the file based on it's format extension in
	 * the filename.
	 */
	SYSTEM_FILE
}
