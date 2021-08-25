package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.http.client.methods.CloseableHttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;

import main.Main;
import main.UploadStrategy;
import uploader.FileUploader;
import uploader.JSONParser;

/**
 * The main frame of the application that is displayed to the user after a
 * successful startup. Contains the preview of the image/text that is going to
 * be uploaded and file picker to upload a specific file from the system. Files
 * can also be directly dragged into the frame instead of being chosen from the
 * file picker. The frame also has a key listener for "ctrl + v" keys combo to
 * swap the currently loaded media to a the current clipboard content.
 * 
 * @author Roberts Ziedins
 * 
 */
public class MainFrame extends JFrame implements KeyListener {

	private static final long serialVersionUID = 2269971701250845501L;
	private final static int PREFERED_MEDIA_PREVIEW_WIDTH = 800;
	private final static int PREFERED_MEDIA_PREVIEW_HEIGHT = 500;
	
	private final static Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
	private final DragAndDropListener dragAndDroplistener = new DragAndDropListener();
	
	// The actual data to upload
	private BufferedImage uploadBufferedImage;
	private String uploadTextString;
	private File uploadCustomFile;

	// Frame contents
	private JLabel previewHeaderText;
	private JComponent mediaPreview;
	
	private JPanel buttonsPanel = new JPanel(new FlowLayout());
	private JButton startUploadButton;
	private JButton selectFileButton;
	
	// KeyListener keys pressed states for "ctrl + v" combo
	private boolean keyControlPressed = false;
	private boolean keyVPressed = false;

	public MainFrame() {
		// Basic frame settings
		setTitle("PasteUploader");
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		new DropTarget(this, dragAndDroplistener);

		// Start building the window contents
		build();
		pack();
		setLocationRelativeTo(null);
		
		// Ready to display
		setVisible(true);
	}

	private void createPreviewHeaderText() {
		previewHeaderText = new JLabel("Content to upload preview:");
		previewHeaderText.setFont(new Font(null, Font.BOLD, 22));
	}

	private void createUploadContentPreview() {
		// SYSTEM_FILE strategy will never be selected on startup
		if (Main.uploadStrategy.equals(UploadStrategy.SYSTEM_FILE)) {
			mediaPreview = new JLabel("Selected file: " + uploadCustomFile.getAbsolutePath());
			
			return;
		}
		
		try {
			// Try loading in the clipboard contents as an image
			this.uploadBufferedImage = (BufferedImage) CLIPBOARD.getData(DataFlavor.imageFlavor);
			mediaPreview = new JLabel(new ImageIcon(uploadBufferedImage));

			Main.uploadStrategy = UploadStrategy.PASTE_IMAGE;
		} catch (Exception e) {
			try {
				// If that fails - try loading as a text
				this.uploadTextString = (String) CLIPBOARD.getData(DataFlavor.stringFlavor);
				mediaPreview = new JTextArea(uploadTextString);
				// Here we have to attach the drag and drop listener
				// to the JTextArea, otherwise we can't drop files
				// into the whole text area unlike on JLabel
				new DropTarget(mediaPreview, dragAndDroplistener);
				
				Main.uploadStrategy = UploadStrategy.PASTE_TEXT;
			} catch (Exception e1) {
				// If that fails too - let the user know that it could be empty
				mediaPreview = new JLabel("The clipboard seems to be empty... "
						+ "Try pasting something into this window or drag a file into this window");
				mediaPreview.setForeground(Color.RED);
				
				Main.uploadStrategy = UploadStrategy.NONE;
			}
		}

		mediaPreview.setFocusable(false);
		mediaPreview.setPreferredSize(new Dimension(PREFERED_MEDIA_PREVIEW_WIDTH, PREFERED_MEDIA_PREVIEW_HEIGHT));
	}
	
