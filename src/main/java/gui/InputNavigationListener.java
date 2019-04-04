package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InputNavigationListener implements ActionListener {
	private ElectionsUI source;
	private InputForms formType;

	public InputNavigationListener(ElectionsUI source, InputForms type) {
		this.source = source;
		formType = type;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		source.navigateInput(formType);
	}

}
