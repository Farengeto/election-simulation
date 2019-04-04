package gui;

import election.ElectionData;
import election.VotingData;
import election.NationalComparator;
import nation.Party;
import nation.Province;
import votingLibrary.VotingType;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.File;
import java.util.*;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * @author Travis
 * Simulates the election of a nation from a data set.
 * Output can be displayed in a JFrame. The frame is notVisible by default for operations that do not require displaying
 * National results for each party are restored in their respective Party object
 * Divisional results for each party are stored in the "results" map for the respective division
 * 
 * Simulation levels:
 * National level is the entirety of the country (i.e. Canada)
 * Provinces represent highest level divisions of country (i.e. the province of Ontario)
 * Regions represent sub-divisions of the provinces (i.e. the Greater Toronto Area)
 * 
 * To simulate only one division level, set one region per province or place all regions in one province
 * To simulate single-seat first past the post voting, set seats in each region to one and define each seat as its own region
 */


public class UpdatedVoting extends JFrame {
	private NationalResultsPanel nResults;
	private ProvincialResultsPanel pResults;
	private RegionalResultsPanel rResults;
	private ElectionData electionData;
	private VotingData votingData;

	public UpdatedVoting() {
		this(ElectionData.generateDefault());
	}

	public UpdatedVoting(String inSource) throws Exception {
		this(ElectionData.importFromTextFile(new File(inSource)));
	}

	public UpdatedVoting(ElectionData source) {
		super("Election Results");
		electionData = source;
		votingData = new VotingData(electionData);

		//set-up JFrame
		this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));
		nResults = new NationalResultsPanel(electionData, votingData);
		pResults = new ProvincialResultsPanel(electionData, votingData);
		rResults = new RegionalResultsPanel(electionData, votingData);
		//each panel is contained in a scroll pane
		JScrollPane nPane = new JScrollPane(nResults);
		JScrollPane pPane = new JScrollPane(pResults);
		JScrollPane rPane = new JScrollPane(rResults);
		add(nPane);
		add(pPane);
		add(rPane);
		pack();
		//Minimum size is set on national and provincial panels to prevent their width from change
		Dimension min = new Dimension((int) nResults.getSize().getWidth(), 1);
		Dimension max = new Dimension((int) nResults.getSize().getWidth(), 10000);
		nPane.setMinimumSize(min);
		nPane.setMaximumSize(max);
		nPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		min = new Dimension((int) pResults.getSize().getWidth(), 1);
		max = new Dimension((int) pResults.getSize().getWidth(), 10000);
		pPane.setMinimumSize(min);
		pPane.setMaximumSize(max);
		pPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		//Prevent frame size from exceeding screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (screenSize.getWidth() <= getWidth() || screenSize.getHeight() <= getHeight()) {
			setExtendedState(MAXIMIZED_BOTH);
		}
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	//randomize party support levels by a randomized amount
	//Generates values for the national, provincial and regional levels to simulate trends on all levels of the election
	//Uses Gaussian distribution for randomization, parameter is assumed to be a two sigma (95%) confidence interval
	public void update(double confidenceMargin) {
		votingData.modifySupport(confidenceMargin / 2);
	}

	//perform elections and tally results
	public void results(VotingType votingMethod, double electionThreshold) {
		votingData.calculateResults(votingMethod, electionThreshold);
		rResults.updateTable();
		revalidate();
	}

	//print results to the console
	public void printResults() {
		System.out.println("Results:");
		List<Party> parties = electionData.getParties();
		parties.sort(new NationalComparator(votingData));
		for (Party p : parties) {
			System.out.println(p.getResults());
		}
		System.out.println();
		System.out.println("Divisions:");
		for (Province pr : electionData.getProvinces()) {
			System.out.println(pr.toString());
			System.out.println();
		}
	}

	//get methods
	public Map<Party, Long> getVotes() {
		Map<Party, Long> v = new HashMap<>();
		for (Party p : electionData.getParties()) {
			v.put(p, votingData.getVotesNation(p));
		}
		return v;
	}

	public Map<Party, Integer> getResults() {
		Map<Party, Integer> r = new HashMap<>();
		for (Party p : electionData.getParties()) {
			r.put(p, votingData.getSeatsNation(p));
		}
		return r;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}
}