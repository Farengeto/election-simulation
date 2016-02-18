import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.table.*;
import javax.swing.JLabel;

public class PartyInput implements ActionListener{
	private JFrame frame;
	private JTable table;
	private DefaultTableModel dtm;
	private JTextField sizeCounter;
	private JButton countButton;
	private InputForm input;
	
	//default from blank data set, default 3 parties
	public PartyInput(InputForm input){
		this(input,3);
	}
	//default from blank data set, party count defined in size
	public PartyInput(InputForm input, int size){
		this(input,new Object[size][5]);
	}
	//default from blank data set, parties defined by given party list
	public PartyInput(InputForm input, List<Party> parties){
		this(input,makeData(parties));
	}
	//generate from given data set, party count defined in size
	public PartyInput(InputForm input, Object[][] data){
		this.input = input;
		Object[] columns = {"Party name","Approval (%)","Colour, Red (0-255)","Colour, Green (0-255)","Colour, Blue (0-255)"};
		//initialize any empty values
		for(int i = 0; i < data.length; i++){
			if(data[i][1] == null){
				data[i][1] = 0.0;
			}
			if(data[i][2] == null){
				data[i][2] = 128;
			}
			if(data[i][3] == null){
				data[i][3] = 128;
			}
			if(data[i][4] == null){
				data[i][4] = 128;
			}
		}
		//create the table
		dtm = new DefaultTableModel(data,columns){
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
		table = new JTable(dtm);
		JScrollPane pane = new JScrollPane(table);
		JPanel partySelect = new JPanel();
		partySelect.setLayout(new GridLayout(1,3));
		Integer size = (Integer)(data.length);
		partySelect.add(new JLabel("Number of Parties:"));
		sizeCounter = new JTextField(size.toString());
		partySelect.add(sizeCounter);
		countButton = new JButton("Update");
		countButton.addActionListener(this);
		partySelect.add(countButton);
		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(new PartyListener(input));
		//create the JFrame
		frame = new JFrame("Party Input Menu");
		frame.setLayout(new BorderLayout());
		frame.add(pane,BorderLayout.CENTER);
		frame.add(nextButton,BorderLayout.SOUTH);
		frame.add(partySelect,BorderLayout.NORTH);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	//create the table data set from party data
	private static Object[][] makeData(List<Party> parties){
		Object[][] data = new Object[parties.size()][5];
		int count = 0;
		for(Party p : parties){
			data[count][0] = p.getName();
			data[count][1] = p.getApproval()*100;
			Color c = p.getColor();
			data[count][2] = c.getRed();
			data[count][3] = c.getGreen();
			data[count][4] = c.getBlue();
			count++;
		}
		return data;
	}
	
	//update table to new party count on button press
	public void actionPerformed(ActionEvent e) {
		System.out.println(sizeCounter.getText());
		int size = Integer.parseInt(sizeCounter.getText());
		while(size < dtm.getRowCount()){
			dtm.removeRow(dtm.getRowCount()-1);
		}
		while(size > dtm.getRowCount()){
			Object[] newRow = {"",0.0,128,128,128};
			dtm.addRow(newRow);
		}
		dtm.fireTableDataChanged();
	}
	
	//"Next" button for window
	public class PartyListener implements ActionListener{
		InputForm input;
		
		public PartyListener(InputForm input){
			this.input = input;
		}
		
		//Save table data to inputForm, when button pressed
		public void actionPerformed(ActionEvent e) {
			List<Party> newParties = new ArrayList<>();
			for(int i = 0; i < dtm.getRowCount(); i++){
				Color c = new Color((Integer)table.getValueAt(i,2),(Integer)table.getValueAt(i,3),(Integer)table.getValueAt(i,4));
				double a =  (Double)(table.getValueAt(i,1))/100.0;
				newParties.add(new Party((String)table.getValueAt(i,0),c,a));
			}
			for(Party p : newParties){
				System.out.println(p.getResults());
			}
			input.setParties(newParties);
			frame.setVisible(false);
			new RegionInput(input,input.getRegions());
		}
	}
}