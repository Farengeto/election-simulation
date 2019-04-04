package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

public class TextListener implements ActionListener {
	private ElectionsUI GUI;
	private JComponent in;
	private JComponent out;
	private JFileChooser fc;

	public TextListener(ElectionsUI g, JComponent in, JComponent out) {
		GUI = g;
		fc = new JFileChooser();
		//fc.addChoosableFileFilter(new TextFilter());
		fc.setFileFilter(new TextFilter());
		this.in = in;
		this.out = out;
	}

	/**
	 * Imports the Election data from a text file
	 *
	 * @param e
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == in) {
			int returnVal = fc.showOpenDialog(GUI.getFrame());

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				GUI.loadFromText(file);
			}
		} else if (e.getSource() == out) {
			int returnVal = fc.showSaveDialog(GUI.getFrame());

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				GUI.saveToText(file);
			}
		} else {
			System.err.println("Source not recognized.");
		}
	}
}