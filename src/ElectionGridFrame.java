import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;


public abstract class ElectionGridFrame extends JFrame {
	protected ElectionData electionData;
	protected VotingType votingType;
	protected JTable table;
	protected DefaultTableModel dtm;
	protected JTextField stdDev;
	protected JTextField iterationCount;
	protected JTextField seatThreshold;
	protected JButton runVoting;
	protected JButton saveVoting;
	
	public ElectionGridFrame(ElectionData data, VotingType type, String frameHeader, String labelText, int iterations, double defaultShift){
		super(frameHeader);
		
		electionData = data;
		votingType = type;
		
		setLayout(new BorderLayout());
		
		createDTM();
		table = new JTable(dtm);
		JScrollPane pane = new JScrollPane(table);
		add(pane,BorderLayout.CENTER);
		
		JPanel regionSelect = new JPanel();
		regionSelect.setLayout(new GridLayout(3,2));
		regionSelect.add(new JLabel("Margin of Error (+/- %):"));
		stdDev = new JTextField(Double.toString(defaultShift*100));
		regionSelect.add(stdDev);
		regionSelect.add(new JLabel("Seat Threshold (%):"));
		seatThreshold = new JTextField(Double.toString(5));
		regionSelect.add(seatThreshold);
		regionSelect.add(new JLabel(labelText));
		iterationCount = new JTextField(Integer.toString(iterations));
		regionSelect.add(iterationCount);
		add(regionSelect,BorderLayout.NORTH);
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(2,1));
		runVoting = new JButton("Run");
		runVoting.addActionListener(new RunResultsListener(this));
		saveVoting = new JButton("Save results");
		saveVoting.addActionListener(new SaveResultsListener(this));
		buttons.add(runVoting);
		buttons.add(saveVoting);
		add(buttons,BorderLayout.SOUTH);
		
		runSystem();
		this.pack();
		this.setVisible(true);
	}

	public ElectionData getElectionData() {
		return electionData;
	}

	public void setElectionData(ElectionData electionData) {
		this.electionData = electionData;
	}
	
	protected abstract void createDTM();
	
	public abstract void runSystem();
	
	public abstract void saveResultToXML(File f);
}
