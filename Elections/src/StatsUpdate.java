import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.io.PrintWriter;

import javax.swing.JFrame;
import javax.swing.JPanel;


//uses the text input for a base data, then "updates" it for a new election.
public class StatsUpdate extends JPanel {
	public static boolean createFile = true;
	public static String fileInName = "ElectionsIn.txt";
	public static File fileIn = new File(fileInName);
	public static String fileOut = "ElectionsOut.txt";
	public Voting votes = new Voting(true);
	private int[] regions = new int[25];
	private int numRegions = 6;
	private long[] regionPop = new long[numRegions];
	//private int[] prev = {40,132,48,2,3,6};
	//private int prevMax = 132;
	//private int[][] prevRegion = {{8,47,14,0,0,2},{20,12,2,0,0,1},{5,29,5,2,0,1},{4,14,24,0,0,2},{3,29,2,0,0,0},{0,1,1,0,3,0}};
	//private int prevSum = 231;
	//private int[] prevRegionSum = {71,35,42,44,34,5};
	private double[] partyPopularity = new double[numRegions];
	private String[] regionNames = new String[numRegions];
	
	public StatsUpdate(){
		votes = importStats();
		votes = updateStats(votes);
		//}
		/*for(int i = 0; i < votes.region; i++){
			regionPop[regions[i]] += votes.regions[i];
		}*/
	}
	
	public static void main(String[] args) throws InterruptedException {
		JFrame frame = new JFrame("Elections");
		StatsUpdate voting = new StatsUpdate();
		frame.add(voting);
		frame.setSize(1500, 900);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		/*while(voting.votes.count < voting.votes.population){
			Random rand = new Random();
			//calculate votes
			int v = 100000;
			for(int i = 0; i < v && voting.votes.count < voting.votes.population; i++){
				voting.votes.vote(rand.nextDouble());
			}
			voting.votes.result();
			voting.repaint();
			Thread.sleep(10);
		}*/
		voting.election();
		voting.repaint();
	}
	
	public void election(){
		for(int r = 0; r < votes.region; r++){
			for(int p = 0; p < votes.party-1; p++){
				votes.votes[r][p] = (int)Math.round(votes.regions[r] * votes.parties[r][p]);
				votes.polls[r] += votes.votes[r][p];
			}
			votes.votes[r][votes.party-1] = (int)votes.regions[r] - votes.polls[r];
			votes.polls[r] = (int)votes.regions[r];
			votes.count += votes.polls[r];
		}
		
		//Random rand = new Random();
		//while(votes.count < votes.population){
		//	votes.vote(rand.nextDouble());
		//}
		votes.result();
	}
	
	public Voting importStats(){
		Voting vote  = new Voting(true);
		try{
			Scanner sc = new Scanner(fileIn);
			sc.next();
			numRegions = sc.nextInt();
			sc.next();
			vote.region = sc.nextInt();
			vote.rNames = new String[vote.region];
			vote.seats = new int[vote.region]; 
			vote.regions = new long[vote.region];
			vote.polls = new int[vote.region];
			vote.done = new boolean[vote.region];
			regions = new int[vote.region];
			regionPop = new long[numRegions];
			//prevRegionSum = new int[numRegions];
			regionNames = new String[numRegions];
			//System.out.println(numRegions + " - " + vote.region);
			sc.nextLine();
			/*s = sc.next();
			for(int i = 0; i < vote.region; i++){
				vote.rNames[i] = s;
				System.out.println(i + " " + s);
				s = sc.next();
			}*/
			int k = 0;
			for(int i = 0; i < numRegions; i++){
				Scanner s2 = new Scanner(sc.nextLine());
				regionNames[i] = s2.next().trim();
				regionNames[i] = regionNames[i].substring(0, regionNames[i].length()-1);
				//System.out.println(regionNames[i]);
				while(s2.hasNext()){
					vote.rNames[k] = s2.next().trim();
					//vote.rNames[k] = vote.rNames[k].substring(0, vote.rNames[k].length()-1);
					regions[k] = i;
					//System.out.println(k + " " + regions[k] + " " + vote.rNames[k]);
					k++;
				}
				s2.close();
			}
			sc.nextLine();
			sc.nextLine();
			//System.out.println(sc.nextLine());
			//System.out.println(sc.nextLine());
			vote.population = 0;
			for(int i = 0; i < vote.region; i++){
				vote.regions[i] = sc.nextLong();
				vote.population += vote.regions[i];
				//System.out.print( i + "-" + vote.regions[i] + " ");
			}
			sc.nextLine();
			sc.nextLine();
			sc.nextLine();
			//System.out.println(sc.nextLine());
			//System.out.println(sc.nextLine());
			//System.out.println(sc.nextLine());
			vote.reps = 0;
			for(int i = 0; i < vote.region; i++){
				vote.seats[i] = sc.nextInt();
				vote.reps += vote.seats[i];
				//System.out.print( i + "-" + vote.seats[i] + " ");
			}
			sc.nextLine();
			sc.nextLine();
			//System.out.println(sc.nextLine());
			//System.out.println(sc.nextLine());
			sc.next();
			vote.party = sc.nextInt();
			partyPopularity = new double[vote.party];
			//System.out.println(vote.party);
			vote.parties = new double[vote.region][vote.party];
			vote.votes = new int[vote.region][vote.party];
			vote.result = new int[vote.region][vote.party];
			vote.gov = new int[vote.party];
			vote.names = new String[vote.party];
			vote.colors = new Color[vote.party];
			sc.nextLine();
			for(int i = 0; i < vote.party; i++){
				vote.names[i] = sc.nextLine();
				vote.names[i] = vote.names[i].trim();
				//System.out.print(vote.names[i] + " - ");
				int r = sc.nextInt();
				int g = sc.nextInt();
				int b = sc.nextInt();
				//System.out.print(r + "/" + g + "/" + b + " - ");
				if(i < vote.party-1){
					partyPopularity[i] = Double.parseDouble(vote.names[i].substring(vote.names[i].indexOf("Approval:")+10,vote.names[i].indexOf('%')))/100.0;
				}
				else{
					vote.names[i] = vote.names[i].substring(0,vote.names[i].indexOf(" - "));
					partyPopularity[i] = 0.5;
				}
				vote.colors[i] = new Color(r,g,b);
				for(int j = 0; j < vote.region; j++){
					//add variability to party support
					vote.parties[j][i] = sc.nextDouble();
					//vote.parties[j][i] = sc.nextDouble() * (0.8 + Math.random()*0.4);
					//System.out.print(vote.parties[j][i] + " ");
				}
				//System.out.println(sc.nextLine() + " -");
				sc.nextLine();
			}
			/*while(sc.hasNextLine()){
				System.out.println(sc.nextLine());
			}*/
			sc.close();
		}catch(IOException ex){}
		
		for(int i = 0; i < vote.region; i++){
			vote.polls[i] = 0;
			vote.done[i] = false;
			double count = 0;
			for(int j = 0; j < vote.party; j++){
				vote.votes[i][j] = 0;
				vote.result[i][j] = 0;
				vote.gov[j] = 0;
				count += vote.parties[i][j];
			}
			for(int j = 0; j < vote.party; j++){
				vote.parties[i][j] = vote.parties[i][j]/count;
			}
		}
		return vote;
	}
	
