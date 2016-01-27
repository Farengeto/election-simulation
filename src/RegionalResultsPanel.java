import java.awt.Graphics;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;


//public class RegionalResultsPanel extends ResultsPanel{
public class RegionalResultsPanel extends JTable{	
	private UpdatedVoting results;
	private boolean edit;
	
	public RegionalResultsPanel(UpdatedVoting voting){
		this(voting,makeData(voting),makeColumns(voting));
		edit = false;
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
		setFillsViewportHeight(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
	
	public RegionalResultsPanel(UpdatedVoting voting,Object[][] data,Object[] columns){
		super(data,columns);
		results = voting;
		edit = false;
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
		setFillsViewportHeight(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
	
	//prevents cells from being edited unless editing is enabled by updateTable
	public boolean isCellEditable(int row,int column){
		return edit; 
	}
	
	public static Object[] makeColumns(UpdatedVoting result){
		List<Party> parties = result.getParties();
		//resort parties 
			parties.sort(new NationalComparator());
		int count = 0;
		Object[] columns = new String[parties.size()+3];
		columns[0] = "Province";
		columns[1] = "Region";
		columns[2] = "Population";
		for(Party p : parties){
			columns[count+3] = p.getName(); 
			count++;
		}
		return columns;
	}
	
	public static Object[][] makeData(UpdatedVoting result){
		//get current set of results
		List<Party> parties = result.getParties();
		List<Region> regions = result.getRegions();
		//resort parties 
		//parties.sort(new NationalComparator());
		//get each region's province, name, demographics and votes
		Object[][] data = new Object[regions.size()][parties.size()+3];
		int count = 0;
		for(Region r : regions){
			data[count][0] = r.getProvince().getName();
			data[count][1] = r.getName();
			data[count][2] = Long.toString(r.getPopulation());
			int pCount = 0;
			for(Party p : parties){
				if(r.getResults().get(p) != null && r.getVotes().get(p) != null){
					data[count][pCount+3] = r.getResults().get(p) + " - " + Math.round(1000.0 * (double)r.getVotes().get(p) / (double)r.getPopulation())/10.0 + "%";
				}
				else{
					data[count][pCount+3] = "0 - N/A";
				}
				pCount++;
			}
			count++;
		}
		return data;
	}
	
	public void updateTable(){
		//get current set of results
		List<Party> parties = results.getParties();
		List<Region> regions = results.getRegions();
		edit = true; //enable editing
		//resort parties 
		//parties.sort(new NationalComparator());
		int count = 0;
		/*for(Party p : parties){
			//System.out.println(p.getName() + " Test");
			getColumnModel().getColumn(count+3).setHeaderValue(p.getName() + " Test");
			count++;
		}
		//replace all data values with the newest set
		count = 0;*/
		for(Region r : regions){
			int pCount = 0;
			for(Party p : parties){
				if(r.getResults().get(p) != null && r.getVotes().get(p) != null){
					setValueAt(r.getResults().get(p) + " - " + Math.round(1000.0 * (double)r.getVotes().get(p) / (double)r.getPopulation())/10.0 + "%", count, pCount+3);
				}
				else{
					setValueAt("0 - N/A", count, pCount+3);
				}
				pCount++;
			}
			count++;
		}
		edit = false; //disable editing
	}
	
	//refreshes the table whenever the panel is repainted
	public void paint(Graphics g){
		super.paint(g);
		//updateTable();
	}
}