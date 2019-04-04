package gui;

import election.ElectionData;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public abstract class InputPanel extends JPanel {
	protected ElectionData electionData;
	protected JTable table;
	protected DefaultTableModel dtm;
	protected JTextField sizeCounter;
	protected JButton countButton;

	public InputPanel(ElectionData data) {
		this(data, "Number of Rows:");
	}

	public InputPanel(ElectionData data, String labelText) {
		electionData = data;

		setLayout(new BorderLayout());

		createDTM();
		table = new JTable(dtm);
		JScrollPane pane = new JScrollPane(table);
		add(pane, BorderLayout.CENTER);

		JPanel regionSelect = new JPanel();
		regionSelect.setLayout(new GridLayout(1, 3));
		regionSelect.add(new JLabel(labelText));
		sizeCounter = new JTextField(Integer.toString(dtm.getRowCount()));
		regionSelect.add(sizeCounter);
		countButton = new JButton("Update");
		countButton.addActionListener(new RowUpdateListener(this));
		regionSelect.add(countButton);
		add(regionSelect, BorderLayout.NORTH);

	}

	public ElectionData getElectionData() {
		return electionData;
	}

	public void setElectionData(ElectionData electionData) {
		this.electionData = electionData;
	}

	protected abstract void createDTM();

	protected abstract Object[][] makeTableData(int rows);

	public abstract void changeTableRows();

	public abstract void updateElectionData();
}
