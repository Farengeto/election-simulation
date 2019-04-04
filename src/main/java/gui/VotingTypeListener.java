package gui;

import votingLibrary.VotingType;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VotingTypeListener implements ActionListener {
    private ElectionsUI GUI;
    private VotingType voting;

    public VotingTypeListener(ElectionsUI ui, VotingType v) {
        GUI = ui;
        voting = v;
    }

    /**
     * Changes the simulation's strategy to the selected one
     */
    public void actionPerformed(ActionEvent e) {
        GUI.setVotingType(voting);
    }
}
