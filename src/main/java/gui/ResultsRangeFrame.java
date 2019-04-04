package gui;

import election.ElectionData;
import election.VotingData;
import nation.Party;
import votingLibrary.VotingType;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;


public class ResultsRangeFrame extends ElectionGridFrame {

	public ResultsRangeFrame(ElectionData data, VotingType votingType) {
		super(data, votingType, "Results Range Calculator", "Simulation Count:", 1000, 0.10);
	}

	protected void createDTM() {
		int pSize = electionData.getParties().size();
		Object[] tableColumns = new Object[]{"Party", "95% Lower", "68% Lower", "Median", "68% Upper", "95% Upper"};
		Object[][] tableData = new Object[pSize][6];
		for (int p = 0; p < pSize; p++) {
			tableData[p][0] = electionData.getParty(p).getName();
		}
		dtm = new DefaultTableModel(tableData, tableColumns) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
	}

	public void runSystem() {
		int iterations = 0;
		double shiftDev = 0;
		try {
			iterations = Math.max(0, Integer.parseInt(iterationCount.getText()));
			shiftDev = Math.abs(Double.parseDouble(stdDev.getText()) / 200.0);
			if (iterations <= 0) {
				System.err.println("Invalid iteration count: " + iterations);
				return;
			}
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			return;
		}
		while (dtm.getRowCount() > 0) {
			dtm.removeRow(0);
		}
		//int[][] seats = new int[iterations][partyCount];
		//long[][] votes = new long[iterations][partyCount];
		Map<String, List<Integer>> seats = new HashMap<>();
		Map<String, List<Long>> votes = new HashMap<>();
		for (Party p : electionData.getParties()) {
			seats.put(p.getName(), new ArrayList<Integer>());
			votes.put(p.getName(), new ArrayList<Long>());
		}

		VotingData votingData = new VotingData(electionData);
		for (int i = 0; i < iterations; i++) {
			votingData.modifySupport(shiftDev);
			votingData.calculateResults(votingType, 0.00);

			for (Party p : electionData.getParties()) {
				String pName = p.getName();
				seats.get(pName).add(votingData.getSeatsNation(p));
				votes.get(pName).add(votingData.getVotesNation(p));
			}

			votingData.resetAll();
			;
		}

		for (Party p : electionData.getParties()) {
			String pName = p.getName();
			List<Long> pVotes = votes.get(pName);
			Collections.sort(pVotes);

			Object[] newRow = new Object[6];
			newRow[0] = "(Votes) " + pName;
			newRow[1] = (pVotes.get((int) Math.ceil((double) iterations * 0.02275))
					+ pVotes.get((int) Math.floor((double) iterations * .02275))) / 2;
			newRow[2] = (pVotes.get((int) Math.ceil((double) iterations * 0.15866))
					+ pVotes.get((int) Math.floor((double) iterations * 0.15866))) / 2;
			newRow[3] = (pVotes.get((int) Math.ceil((double) iterations * 0.5))
					+ pVotes.get((int) Math.floor((double) iterations * 0.5))) / 2;
			newRow[4] = (pVotes.get((int) Math.ceil((double) iterations * 0.84134))
					+ pVotes.get((int) Math.floor((double) iterations * 0.84134))) / 2;
			newRow[5] = (pVotes.get((int) Math.ceil((double) iterations * 0.97725))
					+ pVotes.get((int) Math.floor((double) iterations * 0.97725))) / 2;

			dtm.addRow(newRow);
		}

		for (Party p : electionData.getParties()) {
			String pName = p.getName();
			List<Integer> pSeats = seats.get(pName);
			Collections.sort(pSeats);

			Object[] newRow = new Object[6];
			newRow[0] = "(Seats) " + pName;
			newRow[1] = (pSeats.get((int) Math.ceil((double) iterations * 0.02275))
					+ pSeats.get((int) Math.floor((double) iterations * .02275))) / 2;
			newRow[2] = (pSeats.get((int) Math.ceil((double) iterations * 0.15866))
					+ pSeats.get((int) Math.floor((double) iterations * 0.15866))) / 2;
			newRow[3] = (pSeats.get((int) Math.ceil((double) iterations * 0.5))
					+ pSeats.get((int) Math.floor((double) iterations * 0.5))) / 2;
			newRow[4] = (pSeats.get((int) Math.ceil((double) iterations * 0.84134))
					+ pSeats.get((int) Math.floor((double) iterations * 0.84134))) / 2;
			newRow[5] = (pSeats.get((int) Math.ceil((double) iterations * 0.97725))
					+ pSeats.get((int) Math.floor((double) iterations * 0.97725))) / 2;

			dtm.addRow(newRow);
		}

		dtm.fireTableDataChanged();
	}

	public void saveResultToXML(File f) {
		try {
			PrintWriter writer = new PrintWriter(f, "UTF-8");
			String s = toXML();
			writer.write(s);
			writer.close();
		} catch (IOException e) {
			System.err.println("IO Error, export failed");
		}
	}

	public String toXML() {
		int partyCount = electionData.getParties().size();
		String s = "<RangeData>\n";
		for (int p = 0; p < partyCount; p++) {
			s += "\t<Party name=\"" + electionData.getParty(p).getName() + "\">\n";
			s += "\t\t<Votes>\n";
			s += "\t\t\t<Min>" + dtm.getValueAt(p, 1) + "</Min>\n";
			s += "\t\t\t<Low>" + dtm.getValueAt(p, 2) + "</Low>\n";
			s += "\t\t\t<Average>" + dtm.getValueAt(p, 3) + "</Average>\n";
			s += "\t\t\t<High>" + dtm.getValueAt(p, 4) + "</High>\n";
			s += "\t\t\t<Max>" + dtm.getValueAt(p, 5) + "</Max>\n";
			s += "\t\t</Votes>\n";
			s += "\t\t<Seats>\n";
			s += "\t\t\t<Min>" + dtm.getValueAt(p + partyCount, 1) + "</Min>\n";
			s += "\t\t\t<Low>" + dtm.getValueAt(p + partyCount, 2) + "</Low>\n";
			s += "\t\t\t<Average>" + dtm.getValueAt(p + partyCount, 3) + "</Average>\n";
			s += "\t\t\t<High>" + dtm.getValueAt(p + partyCount, 4) + "</High>\n";
			s += "\t\t\t<Max>" + dtm.getValueAt(p + partyCount, 5) + "</Max>\n";
			s += "\t\t</Seats>\n";
			s += "\t</Party>\n";
		}
		s += "</RangeData>\n";
		return s;
	}
}
