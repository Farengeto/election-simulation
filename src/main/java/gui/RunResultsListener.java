package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class RunResultsListener implements ActionListener {
	private ElectionGridFrame source;

	public RunResultsListener(ElectionGridFrame gui) {
		source = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		source.runSystem();
	}
}
