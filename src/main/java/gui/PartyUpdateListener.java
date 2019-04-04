package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class PartyUpdateListener implements ActionListener {
	private InputParty source;

	public PartyUpdateListener(InputParty source) {
		this.source = source;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		source.changeTableRows();
	}

}
