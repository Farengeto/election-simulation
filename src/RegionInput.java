import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class RegionInput implements ActionListener{
	private JFrame frame;
	private JTable table;
	private DefaultTableModel dtm;
	private JTextField sizeCounter;
	private JButton countButton;
	private InputForm input;
	
	//default from blank data set, default 3 parties
	public RegionInput(InputForm input){
		this(input,5);
	}
	public RegionInput(InputForm input, int size){
		this(input,new Object[size][input.getParties().size()+4]);
	}
	public RegionInput(InputForm input, List<Region> regions){
		this(input,makeData(input,regions));
	}
	public RegionInput(InputForm input, Object[][] data){
		this.input = input;
		Object[] columns = new String[input.getParties().size()+4];
		columns[0] = "Province";
		columns[1] = "Region";
		columns[2] = "Population";
		columns[3] = "Seats";
		int count = 4;
		for(Party p : input.getParties()){
			columns[count] = p.getName(); 
			for(int r = 0; r < data.length; r++){
				//initialize any empty values
				if(data[r][count] == null){
					data[r][count] = 1.0;
				}
			}
			count++;
		}
		for(int r = 0; r < data.length; r++){
			//initialize any empty values
			if(data[r][2] == null){
				data[r][2] = 0;
			}
			if(data[r][3] == null){
				data[r][3] = 0;
			}
		}
		
		dtm = new DefaultTableModel(data,columns){
			@Override
			public Class getColumnClass(int column) {
				switch (column) {
					case 0:
						return String.class;
					case 1:
						return String.class;
					case 2:
						return Long.class;
					case 3:
						return Integer.class;
					default:
						return Double.class;
				}
            }
        };
		table = new JTable(dtm);
		JScrollPane pane = new JScrollPane(table);
		JPanel partySelect = new JPanel();
		partySelect.setLayout(new GridLayout(1,3));
		Integer size = (Integer)(data.length);
		partySelect.add(new JLabel("Number of Regions:"));
		sizeCounter = new JTextField(size.toString());
		partySelect.add(sizeCounter);
		countButton = new JButton("Update");
		countButton.addActionListener(this);
		partySelect.add(countButton);
		//JButton nextButton = new JButton("Next");
		//nextButton.addActionListener(new RegionListener(input));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(4,1));
		JButton nextButton = new JButton("Save");
		nextButton.addActionListener(new RegionListener(input));
		buttonPanel.add(nextButton);
		JButton electionButton = new JButton("Save and run Election");
		electionButton.addActionListener(new ElectionListener(input));
		buttonPanel.add(electionButton);
		JButton campaignButton = new JButton("Save and run Campaign");
		campaignButton.addActionListener(new CampaignListener(input));
		buttonPanel.add(campaignButton);
		JButton rangeButton = new JButton("Save and run Range");
		rangeButton.addActionListener(new RegionListener(input));
		buttonPanel.add(rangeButton);
		
		
		frame = new JFrame("Region Input Menu");
		frame.setLayout(new BorderLayout());
		frame.add(pane,BorderLayout.CENTER);
		frame.add(buttonPanel,BorderLayout.SOUTH);
		frame.add(partySelect,BorderLayout.NORTH);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private static Object[][] makeData(InputForm input, List<Region> regions){
		Object[][] data = new Object[regions.size()][input.getParties().size()+4];
		int count = 0;
		for(Region r : regions){
			data[count][0] = r.getProvince().getName();
			data[count][1] = r.getName();
			data[count][2] = r.getPopulation();
			data[count][3] = r.getSeats();
			int pCount = 4;
			for(Party p : input.getParties()){
				Party op = input.getConversion(p);
				if(op != null){
					data[count][pCount] = r.getSupport(op)*100;
				}
				else{
					data[count][pCount] = 0.0;
				}
				pCount++;
			}
			count++;
		}
		return data;
	}
	
	public void actionPerformed(ActionEvent e) {
		System.out.println(sizeCounter.getText());
		//updateTable();
		int size = Integer.parseInt(sizeCounter.getText());
		while(size < dtm.getRowCount()){
			dtm.removeRow(dtm.getRowCount()-1);
		}
		while(size > dtm.getRowCount()){
			Object[] newRow = new Object[input.getParties().size()+4];
			newRow[2] = 0;
			newRow[3] = 0;
			for(int p = 0; p < input.getParties().size(); p++){
				newRow[p+4] = 1.0;
			}
			dtm.addRow(newRow);
		}
		dtm.fireTableDataChanged();
	}
	
	//initialize list of Provinces
	public List<Province> createProvinces(){
		//initialize list of Provinces
		Set<String> provSet = new HashSet<>();
		for(int i = 0; i < dtm.getRowCount(); i++){
			provSet.add((String)table.getValueAt(i,0));
		}
		List<Province> newProvinces = new ArrayList<>();
		for(String s : provSet){
			newProvinces.add(new Province(s));
		}
		return newProvinces;
	}
	
	//initialize list of Regions
	public List<Region> createRegions(List<Province> newProvinces){
		List<Region> newRegions = new ArrayList<>();
		for(int i = 0; i < dtm.getRowCount(); i++){
			String pName = (String)table.getValueAt(i,0);
			String name = (String)table.getValueAt(i,1);
			long population = (Long)table.getValueAt(i,2);
			int seats = (Integer)table.getValueAt(i,3);
			int j = 4;
			Map<Party,Double> support = new HashMap<>();
			for(Party p : input.getParties()){
				support.put(p, (Double)table.getValueAt(i,j));
				j++;
			}
			//find region's province and add it to the region
			for(Province p : newProvinces){
				if(p.getName().equals(pName)){
					newRegions.add(new Region(name,population,seats,p,support));
				}
			}
		}
		return newRegions;
	}
	
	public void saveData(){
		List<Province> newProvinces = createProvinces();
		List<Region> newRegions = createRegions(newProvinces);
		input.setProvinces(newProvinces);
		input.setRegions(newRegions);
		input.writeToFile();
	}
	
	public class RegionListener implements ActionListener{
		InputForm input;
		
		public RegionListener(InputForm input){
			this.input = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			saveData();
		}
	}
	
	public class ElectionListener implements ActionListener{
		InputForm input;
		
		public ElectionListener(InputForm input){
			this.input = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			saveData();
			//perform election
			UpdatedVoting election = new UpdatedVoting(input.getOutFile());
			election.update(0.10);
			election.results();
			election.setVisible(true);
			election.repaint();
		}
	}
	public class CampaignListener implements ActionListener{
		InputForm input;
		
		public CampaignListener(InputForm input){
			this.input = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			saveData();
			Campaign sample = new Campaign(50,input.getOutFile());
			sample.calculate();
			sample.polls.setVisible(true);
			sample.resultsOut();
		}
	}
	public class RangeListener implements ActionListener{
		InputForm input;
		
		public RangeListener(InputForm input){
			this.input = input;
		}
		
		public void actionPerformed(ActionEvent e) {
			saveData();
			ResultsRange sample = new ResultsRange(1000);
			sample.calculate();
			sample.resultsOut();
		}
	}
}
