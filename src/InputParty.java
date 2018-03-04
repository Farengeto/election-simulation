import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

public class InputParty extends InputPanel {
	private static final Object[] tableColumns = {"Party name","Approval (%)","Colour, Red (0-255)","Colour, Green (0-255)","Colour, Blue (0-255)"};
	
	public InputParty(ElectionData data) {
		super(data, "Number of Parties:");
	}
	
	protected void createDTM(){
		int size = electionData.getParties().size();
		Object[][] tableData = makeTableData(size);
		dtm = new DefaultTableModel(tableData,tableColumns){
			//lock column data types
			@Override
			public Class getColumnClass(int column) {
				switch (column) {
					case 0:
						return String.class;
					case 1:
						return Double.class;
					case 2:
						return Integer.class;
					case 3:
						return Integer.class;
					case 4:
						return Integer.class;
				}
				return String.class;
            }
        };
	}
	
	protected Object[][] makeTableData(int rows){
		Object[][] tableData = new Object[rows][5];
		for(int i = 0; i < rows; i++){
			if(i >= electionData.getParties().size()){
				tableData[i][1] = 0.0;
				tableData[i][2] = 128;
				tableData[i][3] = 128;
				tableData[i][4] = 128;
			}
			else{
				Party p = electionData.getParty(i);
				if(p == null){
					tableData[i][1] = 0.0;
					tableData[i][2] = 128;
					tableData[i][3] = 128;
					tableData[i][4] = 128;
				}
				else{
					tableData[i][0] = p.getName();
					tableData[i][1] = p.getApproval()*100;
					tableData[i][2] = p.getColor().getRed();
					tableData[i][3] = p.getColor().getGreen();
					tableData[i][4] = p.getColor().getBlue();
				}
			}
		}
		return tableData;
	}
	
	public void changeTableRows(){
		int rows = Integer.parseInt(sizeCounter.getText());
		if(dtm != null){
			while(rows < dtm.getRowCount()){
				dtm.removeRow(dtm.getRowCount()-1);
			}
			while(rows > dtm.getRowCount()){
				Object[] newRow = {"",0.0,128,128,128};
				dtm.addRow(newRow);
			}
			dtm.fireTableDataChanged();
		}
	}

	@Override
	public void updateElectionData() {
		List<Party> newParties = new ArrayList<>();
		for(int i = 0; i < dtm.getRowCount(); i++){
			String name = (String)table.getValueAt(i,0);
			double approval =  (Double)(table.getValueAt(i,1))/100.0;
			Color colour = new Color((Integer)table.getValueAt(i,2),(Integer)table.getValueAt(i,3),(Integer)table.getValueAt(i,4));
			newParties.add(new Party(name, colour, approval));
		}
		electionData.setParties(newParties);
	}
}
