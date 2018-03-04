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
		votingType = VotingType.PR_HARE;
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
		JMenuItem PRRegionHareMenu = new JMenuItem("Proportional Representation (Regional, Hare quota)");
		JMenuItem PRRegionDroopMenu = new JMenuItem("Proportional Representation (Regional, Droop quota)");
		JMenuItem PRNationHareMenu = new JMenuItem("Proportional Representation (National, Hare quota)");
		JMenuItem PRNationDroopMenu = new JMenuItem("Proportional Representation (National, Droop quota)");
		JMenuItem FPTPRegionMenu = new JMenuItem("First-Past-the-Post (Regional)");
		JMenuItem FPTPProvinceMenu = new JMenuItem("First-Past-the-Post (Provincial)");
		JMenuItem FPTPNationMenu = new JMenuItem("First-Past-the-Post (National)");
		PRRegionHareMenu.addActionListener(new VotingTypeListener(this, VotingType.PR_HARE));
		PRRegionDroopMenu.addActionListener(new VotingTypeListener(this, VotingType.PR_DROOP));
		PRNationHareMenu.addActionListener(new VotingTypeListener(this, VotingType.PR_HARE_NATIONAL));
		PRNationDroopMenu.addActionListener(new VotingTypeListener(this, VotingType.PR_DROOP_NATIONAL));
		FPTPRegionMenu.addActionListener(new VotingTypeListener(this, VotingType.FPTP));
		FPTPProvinceMenu.addActionListener(new VotingTypeListener(this, VotingType.FPTP_PROVINCE));
		FPTPNationMenu.addActionListener(new VotingTypeListener(this, VotingType.FPTP_NATIONAL));
		votingTypes.put(VotingType.PR_HARE, PRRegionHareMenu);
		votingTypes.put(VotingType.PR_DROOP, PRRegionDroopMenu);
		votingTypes.put(VotingType.PR_HARE_NATIONAL, PRNationHareMenu);
		votingTypes.put(VotingType.PR_DROOP_NATIONAL, PRNationDroopMenu);
		votingTypes.put(VotingType.FPTP, FPTPRegionMenu);
		votingTypes.put(VotingType.FPTP_PROVINCE, FPTPProvinceMenu);
		votingTypes.put(VotingType.FPTP_NATIONAL, FPTPNationMenu);
		votingMenu.add(PRRegionHareMenu);
		votingMenu.add(PRRegionDroopMenu);
		votingMenu.add(PRNationHareMenu);
		votingMenu.add(PRNationDroopMenu);
		votingMenu.add(FPTPRegionMenu);
		votingMenu.add(FPTPProvinceMenu);
		votingMenu.add(FPTPNationMenu);
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
		campaignButton.setEnabled(false);
		RunElectionListener runRangeListener = new RunElectionListener(this, ElectionModes.RANGE);
		rangeButton.addActionListener(runRangeListener);
		rangeButton.setEnabled(false);
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
				break;
			case RANGE:
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
