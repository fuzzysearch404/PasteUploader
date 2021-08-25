package main;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ui.MainFrame;

/**
 * The main entry point that should be used to start the program. Also contains
 * the command line parser for configuration.
 * 
 * @author Roberts Ziedins
 *
 */
public class Main {
	
	// TODO: Move to some kind of constant storing class.
	public final static String DEFAULT_IMAGE_UPLOAD_EXTENSION = "png";
	public final static String DEFAULT_TEXT_UPLOAD_EXTENSION = "txt";
	public final static String DEFAULT_IMAGE_UPLOAD_FILENAME = "upload";
	public final static String DEFAULT_TEXT_UPLOAD_FILENAME = "upload";
	
	// TODO: Move these to some kind of app's state storing class
	public static String mediaURL;
	public static String formFileKeyName;
	public static String customUploadFileName = null;
	public static String copyJSONFieldName = null;
	public static String[] extraParams;
	public static boolean autoQuit = false;
	
	public static UploadStrategy uploadStrategy = UploadStrategy.NONE;

	public static void main(String args[]) {
		if (GraphicsEnvironment.isHeadless())
			throw new HeadlessException(
					"This system is unsupported, because it is headless");

		final Option optionUploadUrl = Option.builder("u")
				.longOpt("URL")
				.required(true)
				.desc("Full URL of the upload service where the media "
						+ "will be uploaded to. REQUIRED")
				.hasArg()
				.build();
		final Option optionFormFileKeyName = Option.builder("f")
				.longOpt("FORM_FILE_KEY_NAME")
				.required(true)
				.desc("Name of the key in multipart/form-data for "
						+ "the media bytes that will be sent. REQUIRED")
				.hasArg()
				.build();
		final Option optionCustomFileName = Option.builder("n")
				.longOpt("CUSTOM_FILE_NAME")
				.required(false)
				.desc("Provides a custom file name for all uploads. "
						+ "The name can be any string and it does not "
						+ "have to match the name of the file you want to upload.")
				.hasArg()
				.build();
		final Option optionCopyJsonFieldName = Option.builder("c")
				.longOpt("COPY_JSON_FIELD_NAME")
				.required(false)
				.desc("This option enables automatically copying a string "
						+ "value from the resulting successful JSON response. "
						+ "For example, sharing link for the uploaded file. "
						+ "This does not support accessing nested JSON objects.")
				.hasArg()
				.build();
		final Option optionFormExtraParams = Option.builder("e")
				.longOpt("EXTRA_FORM_DATA")
				.required(false)
				.desc("Extra data for the multipart/form-data."
						+ "Format: key=value. Unlimited args")
				.numberOfArgs(Option.UNLIMITED_VALUES)
				.build();
		final Option optionAutoQuit = Option.builder("q")
				.longOpt("AUTO_QUIT")
				.required(false)
				.desc("Enables automatically exiting the program after successfully "
						+ "copying the desired JSON field with the -c option. "
						+ "This is option is ignored if the -c value is not present.")
				.build();

		final Options options = new Options();
		options.addOption(optionUploadUrl);
		options.addOption(optionFormFileKeyName);
		options.addOption(optionCustomFileName);
		options.addOption(optionCopyJsonFieldName);
		options.addOption(optionFormExtraParams);
		options.addOption(optionAutoQuit);
		
		CommandLine commandLine;
		try {
			CommandLineParser parser = new DefaultParser();
			commandLine = parser.parse(options, args);
			
			mediaURL = commandLine.getOptionValue("u");
			formFileKeyName = commandLine.getOptionValue("f");
			
			if (commandLine.hasOption("n"))
				customUploadFileName = commandLine.getOptionValue("n");
			
			if (commandLine.hasOption("c")) { 
				copyJSONFieldName = commandLine.getOptionValue("c");
				
				if (commandLine.hasOption("q"))
					autoQuit = true;
			}

			if (commandLine.hasOption("e"))
				extraParams = commandLine.getOptionValues("e");
			else
				extraParams = new String[0];
		} catch (ParseException e) {
			final HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp("java -jar PasteUploader.jar", options);

			System.err.println(e.getMessage());
			System.exit(1);
		}

		new MainFrame();
	}

}