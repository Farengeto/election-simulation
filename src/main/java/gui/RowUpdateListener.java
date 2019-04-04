package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class RowUpdateListener implements ActionListener {
	private InputPanel source;

	public RowUpdateListener(InputPanel source) {
		this.source = source;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		source.changeTableRows();
	}

}
