package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class RunElectionListener implements ActionListener {
	private ElectionsUI source;
	private ElectionModes runType;

	public RunElectionListener(ElectionsUI gui, ElectionModes type) {
		source = gui;
		runType = type;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		source.runElection(runType);
	}
}
