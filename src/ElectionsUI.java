import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ElectionsUI {
	private ElectionData electionData;
	private JFrame GUI;
	private InputPanel currentPanel;
	private JButton partiesButton;
	private JButton regionsButton;
	private JTextField fileField;
	
	private VotingType votingType;
	private Map<VotingType,JMenuItem> votingTypes;
	
	public ElectionsUI(ElectionData data){
		electionData = data;
		votingType = VotingType.PR_DROOP;
		votingTypes = new HashMap<>();
		
		GUI = new JFrame("Election Simulator");
		GUI.setLayout(new BorderLayout());
		
		JMenuBar menu      = new JMenuBar();
		JMenu fileMenu     = new JMenu("File");
		JMenuItem load     = new JMenuItem("Open (XML)");
		JMenuItem loadText = new JMenuItem("Open (Text)");
		JMenuItem save     = new JMenuItem("Save");
		//JMenuItem saveText = new JMenuItem("Save (Text)");
		XMLListener xmlListener = new XMLListener(this, load, save);
		load.addActionListener(xmlListener);
		save.addActionListener(xmlListener);
		TextListener textListener = new TextListener(this, loadText, null);
		loadText.addActionListener(textListener);
		//saveText.addActionListener(textListener);
		fileMenu.add(load);
		fileMenu.add(loadText);
		fileMenu.add(save);
		//fileMenu.add(saveText);
		menu.add(fileMenu);
		
		JMenu votingMenu = new JMenu("Voting");	
		votingMenu.add(createVotingTypeMenuItem(VotingType.FPTP, "First-Past-the-Post (Regional)"));
		votingMenu.add(createVotingTypeMenuItem(VotingType.FPTP_PROVINCE, "First-Past-the-Post (Provincial)"));
		votingMenu.add(createVotingTypeMenuItem(VotingType.FPTP_NATIONAL, "First-Past-the-Post (National)"));
		votingMenu.addSeparator();
		votingMenu.add(createVotingTypeMenuItem(VotingType.PR_HARE, "Proportional Representation (Regional, Hare quota)"));
		votingMenu.add(createVotingTypeMenuItem(VotingType.PR_DROOP, "Proportional Representation (Regional, Droop quota)"));
		//votingMenu.add(createVotingTypeMenuItem(VotingType.PR_HARE_PROVINCE, "Proportional Representation (Provincial, Hare quota)"));
		//votingMenu.add(createVotingTypeMenuItem(VotingType.PR_DROOP_PROVINCE, "Proportional Representation (Provincial, Droop quota)"));
		votingMenu.add(createVotingTypeMenuItem(VotingType.PR_HARE_NATIONAL, "Proportional Representation (National, Hare quota)"));
		votingMenu.add(createVotingTypeMenuItem(VotingType.PR_DROOP_NATIONAL, "Proportional Representation (National, Droop quota)"));
		votingMenu.addSeparator();
		votingMenu.add(createVotingTypeMenuItem(VotingType.MMM_HARE, "Mixed-Member Majoritarian (Hare quota)"));
		votingMenu.add(createVotingTypeMenuItem(VotingType.MMM_DROOP, "Mixed-Member Majoritarian (Droop quota)"));
		votingMenu.addSeparator();
		votingMenu.add(createVotingTypeMenuItem(VotingType.MMP_DHONDT, "Mixed-Member Proportional (D'Hondt Method)"));
		votingMenu.add(createVotingTypeMenuItem(VotingType.MMP_SAINTELAGUE, "Mixed-Member Proportional (Webster/Sainte-Lague Method)"));
		menu.add(votingMenu);
		
		
		
		JPanel browsePanel = new JPanel();
		browsePanel.setLayout(new GridLayout(1,3));
		browsePanel.add(new JLabel("Data File:"));
		fileField = new JTextField("Elections.xml");
		fileField.setEditable(false);
		browsePanel.add(fileField);
		JButton browse = new JButton("Browse");
		browsePanel.add(browse);
		
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new BorderLayout());
		menuPanel.add(menu, BorderLayout.NORTH);
		menuPanel.add(browsePanel, BorderLayout.CENTER);
		GUI.add(menuPanel, BorderLayout.NORTH);
		
		currentPanel = new InputParty(electionData);
		GUI.add(currentPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3,2));
		JButton saveButton = new JButton("Save");
		partiesButton = new JButton("Edit Parties");
		regionsButton = new JButton("Edit Regions");
		JButton electionButton = new JButton("Run Election");
		JButton campaignButton = new JButton("Run Campaign");
		JButton rangeButton = new JButton("Run Range Calculator");
		XMLListener saveListener = new XMLListener(this, browse, saveButton);
		browse.addActionListener(saveListener);
		saveButton.addActionListener(saveListener);
		InputNavigationListener partyNavListener = new InputNavigationListener(this, InputForms.PARTY);
		partiesButton.addActionListener(partyNavListener);
		InputNavigationListener regionNavListener = new InputNavigationListener(this, InputForms.REGION);
		regionsButton.addActionListener(regionNavListener);
		RunElectionListener runElectionListener = new RunElectionListener(this, ElectionModes.ELECTION);
		electionButton.addActionListener(runElectionListener);
		RunElectionListener runCampaignListener = new RunElectionListener(this, ElectionModes.CAMPAIGN);
		campaignButton.addActionListener(runCampaignListener);
		//campaignButton.setEnabled(false);
		RunElectionListener runRangeListener = new RunElectionListener(this, ElectionModes.RANGE);
		rangeButton.addActionListener(runRangeListener);
		//rangeButton.setEnabled(false);
		buttonPanel.add(saveButton);
		buttonPanel.add(electionButton);
		buttonPanel.add(partiesButton);
		buttonPanel.add(campaignButton);
		buttonPanel.add(regionsButton);
		buttonPanel.add(rangeButton);
		GUI.add(buttonPanel, BorderLayout.SOUTH);
		
		buttonUpdate();
		
		GUI.pack();
		//GUI.setSize(400, 500);
		GUI.setVisible(true);
		GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	//Method to condense voting type creation
	private JMenuItem createVotingTypeMenuItem(VotingType votingType, String text){
		JMenuItem button = new JMenuItem(text);
		button.addActionListener(new VotingTypeListener(this, votingType));
		votingTypes.put(votingType, button);
		return button;
	}
	
	public static void main(String[] args){
		ElectionData e = null;
		//load configuration from default file location if available
		try{
			e = ElectionData.importFromXMLFile(new File("Elections.xml"));
		}catch(Exception ex){}
		//if not set, generate default configuration
		if(e == null){
			System.out.println("No configuration loaded, creating default");
			e = ElectionData.generateDefault();
		}
		new ElectionsUI(e);
	}
	
	public JFrame getFrame(){
		return GUI;
	}
	
	public void loadFromText(File f){
		try{
			electionData = ElectionData.importFromTextFile(f);
		}catch(Exception e){
			System.out.println(e.getMessage());
			return;
		}
		fileField.setText(f.getPath());
		GUI.remove(currentPanel);
		currentPanel = new InputParty(electionData);
		GUI.add(currentPanel, BorderLayout.CENTER);
		buttonUpdate();
		GUI.revalidate();
	}
	public void saveToText(File f){
		applyChanges();
		electionData.writeToTextFile(f);
	}
	public void loadFromXML(File f){
		try{
			electionData = ElectionData.importFromXMLFile(f);
		}catch(Exception e){
			System.out.println(e.getMessage());
			return;
		}
		fileField.setText(f.getPath());
		GUI.remove(currentPanel);
		currentPanel = new InputParty(electionData);
		GUI.add(currentPanel, BorderLayout.CENTER);
		buttonUpdate();
		GUI.revalidate();
	}
	public void saveToXML(File f){
		applyChanges();
		electionData.writeToXMLFile(f);
	}
	
	public void navigateInput(InputForms formType){
		applyChanges();
		GUI.remove(currentPanel);
		switch(formType){
			case PARTY:
				currentPanel = new InputParty(electionData);
				break;
			case REGION:
				currentPanel = new InputRegion(electionData);
				break;
		}
		GUI.add(currentPanel, BorderLayout.CENTER);
		buttonUpdate();
		GUI.revalidate();
	}
	
	public void runElection(ElectionModes electionType){
		applyChanges();
		
		switch(electionType){
			case ELECTION:
				UpdatedVoting election = new UpdatedVoting(electionData);
				election.update(0.10);
				election.results(votingType);
				election.setVisible(true);
				break;
			case CAMPAIGN:
				new CampaignFrame(electionData, votingType);
				break;
			case RANGE:
				new ResultsRangeFrame(electionData, votingType);
				break;
		}
	}
	
	public void setVotingType(VotingType v){
		votingType = v;
		buttonUpdate();
	}
	
	private void applyChanges(){
		if(currentPanel != null){
			currentPanel.updateElectionData();
		}
	}
	
	private void buttonUpdate(){
		partiesButton.setEnabled(currentPanel == null || currentPanel.getClass() != InputParty.class);
		regionsButton.setEnabled(currentPanel == null || currentPanel.getClass() != InputRegion.class);
		
		for(VotingType v : votingTypes.keySet()){
			boolean isCurrent = (v != votingType);
			votingTypes.get(v).setEnabled(isCurrent);
		}
		
		GUI.revalidate();
	}
}