	public Voting updateStats(Voting vote){
		Random random = new Random();
		vote.population = 0;
		for(int i = 0; i < numRegions; i++){
			regionPop[i] = 0;
		}
		for(int i = 0; i < vote.region; i++){
			vote.population += vote.regions[i];
			regionPop[regions[i]] += vote.regions[i];
			vote.seats[i] = Math.max((int)Math.round((double)vote.regions[i]/100000.0),1);
			double count = 0;
			for(int j = 0; j < vote.party; j++){
				if(vote.parties[i][j] != 0){
					vote.parties[i][j] = Math.max(Math.abs(random.nextGaussian()*0.05) + (vote.parties[i][j]*(1 + random.nextGaussian()*0.25)*Math.pow(partyPopularity[j]*2,2)),0.001);
				}
				//if(i <= 20 && j == vote.party-2){
				if(!regionNames[regions[i]].contains("R/") && vote.names[j].contains("R/")){
					vote.parties[i][j] = 0;
				}
				else if(j == vote.party-1){
					vote.parties[i][j] *= 0.33;
				}
				count += vote.parties[i][j];
			}
			for(int j = 0; j < vote.party; j++){
				vote.parties[i][j] = vote.parties[i][j]/count;
			}

		}
		for(int i = 0; i < vote.party; i++){
			if(vote.names[i].contains("R/")){
				votes.names[i] = votes.names[i].substring(2,votes.names[i].length());
			}
		}
		for(int i = 0; i < numRegions; i++){
			if(regionNames[i].contains("R/")){
				regionNames[i] = regionNames[i].substring(2,regionNames[i].length());
			}
		}
		return vote;
	}
	
	
	public int[] exportPolls(){
		int[] polls = new int[votes.party];
		for(int p = 0; p < votes.party; p++){
			polls[p] = 0;
			for(int r = 0; r < votes.region; r++){
				polls[p] += votes.votes[r][p];
			}
		}
		return polls;
	}
	
