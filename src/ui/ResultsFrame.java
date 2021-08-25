package ui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTextArea;

/**
 * JFrame class for the results window pop up that displays the resulting JSON
 * response.
 * 
 * @author Roberts Ziedins
 *
 */
public class ResultsFrame extends JFrame {

	private static final long serialVersionUID = -4644619724924373993L;
	private static final int PREFERED_WINDOW_WIDTH = 400;
	private static final int PREFERED_WINDOW_HEIGHT = 400;

	private JTextArea responseBodyTextArea;

	public ResultsFrame (int responseCode, String resultBody) {
		setTitle("Response: " + responseCode);
		setPreferredSize(new Dimension(PREFERED_WINDOW_WIDTH, PREFERED_WINDOW_HEIGHT));
		setLocationRelativeTo(null);

		this.responseBodyTextArea = new JTextArea(resultBody);
		this.responseBodyTextArea.setEditable(false);

		add(responseBodyTextArea);

		pack();
		setVisible(true);
	}
}
