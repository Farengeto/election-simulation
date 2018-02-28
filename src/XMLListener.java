import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

public class XMLListener implements ActionListener{
	private ElectionsUI GUI;
	private JComponent in;
	private JComponent out;
	private JFileChooser fc;
	
	public XMLListener(ElectionsUI g, JComponent in, JComponent out){
		GUI = g;
		fc = new JFileChooser();
		//fc.addChoosableFileFilter(new XMLFilter());
		fc.setFileFilter(new XMLFilter());
		this.in = in;
		this.out = out;
	}
	
	/**
	 * Imports the Network topology from a XML file
	 * @param ActionEvent e
	 * @return void
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == in){
			int returnVal = fc.showOpenDialog(GUI.getFrame());

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            GUI.loadFromXML(file);
	        }
		}
		else if(e.getSource() == out){
			int returnVal = fc.showSaveDialog(GUI.getFrame());

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            GUI.saveToXML(file);
	        }
		}
		else{
			System.err.println("Source not recognized.");
		}
	}
}