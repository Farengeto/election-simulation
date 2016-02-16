import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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


public class UpdatedVoting extends JFrame{
	private File fileIn;
	private int seats;
	private long population;
	private List<Province> provinces;
	private List<Region> regions;
	private List<Party> parties;
	private NationalResultsPanel nResults;
	private ProvincialResultsPanel pResults;
	private RegionalResultsPanel rResults;
	
	public UpdatedVoting(){
		this("ElectionsIn.txt");
	}
	
	public UpdatedVoting(String inSource){
		super("Elections");
		seats = 0;
		population = 0;
		provinces = new ArrayList<Province>();
		regions = new ArrayList<Region>();
		parties = new ArrayList<Party>();
		fileIn = new File(inSource);
		try{
			Scanner sc = new Scanner(fileIn);
			//skip intro lines
			String line = sc.nextLine();
			line = sc.nextLine();
			line = sc.nextLine();
			line = sc.nextLine();
			//skip any blank lines
			do{
				line = sc.nextLine();
			}while(line.equals(""));
			//read list of parties
			parties = new ArrayList<Party>();
			while(!line.equals("Province:")){
				String name = line;
				line = sc.nextLine();
				Double approval = Double.parseDouble(line.substring(line.indexOf("Approval:")+9,line.indexOf('%')).trim())/100.0;
				int r = sc.nextInt();
				int g = sc.nextInt();
				int b = sc.nextInt();
				sc.nextLine();
				Party nParty = new Party(name,new Color(r,g,b),approval);
				parties.add(nParty);
				line = sc.nextLine();
				while(line.equals("")){
					line = sc.nextLine();
				}
			}
			//skip to first province entry
			do{
				line = sc.nextLine();
			}while(line.length() == 0 || line.charAt(line.length()-1) != ':');
			//read all Provinces
			//read until end of file, or reach delimiter string of "---#"
			while(sc.hasNextLine() && !line.contains("---#")){
				//initialize name and regions list
				String pName = line.substring(0,line.length()-1);
				Province p = new Province(pName);
				
				//read all regions in province
				//scan all lines until next province 
				do{	
					line = sc.nextLine();
					//read line if not blank
					if(!line.equals("") && !line.contains("---#") && line.charAt(line.length()-1) != ':'){
						Scanner reg = new Scanner(line);
						String rName = reg.next();
						//check for additional words in name
						while(!reg.hasNextLong()){
							rName += (" " + reg.next());
						}
						long rPop = reg.nextLong();
						population += rPop;
						int rSeat = reg.nextInt();
						seats += rSeat;
						Map<Party,Double> rSupport = new HashMap<>();
						int count = 0;
						//read regional party support, entries cannot exceed party count
						while(reg.hasNextDouble() && count < parties.size()){
							rSupport.put(parties.get(count), reg.nextDouble());
							count++;
						}
						//create region and add to division lists
						Region r = new Region(rName,rPop,rSeat,p,rSupport);
						regions.add(r);
						reg.close();
					}
				}while((line.length() == 0 || line.charAt(line.length()-1) != ':') && sc.hasNextLine() && !line.contains("---#"));
				provinces.add(p);
			}
			sc.close();
		}catch(IOException ex){}
		
		//set-up JFrame
		this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));
		nResults = new NationalResultsPanel(this);
		pResults = new ProvincialResultsPanel(this);
		rResults = new RegionalResultsPanel(this);
		//each panel is contained in a scroll pane
		JScrollPane nPane = new JScrollPane(nResults);
		JScrollPane pPane = new JScrollPane(pResults);
		JScrollPane rPane = new JScrollPane(rResults);
		add(nPane);
		add(pPane);
		add(rPane);
		pack();
		//Minimum size is set on national and provincial panels to prevent their width from change
		Dimension min = new Dimension((int)nResults.getSize().getWidth(),1);
		Dimension max = new Dimension((int)nResults.getSize().getWidth(),10000);
		nPane.setMinimumSize(min);
		nPane.setMaximumSize(max);
		nPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		min = new Dimension((int)pResults.getSize().getWidth(),1);
		max = new Dimension((int)pResults.getSize().getWidth(),10000);
		pPane.setMinimumSize(min);
		pPane.setMaximumSize(max);
		pPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		//Prevent frame size from exceeding screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if(screenSize.getWidth() <= getWidth() || screenSize.getHeight() <= getHeight()){
			//int width = (int)Math.min(screenSize.getWidth(),getWidth());
			//int height = (int)Math.min(screenSize.getHeight(),getHeight());
			//setSize(width,height);
			setExtendedState(MAXIMIZED_BOTH);
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) throws InterruptedException {
		//perform election
		UpdatedVoting election = new UpdatedVoting();
		election.update(0.10);
		election.results();
		//print results
		//election.printResults();
		//show results
		election.setVisible(true);
		election.repaint();
	}
	
	//randomize party support levels by a randomized amount
	//Generates values for the national, provincial and regional levels to simulate trends on all levels of the election
	//Uses Gaussian distribution for randomization, takes a double for the standard deviation
	public void update(double shiftMargin){
		//randomize national support
		Random random = new Random();
		Map<Party,Double> natShift = new HashMap<>();
		for(Party p : parties){
			natShift.put(p, Math.max(1.0 + random.nextGaussian()*shiftMargin,0.0));
		}
		for(Province pr : provinces){
			pr.update(shiftMargin,natShift);
		}
		
	}
	
	//perform elections and tally results
	public void results(){
		for(Party p : parties){
			p.setResults(0,0);
		}
		for(Province pr : provinces){
			pr.results();
			Set<Party> parties = pr.getParties();
			for(Party p : parties){
				long vote = p.getVotes() + pr.getVotes().get(p);
				int seat = p.getSeats() + pr.getResults().get(p);
				p.setResults(vote,seat);
			}
		}
		rResults.updateTable();
	}	
	
	//print results to the console
	public void printResults(){
		System.out.println("Results:");
		List<Party> parties = getParties();
		parties.sort(new NationalComparator());
		for(Party p : parties){
			System.out.println(p.getResults());
		}
		System.out.println();
		System.out.println("Divisions:");
		for(Province pr : getProvinces()){
			System.out.println(pr.toString());
			System.out.println();
		}
	}
		

	public void reset(){
		for(Party p : parties){
			p.setResults(0,0);
		}
	}
	
	//get methods
	public int getSeats(){
		return seats;
	}
	
	public long getPopulation(){
		return population;
	}
	
	public List<Province> getProvinces(){
		return provinces;
	}
	
	public List<Region> getRegions(){
		return regions;
	}
	
	public List<Party> getParties(){
		return parties;
	}
	
	public Map<Party,Long> getVotes(){
		Map<Party,Long> v = new HashMap<>();
		for(Party p : parties){
			v.put(p, p.getVotes());
		}
		return v;
	}
	
	public Map<Party,Integer> getResults(){
		Map<Party,Integer> r = new HashMap<>();
		for(Party p : parties){
			r.put(p, p.getSeats());
		}
		return r;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}
}