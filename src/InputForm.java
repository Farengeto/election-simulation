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


public class InputForm{
	//private File fileIn;
	private int seats;
	private long population;
	private List<Province> provinces;
	private List<Region> regions;
	private List<Party> parties;
	private Map<Party,Party> conversion;
	private String outFile = "ElectionsIn.txt";
	public boolean done = false;
	
	//create the input form using either the default or a given file
	public InputForm(){
		this("ElectionsIn.txt");
	}
	
	public InputForm(String inSource){
		seats = 0;
		population = 0;
		provinces = new ArrayList<Province>();
		regions = new ArrayList<Region>();
		parties = new ArrayList<Party>();
		conversion = new HashMap<>();
		File fileIn = new File(inSource);
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
	}
	
	//runs the input form with default file settings
	public static void main(String[] args){
		InputForm input = new InputForm();
		PartyInput partyIn = new PartyInput(input,input.getParties());
	}
	
	public List<Party> getParties(){
		return parties;
	}
	
	public List<Province> getProvinces(){
		return provinces;
	}
	
	public List<Region> getRegions(){
		return regions;
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
	
	public void setProvinces(List<Province> newProvinces){
		provinces = newProvinces;
	}
	
	public void setRegions(List<Region> newRegions){
		regions = newRegions;
	}
	
	//get the party from the file for a party in the Input, if one exists
	public Party getConversion(Party p){
		return conversion.get(p);
	}
	
	public String getOutFile(){
		return outFile;
	}
	
	//writes the updated data to the given output file
	//uses same format as input so data can be reused as input
	public void writeToFile(){
		try{
			PrintWriter writer = new PrintWriter(outFile, "UTF-8");
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
}