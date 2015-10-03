import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

//Simulation levels:
//National level is the entirety of the simulation
//Provinces represent highest level divisions of country (i.e. the province of Ontario)
//Regions represent sub-divisions of the provinces (i.e. the Greater Toronto Area)
//
//To simulate only on sub-division level, set one region per province
//To simulate first past the post voting, set seats per region to one

public class UpdatedVoting extends JPanel{
	//read/write files
	public static String fileInName = "ElectionsIn.txt";
	public static File fileIn = new File(fileInName);
	public static String fileOut = "ElectionsOut.txt";
	//size parameters
	private int numProvinces;
	private int numParties;
	private int numSeats;
	private int numRegions;
	//populations
	private long population = 0;
	private long[] provincePopulations;
	private long[] regionPopulations;
	private int[] provinces;
	private int[] provinceSeats;
	private int[] regionSeats;
	//names
	private String[] provinceNames;
	private String[] regionNames;
	//parties
	private String[] partyNames;
	private Color[] partyColors;
	private double[] partyApproval;
	private double[][] partySupport;
	//results
	private long[][] votes;
	private int[][] result;
	private int[] nationalSeats;
	private long[] nationalVotes;
	private int[][] provincialSeats;
	private long[][] provincialVotes;

	public UpdatedVoting(){
		try{
			Scanner sc = new Scanner(fileIn);
			sc.next();
			numProvinces = sc.nextInt();
			sc.next();
			numRegions = sc.nextInt();
			//Initialize all fields using regions and provinces
			provincePopulations = new long[numProvinces];
			regionPopulations = new long[numRegions];
			provinces = new int[numRegions];
			provinceSeats = new int[numProvinces];
			regionSeats = new int[numRegions];
			provinceNames = new String[numProvinces];
			regionNames = new String[numRegions];
			//Initialize region and province names
			sc.nextLine();
			int k = 0;
			for(int i = 0; i < numProvinces; i++){
				//Set province name
				Scanner s2 = new Scanner(sc.nextLine());
				provinceNames[i] = s2.next().trim();
				provinceNames[i] = provinceNames[i].substring(0, provinceNames[i].length()-1);
				//Set region names, and which province the region belongs to
				while(s2.hasNext()){
					regionNames[k] = s2.next().trim();
					provinces[k] = i;
					k++;
				}
				s2.close();
			}
			sc.nextLine();
			sc.nextLine();
			//Initialize populations
			population = 0;
			for(int i = 0; i < numRegions; i++){
				regionPopulations[i] = sc.nextLong();
				provincePopulations[provinces[i]] += regionPopulations[i];
				population += regionPopulations[i];
			}
			sc.nextLine();
			sc.nextLine();
			sc.nextLine();
			//Initialize seat counts for all levels
			numSeats = 0;
			for(int i = 0; i < numRegions; i++){
				regionSeats[i] = sc.nextInt();
				provinceSeats[provinces[i]] += regionSeats[i];
				numSeats += regionSeats[i];
			}
			sc.nextLine();
			sc.nextLine();
			sc.next();
			//Initialize Parties and their relative levels of support
			numParties = sc.nextInt();
			partyNames = new String[numParties];
			partyColors = new Color[numParties];
			partyApproval = new double[numParties];
			partySupport = new double[numRegions][numParties];
			votes = new long[numRegions][numParties];
			result = new int[numRegions][numParties];
			nationalSeats = new int[numParties];
			nationalVotes = new long[numParties];
			provincialSeats = new int[numProvinces][numParties];
			provincialVotes = new long[numProvinces][numParties];
			sc.nextLine();
			for(int i = 0; i < numParties; i++){
				//Initialize party names and their approval ratings
				//Last party is a hardcoded independent/other with default settings
				partyNames[i] = sc.nextLine().trim();
				if(i < numParties-1){
					partyApproval[i] = Double.parseDouble(partyNames[i].substring(partyNames[i].indexOf("Approval:")+10,partyNames[i].indexOf('%')))/100.0;
				}
				else{
					partyNames[i] = partyNames[i].substring(0,partyNames[i].indexOf(" - "));
					partyApproval[i] = 0.5;
				}
				//initialize party colours
				int r = sc.nextInt();
				int g = sc.nextInt();
				int b = sc.nextInt();
				partyColors[i] = new Color(r,g,b);
				//initialize regional support levels
				for(int j = 0; j < numRegions; j++){
					partySupport[j][i] = sc.nextDouble();
				}
				sc.nextLine();
			}
			sc.close();
		}catch(IOException ex){}
		
		//convert support for each region into 
		for(int i = 0; i < numRegions; i++){
			double count = 0;
			for(int j = 0; j < numParties; j++){
				count += partySupport[i][j];
			}
			for(int j = 0; j < numParties; j++){
				partySupport[i][j] = partySupport[i][j]/count;
			}
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		JFrame frame = new JFrame("Elections");
		UpdatedVoting election = new UpdatedVoting();
		frame.add(election);
		frame.setSize(1500, 900);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		election.update(0.10);
		election.voting();
		election.repaint();
	}
	
	//update party voting
	public void update(double shiftMargin){
		Random random;
		double[] nationalShift = new double[numParties];
		double[][] provincialShift = new double[numProvinces][numParties];
		double[][] regionalShift = new double[numRegions][numParties];
		//randomize party support levels
		for(int p = 0; p < numParties-1; p++){
			random = new Random();
			nationalShift[p] = Math.max(1.0 + random.nextGaussian()*shiftMargin*2.0,0.0);
			for(int s = 0; s < numProvinces; s++){
				provincialShift[s][p] = Math.max(1.0 + random.nextGaussian()*shiftMargin,0.0);
			}
			for(int s = 0; s < numRegions; s++){
				regionalShift[s][p] = Math.max(1.0 + random.nextGaussian()*shiftMargin,0.0);
			}
		}
		//set independents to a neutral modifier nationally and provincially, but allow regional fluctuations
		random = new Random();
		nationalShift[numParties-1] = 1.0;
		for(int s = 0; s < numProvinces; s++){
			provincialShift[s][numParties-1] = 1.0;
		}
		for(int r = 0; r < numRegions; r++){
			regionalShift[r][numParties-1] = Math.max(1.0 + random.nextGaussian()*shiftMargin,0.0);
		}
		//set support level changes
		for(int r = 0; r < numRegions; r++){
			double count = 0;
			for(int p = 0; p < numParties; p++){
				//if party support in a region is zero (i.e. regional parties located only in certain areas) it will remain zero. Party support cannot be reduced to zero.
				if(partySupport[r][p] != 0){
					partySupport[r][p] = Math.max(partySupport[r][p] * nationalShift[p] * provincialShift[provinces[r]][p] * regionalShift[r][p],0.001);
				}
				count += partySupport[r][p];
			}
			//convert new support levels into decimal percentages
			for(int p = 0; p < numParties; p++){
				partySupport[r][p] = partySupport[r][p]/count;
			}
		}
	}
	
	//convert popularity values into vote counts
	public void voting(){
		//reset result arrays to initial states
		reset();
		for(int r = 0; r < numRegions; r++){
			long count = regionPopulations[r];
			//give each party a vote count proportional to its popularity in the region
			for(int p = 0; p < numParties-1; p++){
				votes[r][p] = (int)Math.round(regionPopulations[r] * partySupport[r][p]);
				provincialVotes[provinces[r]][p] += votes[r][p];
				nationalVotes[p] += votes[r][p];
				count -= votes[r][p];
			}
			//give any leftover votes to the independents
			votes[r][numParties-1] = count;
		}
		//calculate results
		results();
	}
	
	//converts completed voting into electoral results
	public void results(){
		for(int i = 0; i < numRegions; i++){
			//Define quote for the largest remainder method
			//hare quota, biased towards smaller parties
			long quota = regionPopulations[i] / regionSeats[i];
			//droop quota, biased towards larger parties
			//long quota = (regionPopulations[i] / (regionSeats[i]+1)) + 1;
			long[] remainder = new long[numParties];
			int remainingSeats = regionSeats[i];
			//use the quota to find the number of seats won, and calculate the remainder
			for(int j = 0; j < numParties; j++){
				int seatsWon = (int)(votes[i][j] / quota);
				result[i][j] = seatsWon;
				remainingSeats -= seatsWon;
				remainder[j] = votes[i][j] % quota;
			}
			//allocate remaining seats using highest remainder method
			while(remainingSeats > 0){
				int max = 0;
				for(int j = 1; j < numParties; j++){
					if(remainder[j] > remainder[max]){
						max = j;
					}
					else if(remainder[j] == remainder[max] && result[i][j] > result[i][max]){
						max = j;
					}
				}
				result[i][max]++;
				remainder[max] = 0;
				remainingSeats--;
			}
		}
		//sum regional seats into a party's national seat totals
		for(int i = 0; i < numParties; i++){
			nationalSeats[i] = 0;
			for(int j = 0; j < numRegions; j++){
				nationalSeats[i] += result[j][i];
				provincialSeats[provinces[j]][i] += result[j][i];
			}
		}
	}
	
	//result all result arrays to initial state
	//must be executed in order to run election again
	public void reset(){
		votes = new long[numRegions][numParties];
		result = new int[numRegions][numParties];
		nationalSeats = new int[numParties];
		nationalVotes = new long[numParties];
		provincialSeats = new int[numProvinces][numParties];
		provincialVotes = new long[numProvinces][numParties];
	}
	
	public int[] getSeats(){
		return nationalSeats;
	}
	
	public long[] getVotes(){
		return nationalVotes;
	}
	
	public int getPartyNum(){
		return numParties;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		//find the party with the most national seats for scaling
		int max = 1;
		for(int i = 0; i < numParties; i++){
			max = Math.max(max,nationalSeats[i]);
		}
		//draw bars for national parties
		for(int i = 0; i < numParties; i++){
			g.setColor(Color.BLACK);
			g.drawString("" + partyNames[i],25,25+i*35);
			g.drawString("" + nationalSeats[i],240*nationalSeats[i]/max+30,39+i*35);
			g.setColor(partyColors[i]);
			g.fillRect(25, 30+i*35, Math.max(240*nationalSeats[i]/max,1), 10);
		}
		//draw pi chart for national seat counts
		g.setColor(Color.GRAY);
		g.fillOval(50,40+35*numParties,200,200);
		int startAngle = 0;
		for (int i = 0; i < numParties; i++) {
			int arcAngle = (int) Math.round((double)nationalSeats[i]/numSeats * 360.0);
			g.setColor(partyColors[i]);
			g.fillArc(50, 40+35*numParties, 200, 200, 
					startAngle, arcAngle);
			startAngle += arcAngle;
		}
		//draw pi chart for national popular voting
		g.setColor(Color.GRAY);
		g.fillOval(50,290+35*numParties,200,200);
		startAngle = 0;
		for (int i = 0; i < numParties; i++) {
			int arcAngle = (int) Math.round((double)nationalVotes[i]/population * 360.0);
			g.setColor(partyColors[i]);
			g.fillArc(50, 290+35*numParties, 200, 200, 
					startAngle, arcAngle);
			startAngle += arcAngle;
		}
		
		//conclusion of drawing national results panel
		g.drawLine(300, 0, 300, 900);
		
		//provincial and regional results panel
		for(int k = 0; k < numProvinces; k++){
			//draw pi chart for regional popular voting
			g.setColor(Color.GRAY);
			g.fillOval(350,25+125*k,100,100);
			startAngle = 0;
			for (int i = 0; i < numParties; i++) {
				int arcAngle = (int) Math.round((double)provincialVotes[k][i]/provincePopulations[k] * 360.0);
				g.setColor(partyColors[i]);
				g.fillArc(350,25+125*k, 100, 100, 
						startAngle, arcAngle);
				startAngle += arcAngle;
			}
			g.setColor(Color. BLACK);
			g.drawString(provinceNames[k], 375, 20+125*k);
			//draw bar charts for party seats in each province
			max = 1;
			for(int j = 0; j < numParties; j++){
				max = Math.max(max,provincialSeats[k][j]);
			}
			for(int j = 0; j < numParties; j++){
				g.setColor(Color.BLACK);
				g.drawString("" + provincialSeats[k][j],480+200*provincialSeats[k][j]/max,34+125*k+10*j);
				g.setColor(partyColors[j]);
				g.fillRect(475, 25+125*k+10*j, Math.max(200*provincialSeats[k][j]/max,1), 10);
			}
			
			//display detailed stats by region
			g.setColor(Color.BLACK);
			for(int j = 0; j < numParties; j++){
				g.drawString("" + partyNames[j].substring(0,3).toUpperCase(),900+60*j,15);
			}
			int n = 0;
			for(int i = 0; i < numRegions; i++){
				if(provinces[i] == k){
					g.drawString(regionNames[i],700,40+125*k+20*n);
					g.drawString(Long.toString(regionPopulations[i]),800,40+125*k+20*n);
					for(int j = 0; j < numParties; j++){
						g.drawString("" + result[i][j],900+60*j,40+125*k+20*n);
						g.drawString(Math.round(1000.0 * (double)votes[i][j] / (double)regionPopulations[i])/10.0 + "%",920+60*j,40+125*k+20*n);
					}
					n++;
				}
			}
		}
	}
}