	public int[] exportSeats(){
		return this.votes.gov;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawString("" + (int)votes.count,25,50+35*votes.party);
		int sum = 0;
		for(int i = 0; i < votes.party; i++){
			sum += votes.gov[i];
		}
		g.drawString("" + (sum),25,70+35*votes.party);
		/*for(int i = 0; i < votes.region; i++){
			g.drawString(votes.polls[i] +"/"+ votes.regions[i],25,530+15*i);
		}*/
		int max = 1;
		for(int i = 0; i < votes.party; i++){
			max = Math.max(max,votes.gov[i]);
		}
		//max = Math.max(max,prevMax);
		for(int i = 0; i < votes.party; i++){
			g.setColor(Color.BLACK);
			g.drawString("" + votes.names[i],25,25+i*35);
			/*for(int j = 0; j < votes.region; j++){
				g.drawString("" + votes.result[j][i],25+j*25,40+i*35);
			}*/
			//if(votes.gov[i]>0){
				g.drawString("" + votes.gov[i],240*votes.gov[i]/max+30,39+i*35);
				g.setColor(votes.colors[i]);
				g.fillRect(25, 30+i*35, Math.max(240*votes.gov[i]/max,1), 10);
				//g.fillRect(25, 40+i*35, 200*prev[i]/max, 2);
				//g.setColor(Color.BLACK);
				//g.drawLine(25, 40+i*35, 25+200*prev[i]/231, 40+i*35);
			//}
		}
		double total = 0.0D;
		Slice[] slices = new Slice[votes.party];
		for (int i = 0; i < slices.length; i++) {
			slices[i] = new Slice(votes.gov[i],votes.colors[i]);
			total += slices[i].value;
		}
		total = Math.max(total, votes.reps);
		g.setColor(Color.GRAY);
		g.fillOval(50,40+35*votes.party,200,200);
		double curValue = 0.0D;
		int startAngle = 0;
		for (int i = 0; i < slices.length; i++) {
			startAngle = (int) Math.round(curValue * 360 / total);
			int arcAngle = (int) Math.round(slices[i].value * 360 / total);
			g.setColor(slices[i].color);
			g.fillArc(50, 40+35*votes.party, 200, 200, 
					startAngle, arcAngle);
			curValue += slices[i].value;
		}
		total = 0.0D;
		slices = new Slice[votes.party];
		for (int i = 0; i < slices.length; i++) {
			int totalVotes = 0;
			for(int j = 0; j < votes.region; j++){
				totalVotes += votes.votes[j][i];
			}
			slices[i] = new Slice(totalVotes,votes.colors[i]);
			total += slices[i].value;
		}
		total = votes.count;
		g.setColor(Color.GRAY);
		g.fillOval(50,290+35*votes.party,200,200);
		curValue = 0.0D;
		startAngle = 0;
		for (int i = 0; i < slices.length; i++) {
			startAngle = (int) Math.round(curValue * 360 / total);
			int arcAngle = (int) Math.round(slices[i].value * 360 / total);
			g.setColor(slices[i].color);
			g.fillArc(50, 290+35*votes.party, 200, 200, 
					startAngle, arcAngle);
			curValue += slices[i].value;
		}
		g.drawLine(300, 0, 300, 900);
		
		int[][] regionVotes = new int[numRegions][votes.party];
		int[][] regionSeats = new int[numRegions][votes.party];
		//int[] regionTotal = new int[numRegions];
		for(int i = 0; i < votes.region; i++){
			for(int j = 0; j < votes.party; j++){
				regionVotes[regions[i]][j] += votes.votes[i][j];
				regionSeats[regions[i]][j] += votes.result[i][j];
				//regionTotal[regions[i]] += votes.votes[i][j];
			}
		}
		for(int k = 0; k < numRegions; k++){
			total = 0.0D;
			slices = new Slice[votes.party];
			for (int i = 0; i < slices.length; i++) {
				slices[i] = new Slice(regionVotes[k][i],votes.colors[i]);
				total += slices[i].value;
			}
			total = regionPop[k];
			g.setColor(Color.GRAY);
			g.fillOval(350,25+125*k,100,100);
			curValue = 0.0D;
			startAngle = 0;
			for (int i = 0; i < slices.length; i++) {
				startAngle = (int) Math.round(curValue * 360 / total);
				int arcAngle = (int) Math.round(slices[i].value * 360 / total);
				g.setColor(slices[i].color);
				g.fillArc(350,25+125*k, 100, 100, 
						startAngle, arcAngle);
				curValue += slices[i].value;
			}
			g.setColor(Color. BLACK);
			g.drawString(regionNames[k], 375, 20+125*k);
			max = 1;
			for(int j = 0; j < votes.party; j++){
				max = Math.max(max,regionSeats[k][j]);
			}
			for(int j = 0; j < votes.party; j++){
				g.setColor(Color.BLACK);
				g.drawString("" + regionSeats[k][j],480+200*regionSeats[k][j]/max,34+125*k+10*j);
				g.setColor(votes.colors[j]);
				g.fillRect(475, 25+125*k+10*j, Math.max(200*regionSeats[k][j]/max,1), 10);
			}
			g.setColor(Color.BLACK);
			for(int j = 0; j < votes.party; j++){
				g.drawString("" + votes.names[j].substring(0,3).toUpperCase(),900+60*j,15);
			}
			int n = 0;
			for(int i = 0; i < votes.region; i++){
				if(regions[i] == k){
					g.drawString(votes.rNames[i],700,40+125*k+20*n);
					g.drawString(Long.toString(votes.regions[i]),800,40+125*k+20*n);
					for(int j = 0; j < votes.party; j++){
						g.drawString("" + votes.result[i][j],900+60*j,40+125*k+20*n);
						g.drawString(Math.round(1000.0 * (double)votes.votes[i][j] / (double)votes.polls[i])/10.0 + "%",920+60*j,40+125*k+20*n);
					}
					n++;
				}
			}
		}
		
	}
}
