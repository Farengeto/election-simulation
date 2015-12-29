import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

//Simulation levels:
//National level is the entirety of the country (i.e. Canada)
//Provinces represent highest level divisions of country (i.e. the province of Ontario)
//Regions represent sub-divisions of the provinces (i.e. the Greater Toronto Area)
//
//To simulate only one division level, set one region per province or place all regions in one province
//To simulate single-seat first past the post voting, set seats in each region to one and define each seat as its own region

public class UpdatedVoting extends JPanel{
	private File fileIn;
	private int seats;
	private long population;
	private List<Province> provinces;
	private List<Region> regions;
	private List<Party> parties;
	
	public UpdatedVoting(){
		this("ElectionsIn.txt");
	}
	
	public UpdatedVoting(String inSource){
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
				line = sc.nextLine();
				while((line.length() == 0 || line.charAt(line.length()-1) != ':') && sc.hasNextLine() && !line.contains("---#")){
					
					//read line if not blank
					if(!line.equals("") && !line.contains("---#")){
						Scanner reg = new Scanner(line);
						String rName = reg.next();
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
					line = sc.nextLine();
				}
				provinces.add(p);
			}
			sc.close();
		}catch(IOException ex){}
	}
	
	public static void main(String[] args) throws InterruptedException {
		JFrame frame = new JFrame("Elections");
		UpdatedVoting election = new UpdatedVoting();
		frame.add(election);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int num = election.getParties().size();
		int width = Math.min((int)screenSize.getWidth(),980+60*num);
		int height = Math.min((int)screenSize.getHeight(),540+35*num);
		frame.setSize(width,height);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		election.update(0.10);
		System.out.println("Beginning Election...");
		election.results();
		System.out.println("Results:");
		List<Party> parties = election.getParties();
		parties.sort(new NationalComparator());
		for(Party p : parties){
			System.out.println(p.getResults());
		}
		System.out.println();
		System.out.println("Divisions:");
		for(Province pr : election.getProvinces()){
			System.out.println(pr.toString());
			System.out.println();
		}
		election.repaint();
	}
	
	//randomize party support levels by a randomized amount
	//Generates values for the national, provincial and regional levels to simulate trends on all levels of the election
	//Uses gaussian distribution for randomization, takes a double for the standard deviation
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
	}
	
	public void reset(){
		for(Party p : parties){
			p.setResults(0,0);
		}
	}
	
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
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		//find the party with the most national seats for scaling
		int max = 1;
		for(Party p : parties){
			max = Math.max(max,p.getSeats());
		}
		int count = 0;
		//draw bars for national parties
		parties.sort(new NationalComparator());
		for(Party p : parties){
			g.setColor(Color.BLACK);
			g.drawString("" + p.getName(),25,25+count*35);
			g.drawString("" + p.getSeats(),240*p.getSeats()/max+30,39+count*35);
			g.setColor(p.getColor());
			g.fillRect(25, 30+count*35, Math.max(240*p.getSeats()/max,1), 10);
			count++;
		}
		//draw pi chart for national seat counts
		g.setColor(Color.GRAY);
		g.fillOval(50,40+35*parties.size(),200,200);
		int startAngle = 0;
		for(Party p : parties) {
			int arcAngle = (int) Math.round((double)p.getSeats()/seats * 360.0);
			g.setColor(p.getColor());
			g.fillArc(50, 40+35*parties.size(), 200, 200, 
					startAngle, arcAngle);
			startAngle += arcAngle;
		}
		//draw pi chart for national popular voting
		g.setColor(Color.GRAY);
		g.fillOval(50,290+35*parties.size(),200,200);
		startAngle = 0;
		for(Party p : parties) {
			int arcAngle = (int) Math.round((double)p.getVotes()/population * 360.0);
			g.setColor(p.getColor());
			g.fillArc(50, 290+35*parties.size(), 200, 200, 
					startAngle, arcAngle);
			startAngle += arcAngle;
		}
		
		//conclusion of drawing national results panel
		g.drawLine(300, 0, 300, 900);
		
		//provincial and regional results panel
		count = 0;
		for(Province pr : provinces){
			parties.sort(new DivisionalComparator((Division)pr));
			//draw pi chart for regional popular voting
			g.setColor(Color.GRAY);
			g.fillOval(350,25+125*count,100,100);
			startAngle = 0;
			for (Party p : parties) {
				int arcAngle = (int) Math.round((double)pr.getVotes().get(p)/pr.population * 360.0);
				g.setColor(p.getColor());
				g.fillArc(350,25+125*count, 100, 100, 
						startAngle, arcAngle);
				startAngle += arcAngle;
			}
			g.setColor(Color. BLACK);
			g.drawString(pr.getName(), 375, 20+125*count);
			//draw bar charts for party seats in each province
			max = 1;
			for(Party p : parties){
				max = Math.max(max,pr.getResults().get(p));
			}
			int pCount = 0;
			for(Party p : parties){
				if(count < 10 && pr.getResults().get(p) > 0){
					g.setColor(Color.BLACK);
					g.drawString("" + pr.getResults().get(p),480+200*pr.getResults().get(p)/max,34+125*count+10*pCount);
					g.setColor(p.getColor());
					g.fillRect(475, 25+125*count+10*pCount, Math.max(200*pr.getResults().get(p)/max,1), 10);
				}
				pCount++;
			}
			count++;
		}
		//display detailed stats by region
		parties.sort(new NationalComparator());
		count = 0;
		g.setColor(Color.BLACK);
		for(Party p : parties){
			g.drawString("" + p.getName().substring(0,3).toUpperCase(),900+60*count,15);
			count++;
		}
		count = 0;
		for(Region r : regions){
			g.setColor(Color.BLACK);
			g.drawString(r.getName(),700,40+20*count);
			g.drawString(Long.toString(r.getPopulation()),800,40+20*count);
			int pCount = 0;
			for(Party p : parties){
				g.drawString("" + r.getResults().get(p),900+60*pCount,40+20*count);
				g.drawString(Math.round(1000.0 * (double)r.getVotes().get(p) / (double)r.getPopulation())/10.0 + "%",920+60*pCount,40+20*count);
				pCount++;
			}
			count++;
		}
	}
}