package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;


public class SaveResultsListener implements ActionListener {
	private ElectionGridFrame source;
	private JFileChooser fc;

	public SaveResultsListener(ElectionGridFrame gui) {
		source = gui;

		fc = new JFileChooser();
		fc.setFileFilter(new XMLFilter());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int returnVal = fc.showSaveDialog(source);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			source.saveResultToXML(file);
		}
	}
}