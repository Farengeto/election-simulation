package gui;

import election.ElectionData;
import election.VotingData;

import java.awt.Dimension;
import javax.swing.JPanel;

//superclass for result panels
public abstract class ResultsPanel extends JPanel {
	protected ElectionData info;
	protected VotingData results;

	public ResultsPanel(ElectionData election, VotingData voting, Dimension dim) {
		info = election;
		results = voting;
		this.setSize(dim);
		this.setPreferredSize(dim);
		Dimension min = new Dimension((int) this.getSize().getWidth(), 1);
		this.setMinimumSize(min);
	}
}