	private void createStartUploadButton() {
		startUploadButton = new JButton("Upload!");
		startUploadButton.setFocusable(false);

		startUploadButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleFileUpload();
			}
		});
		
		buttonsPanel.add(startUploadButton);
	}
	
	private void handleUsingSystemFile(File file) {
		uploadCustomFile = file;
		Main.uploadStrategy = UploadStrategy.SYSTEM_FILE;
		
		rebuild();
	}
	
	private void createSelectFileButton() {
		selectFileButton = new JButton("Choose file");
		selectFileButton.setFocusable(false);
		
		selectFileButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fileChooser = new JFileChooser();
				final int status = fileChooser.showOpenDialog(null);

				if (status == JFileChooser.APPROVE_OPTION) {
					final File file = fileChooser.getSelectedFile();
					
					if (file != null)
						handleUsingSystemFile(file);
				}
			}
		});

		buttonsPanel.add(selectFileButton);
	}

	private void handleFileUpload() {
		try {
			FileUploader uploader = null;
			
			switch (Main.uploadStrategy) {
				case PASTE_IMAGE:
					uploader = new FileUploader(uploadBufferedImage);
					break;
				case PASTE_TEXT:
					uploader = new FileUploader(uploadTextString);
					break;
				case SYSTEM_FILE:
					uploader = new FileUploader(uploadCustomFile);
					break;
				case NONE:
				default:
					throw new IllegalArgumentException("Invalid upload strategy");
			}
			
			final CloseableHttpResponse uploadResult = uploader.upload();
			
			JSONParser parser;
			String formattedJSON = "", copyFieldValue = "";
			try {
				parser = new JSONParser(new String(uploadResult.getEntity().getContent().readAllBytes()));
				formattedJSON = parser.beautifyJSON();
				
				if (Main.copyJSONFieldName != null && !Main.copyJSONFieldName.isBlank())
					copyFieldValue = parser.getJSONKeyValue();
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
			}
			
			if (!copyFieldValue.isEmpty()) {
				Toolkit.getDefaultToolkit()
						.getSystemClipboard()
						.setContents(new StringSelection(copyFieldValue), null);
				
				if (Main.autoQuit)
					System.exit(0);
			}

			new ResultsFrame(uploadResult.getStatusLine().getStatusCode(), formattedJSON);
		} catch (Exception e2) {
			JOptionPane.showMessageDialog(new JFrame(), e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e2.printStackTrace();
		}
	}

	private void build() {
		createPreviewHeaderText();
		add(previewHeaderText, BorderLayout.NORTH);
		createUploadContentPreview();
		add(mediaPreview, BorderLayout.CENTER);
		createStartUploadButton();
		createSelectFileButton();
		add(buttonsPanel, BorderLayout.SOUTH);
	}

	private void rebuild() {
		getContentPane().removeAll();
		buttonsPanel.removeAll();
		build();

		getContentPane().revalidate();
		getContentPane().repaint();
		pack();
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_CONTROL:
				keyControlPressed = false;
				break;
			case KeyEvent.VK_V:
				keyVPressed = false;
				break;
			case KeyEvent.VK_ENTER:
				startUploadButton.doClick();
				break;
			default:
				break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_CONTROL:
				keyControlPressed = true;
				break;
			case KeyEvent.VK_V:
				keyVPressed = true;
				break;
			default:
				break;
		}

		if (keyControlPressed && keyVPressed) {
			// There could be a system file selected that would block
			// us from loading in the new clipboard contents
			Main.uploadStrategy = UploadStrategy.NONE;
			
			rebuild();
		}
	}
	
	/**
	 * This class implements {@link DropTargetListener} that allows detecting
	 * dropped system files into the app's frame.
	 * 
	 * @author Roberts Ziedins
	 *
	 */
	private class DragAndDropListener implements DropTargetListener {

		@SuppressWarnings("unchecked")
		@Override
		public void drop(DropTargetDropEvent dtde) {
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
			
			final Transferable transferable = dtde.getTransferable();
			final DataFlavor[] flavors = transferable.getTransferDataFlavors();
			
			for (DataFlavor flavor : flavors) {
				if (flavor.isFlavorJavaFileListType()) {
					try {
						final List<File> files = (List<File>) transferable.getTransferData(flavor);
						// Only support one file upload at a time
						handleUsingSystemFile(files.get(0));
					} catch (UnsupportedFlavorException | IOException e) {
						e.printStackTrace();
					}
					
				}
			}
		}

		@Override public void dragEnter(DropTargetDragEvent dtde) {}
		@Override public void dragOver(DropTargetDragEvent dtde) {}
		@Override public void dropActionChanged(DropTargetDragEvent dtde) {}
		@Override public void dragExit(DropTargetEvent dte) {}

	}

}
