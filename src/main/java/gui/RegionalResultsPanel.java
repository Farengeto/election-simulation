package gui;

import election.ElectionData;
import election.VotingData;
import election.NationalComparator;
import nation.Party;
import nation.Region;

import java.awt.Graphics;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;


//Table class for the regional results of the election
public class RegionalResultsPanel extends JTable {
	protected ElectionData info;
	protected VotingData results;
	private boolean edit;

	//creates the class for an electoral data set 
	public RegionalResultsPanel(ElectionData election, VotingData voting) {
		super(makeData(election, voting), makeColumns(election, voting));
		info = election;
		results = voting;
		edit = false;
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
		setFillsViewportHeight(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

	//prevents cells from being edited unless editing is enabled by updateTable
	public boolean isCellEditable(int row, int column) {
		return edit;
	}

	//generate the column headers from the data set
	public static Object[] makeColumns(ElectionData election, VotingData voting) {
		List<Party> parties = election.getParties();
		//resort parties 
		parties.sort(new NationalComparator(voting));
		Object[] columns = new String[parties.size() + 4];
		columns[0] = "Province";
		columns[1] = "Region";
		columns[2] = "Population";
		columns[3] = "Seats";
		for (int p = 0; p < parties.size(); p++) {
			columns[p + 4] = parties.get(p).getName();
		}
		return columns;
	}

	//generate the table data from the data set
	public static Object[][] makeData(ElectionData election, VotingData voting) {
		//get current set of results
		List<Party> parties = election.getParties();
		List<Region> regions = election.getRegions();
		//resort parties 
		parties.sort(new NationalComparator(voting));
		//get each region's province, name, demographics and votes
		Object[][] data = new Object[regions.size()][parties.size() + 4];
		int count = 0;
		for (Region r : regions) {
			data[count][0] = r.getProvince().getName();
			data[count][1] = r.getName();
			data[count][2] = r.getPopulation();
			data[count][3] = r.getSeats();
			int pCount = 0;
			for (Party p : parties) {
				data[count][pCount + 4] = voting.getSeatsRegion(r, p) + " - "
						+ (Math.round(10000.0 * (double) voting.getVotesRegion(r, p) / (double) r.getPopulation()) / 100.0) + "%";
				pCount++;
			}
			count++;
		}
		return data;
	}

	//update the results table with a new set of data
	//resorts columns so top national parties are shown first
	public void updateTable() {
		//get current set of results
		List<Party> parties = info.getParties();
		List<Region> regions = info.getRegions();
		edit = true; //enable editing of table
		//sort parties 
		parties.sort(new NationalComparator(results));
		//update column headers for sorting
		for (int p = 0; p < parties.size(); p++) {
			getColumnModel().getColumn(p + 4).setHeaderValue(parties.get(p).getName());
		}
		//replace all data values with the newest set
		int count = 0;
		for (Region r : regions) {
			int pCount = 0;
			for (Party p : parties) {
				//check that data results are not null
				setValueAt(results.getSeatsRegion(r, p) + " - "
								+ (Math.round(10000.0 * (double) results.getVotesRegion(r, p) / (double) r.getPopulation()) / 100.0) + "%",
						count, pCount + 4);
				pCount++;
			}
			count++;
		}
		edit = false; //disable editing of table
	}

	public void paint(Graphics g) {
		super.paint(g);
	}
}