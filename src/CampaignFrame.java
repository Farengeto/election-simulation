import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.table.DefaultTableModel;


public class CampaignFrame extends ElectionGridFrame{

	public CampaignFrame(ElectionData data, VotingType votingType) {
		super(data, votingType, "Campaign Simulator", "Campaign Length:", 30, 0.02);
	}

	protected void createDTM() {
		int pSize = electionData.getParties().size();
		Object[] tableColumns = new Object[pSize+1];
		tableColumns[0] = (Object)"Poll Number";
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
		int iterations = 0;
		double shiftDev = 0;
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
			PrintWriter writer = new PrintWriter(f, "UTF-8");
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
			s += "\t<Poll number=\"" + (d+1) + "\">\n";
			for(int p = 0; p < electionData.getParties().size(); p++){
				s += "\t\t<Party name=\"" + electionData.getParty(p).getName() + "\">\n";
				String value = (String)dtm.getValueAt(d, p+1);
				int splitIndex = value.lastIndexOf("-");
				s += "\t\t\t<Seats>" + value.substring(0, splitIndex-1) + "</Seats>\n";
				s += "\t\t\t<Percentage>" + value.substring(splitIndex+2) + "</Percentage>\n";
				s += "\t\t</Party>\n";
			}
			s += "\t</Poll>\n";
		}
		s += "</CampaignData>\n";
		return s;
	}

}
