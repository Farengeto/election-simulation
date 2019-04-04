package gui;

import election.ElectionData;
import election.VotingData;
import votingLibrary.VotingType;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.swing.table.DefaultTableModel;


public class CampaignFrame extends ElectionGridFrame{

	public CampaignFrame(ElectionData data, VotingType votingType) {
		super(data, votingType, "Campaign Simulator", "Campaign Length:", 30, 0.02);
	}

	protected void createDTM() {
		int pSize = electionData.getParties().size();
		Object[] tableColumns = new Object[pSize+1];
		tableColumns[0] = "Poll Number";
		for(int p = 0; p < pSize; p++){
			tableColumns[p+1] = electionData.getParties().get(p).getName();
		}
		Object[][] tableData = new Object[0][pSize+1];
		dtm = new DefaultTableModel(tableData,tableColumns){
			public boolean isCellEditable(int row, int column){  
		          return false;  
		      }
		};
	}

	public void runSystem() {
		int iterations;
		double shiftDev;
		int partyCount = electionData.getParties().size();
		try{
			iterations = Math.max(0, Integer.parseInt(iterationCount.getText()));
			shiftDev = Math.abs(Double.parseDouble(stdDev.getText()) / 200.0);
			if(iterations <= 0){
				System.err.println("Invalid iteration count: " + iterations);
				return;
			}
		} catch(Exception ex) {
			System.err.println(ex.getMessage());
			return;
		}
		while(dtm.getRowCount() > 0){
			dtm.removeRow(0);
		}
		VotingData votingData = new VotingData(electionData);
		for(int i = 0; i < iterations; i++){
			votingData.modifySupport(shiftDev);
			votingData.calculateResults(votingType, 0.00);
			
			Object[] newRow = new Object[partyCount+1];
			newRow[0] = i+1;
			for(int p = 0; p < partyCount; p++){
				newRow[p+1]= votingData.getSeatsNation(electionData.getParty(p)) + " - "
						+ (Math.round(10000.0 * (double)votingData.getVotesNation(electionData.getParty(p)) / (double)electionData.getPopulation()) / 100.0) + "%";
			}
			dtm.addRow(newRow);
			
			votingData.resetElection();
		}
		dtm.fireTableDataChanged();
	}

	public void saveResultToXML(File f){
		try{
			PrintWriter writer = new PrintWriter(f, StandardCharsets.UTF_8);
			String s = toXML();
			writer.write(s);
			writer.close();
		}catch(IOException e){
			System.err.println("IO Error, export failed");
		}
	}
	
	public String toXML(){
		String s = "<CampaignData>\n";
		for(int d = 0; d < dtm.getRowCount(); d++){
			s = s.concat("\t<Poll number=\"" + (d+1) + "\">\n");
			for(int p = 0; p < electionData.getParties().size(); p++){
				s = s.concat("\t\t<Party name=\"" + electionData.getParty(p).getName() + "\">\n");
				String value = (String)dtm.getValueAt(d, p+1);
				int splitIndex = value.lastIndexOf("-");
				s = s.concat("\t\t\t<Seats>" + value.substring(0, splitIndex-1) + "</Seats>\n");
				s = s.concat("\t\t\t<Percentage>" + value.substring(splitIndex+2) + "</Percentage>\n");
				s = s.concat("\t\t</Party>\n");
			}
			s = s.concat("\t</Poll>\n");
		}
		s = s.concat("</CampaignData>\n");
		return s;
	}

}
