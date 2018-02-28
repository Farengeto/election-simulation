import java.awt.Color;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;


public class ElectionData{
	//private File fileIn;
	private int seats;
	private long population;
	private List<Province> provinces;
	private List<Region> regions;
	private List<Party> parties;
	private Map<Party,Party> conversion;
	public boolean done = false;
	
	//create the input form using either the default or a given file
	public ElectionData(){
		seats = 0;
		population = 0;
		provinces = new ArrayList<Province>();
		regions = new ArrayList<Region>();
		parties = new ArrayList<Party>();
		conversion = new HashMap<>();
	}
	
	//runs the input form with default file settings
	public static void main(String[] args){
		try{
			ElectionData input = ElectionData.importFromTextFile(new File("ElectionsIn.txt"));
			PartyInput partyIn = new PartyInput(input,input.getParties());
		}catch(Exception e){}
	}
	
	public List<Party> getParties(){
		return parties;
	}
	public Party getParty(int index){
		return parties.get(index);
	}
	//maps conversion for old parties to new parties, if applicable
	//used to maintain region data for parties with same names
	public void setParties(List<Party> newParties){
		for(Party np : newParties){
			for(Party p : parties){
				if(p.getName().equals(np.getName())){
					conversion.put(np,p);
				}
			}
		}
		parties = newParties;
	}
	public void addParty(Party newParty){
		parties.add(newParty);
	}
	
	public List<Province> getProvinces(){
		return provinces;
	}
	public void setProvinces(List<Province> newProvinces){
		provinces = newProvinces;
		regions.clear();
	}
	public void addProvince(Province newProvince){
		provinces.add(newProvince);
	}
	
	public List<Region> getRegions(){
		return regions;
	}
	public Region getRegion(int index){
		return regions.get(index);
	}
	public void setRegions(List<Region> newRegions){
		regions = newRegions;
		population = 0;
		seats = 0;
		for(Region r : newRegions){
			population += r.population;
			seats += r.seats;
		}
	}
	public void addRegion(Region newRegion){
		regions.add(newRegion);
		population += newRegion.population;
		seats += newRegion.seats;
	}
	
	//get the party from the file for a party in the Input, if one exists
	public Party getConversion(Party p){
		return conversion.get(p);
	}
	
	public static ElectionData importFromTextFile(File f) throws Exception{
		ElectionData election = new ElectionData();
		Scanner sc = new Scanner(f);
		
		//skip intro lines
		String line = sc.nextLine();
		line = sc.nextLine();
		line = sc.nextLine();
		line = sc.nextLine();
		do{
			line = sc.nextLine();
		}while(line.equals(""));
		
		//read list of parties
		while(!line.equals("Province:")){
			String name = line;
			line = sc.nextLine();
			Double approval = Double.parseDouble(line.substring(line.indexOf("Approval:")+9,line.indexOf('%')).trim())/100.0;
			int r = sc.nextInt();
			int g = sc.nextInt();
			int b = sc.nextInt();
			sc.nextLine();
			Party newParty = new Party(name,new Color(r,g,b),approval);
			election.addParty(newParty);
			line = sc.nextLine();
			while(line.equals("")){
				line = sc.nextLine();
			}
		}
		//skip to first province entry
		do{
			line = sc.nextLine();
		}while(line.length() == 0 || line.charAt(line.length()-1) != ':');
		
		//read all Provinces and regions
		//reads until end of file, or reach delimiter string of "---#"
		while(sc.hasNextLine() && !line.contains("---#")){
			//initialize name and regions list
			String pName = line.substring(0,line.length()-1);
			Province newProvince = new Province(pName);
			election.addProvince(newProvince);
			
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
					int rSeat = reg.nextInt();
					Map<Party,Double> rSupport = new HashMap<>();
					int count = 0;
					//read regional party support, entries cannot exceed party count
					while(reg.hasNextDouble() && count < election.getParties().size()){
						rSupport.put(election.getParty(count), reg.nextDouble());
						count++;
					}
					//create region and add to division lists
					Region newRegion = new Region(rName,rPop,rSeat,newProvince,rSupport);
					election.addRegion(newRegion);
					reg.close();
				}
			}while((line.length() == 0 || line.charAt(line.length()-1) != ':') && sc.hasNextLine() && !line.contains("---#"));
		}
		sc.close();
		return election;
	}
	
	public static ElectionData importFromXMLFile(File f) throws Exception{
		return null;
	}
	
	//writes the updated data to the given output file
	//uses same format as input so data can be reused as input
	public void writeToTextFile(File f){
		try{
			PrintWriter writer = new PrintWriter(f, "UTF-8");
			writer.println("Parties:");
			writer.println("Party name");
			writer.println("Approval: %");
			writer.println("RGB party colour");
			writer.println("");
			for(Party p : parties){
				writer.println(p.getName());
				writer.println("Approval: " + p.getApproval()*100 + "%");
				Color c = p.getColor();
				writer.println(c.getRed() + " " + c.getGreen() + " " + c.getBlue());
				writer.println("");
			}
			writer.println("Province:");
			writer.println("Region\t\tPopulation\tSeats\tParty Support (Party 1, Party 2, Party 3...)");
			writer.println("");
			for(Province pr : provinces){
				writer.println(pr.getName() + ":");
				for(Region r : pr.getRegions()){
					writer.print(r.getName() + "\t" + r.getPopulation() + "\t\t" + r.getSeats() + "\t");
					for(Party p : parties){
						writer.print(r.getSupport(p)*100 + "\t");
					}
					writer.println();
				}
			}
			writer.close();
		} catch (IOException e) {};
	}
	
	public void writeToXMLFile(File f){
		
	}
	
	public static ElectionData generateDefault(){
		ElectionData election = new ElectionData();
		
		election.addParty(new Party("Red Party", new Color(255, 0, 0)));
		election.addParty(new Party("Blue Party", new Color(255, 0, 0)));
		
		Province p = new Province("Subdivision");
		election.addProvince(p);
		
		Map<Party, Double> support = new HashMap<>();
		support.put(election.getParty(0), 0.6);
		support.put(election.getParty(1), 0.4);
		election.addRegion(new Region("City A", 1000, 5, p, support));
		support = new HashMap<>();
		support.put(election.getParty(0), 0.4);
		support.put(election.getParty(1), 0.6);
		election.addRegion(new Region("City B", 600, 3, p, support));
		support = new HashMap<>();
		support.put(election.getParty(0), 0.5);
		support.put(election.getParty(1), 0.5);
		election.addRegion(new Region("City C", 400, 2, p, support));
		
		return election;
	}
}