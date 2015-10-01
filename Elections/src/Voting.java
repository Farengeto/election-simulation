import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
//import java.io.PrintWriter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.*;

//Implements Slice class obtained from StackExchange

public class Voting extends JPanel{
	  
	public int party = 8;
	public int reps = 1024;
	public int region = 10;
	  
	//population
	//public long population = (long)(8350880961000.0);
	//public long[] regions =  {(long)1016215899203.0, (long)447134995649.0, (long)329199267078.0,  (long)937288278690.0,  (long)375134048531.0,
	//						    (long)1614279462482.0, (long)870294433786.0, (long)1527328302699.0, (long)1199872938897.0, (long)34133333333.0};
	//public int[] seats =   {125,                   55,                   40,                    115,                   46,
	//						  107,                   198,                  187,                   147,                   4    };
	public long population = 10000;
	public long[] regions = new long[region];
	public int[] seats = new int[region]; 
	public String[] rNames = new String[region];
	//party support by region
	public double[][] parties = new double[region][party];
	public int[][] votes = new int[region][party];
	public int[] polls = new int[region];
	public double count = 0;
	public int[][] result = new int[region][party];
	public int[] gov = new int[party];
	public boolean[] done = new boolean[region];
	public String[] names = new String[party];
	public Color[] colors = new Color[party];
	//List of voting statisics
	public static File file = new File("Canada Elections.txt");
	
	public Voting(){
		//read data from text file
		try{
			Scanner sc = new Scanner(file);
			String s = sc.next();
			region = sc.nextInt();
			rNames = new String[region];
			seats = new int[region]; 
			regions = new long[region];
			polls = new int[region];
			done = new boolean[region];
			sc.nextLine();
			s = sc.nextLine();
			for(int i = 0; i < region; i++){
				rNames[i] = s;
				s = sc.nextLine();
			}
			sc.nextLine();
			population = 0;
			for(int i = 0; i < region; i++){
				regions[i] = sc.nextLong();
				population += regions[i];
			}
			sc.nextLine();
			sc.nextLine();
			reps = 0;
			for(int i = 0; i < region; i++){
				seats[i] = sc.nextInt();
				reps += seats[i];
				System.out.print(seats[i] + " ");
			}
			sc.nextLine();
			sc.nextLine();
			sc.next();
			party = sc.nextInt();
			
			parties = new double[region][party];
			votes = new int[region][party];
			result = new int[region][party];
			gov = new int[party];
			names = new String[party];
			colors = new Color[party];
			sc.nextLine();
			for(int i = 0; i < party; i++){
				names[i] = sc.nextLine();
				int r = sc.nextInt();
				int g = sc.nextInt();
				int b = sc.nextInt();
				colors[i] = new Color(r,g,b);
				for(int j = 0; j < region; j++){
					//add variability to party support
					parties[j][i] = sc.nextDouble() * (0.8 + Math.random()*0.4);
				}
				sc.nextLine();
			}
		}catch(IOException ex){}
		
		//initialize empty variables and convert party support into percent
		for(int i = 0; i < region; i++){
			polls[i] = 0;
			done[i] = false;
			double count = 0;
			for(int j = 0; j < party; j++){
				votes[i][j] = 0;
				result[i][j] = 0;
				gov[j] = 0;
				count += parties[i][j];
			}
			for(int j = 0; j < party; j++){
				parties[i][j] = parties[i][j]/count;
			}
		}
	}
	
	//generate an empty voting stats set
	public Voting(boolean usingInput){
		
	}

	public static void main(String [] args) throws InterruptedException {
		JFrame frame = new JFrame("Elections");
		Voting vote = new Voting();
		frame.add(vote);
		frame.setSize(300, 800);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		while(vote.count < vote.population){
			Random rand = new Random();
			//calculate votes
			int v = 100000;
			for(int i = 0; i < v && vote.count < vote.population; i++){
				vote.vote(rand.nextDouble());
			}
			vote.result();
			vote.repaint();
			Thread.sleep(10);
		}
		/*for(int i = 0; i < vote.party; i++){
			for(int j = 0; j < vote.region; j++){
				System.out.print(vote.result[j][i] + " ");
			}
			System.out.println(vote.gov[i]);
		}*/
	}

	public void vote(double v){
		int r;
		do{
			r = (int)(Math.random()*region);
		}while(polls[r] >= regions[r]);
		boolean cast = false;
		for(int i = 0; i < party && !cast; i++){
			if(v < parties[r][i]){
				cast = true;
				votes[r][i]++;
			}
			else{
				v -= parties[r][i];
			}
		}
		//increase vote counter
		polls[r]++;
		count++;
	}
	
	public void result(){
		for(int i = 0; i < region; i++){
			int quota = (int)((regions[i]/(seats[i]+1))+1);
			if(polls[i] >= regions[i]){
				if(!done[i]){
					done[i] = true;
					int[] rem = new int[party];
					int s = 0;
					for(int j = 0; j < party; j++){
						int r = (int)(votes[i][j]/quota);
						result[i][j] = r;
						s += r;
						rem[j] = votes[i][j]%quota;
					}
					while(s < seats[i]){
						int max = 0;
						for(int j = 1; j < party; j++){
							if(rem[j] > rem[max]){
								max = j;
							}
							else if(rem[j] == rem[max] && result[i][j] > result[i][max]){
								max = j;
							}
						}
						result[i][max]++;
						rem[max] = 0;
						s++;
					}
				}
			}
			else{
				for(int j = 0; j < party; j++){
					result[i][j] = (int)(votes[i][j]/quota);
				}
			}
		}
		for(int i = 0; i < party; i++){
			gov[i] = 0;
			for(int j = 0; j < region; j++){
				gov[i] += result[j][i];
			}
		}
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawString("" + count,25,500);
		int sum = 0;
		for(int i = 0; i < party; i++){
			sum += gov[i];
		}
		g.drawString("" + (sum),25,515);
		for(int i = 0; i < region; i++){
			g.drawString(polls[i] +"/"+ regions[i],25,530+15*i);
		}
		int max = 1;
		for(int i = 0; i < party; i++){
			max = Math.max(max,gov[i]);
		}
		for(int i = 0; i < party; i++){
			g.setColor(Color.BLACK);
			g.drawString("" + names[i],25,25+i*35);
			//for(int j = 0; j < region; j++){
			//	g.drawString("" + result[j][i],25+j*25,40+i*35);
			//}
			if(gov[i]>0){
				g.drawString("" + gov[i],200*gov[i]/max+30,40+i*35);
				g.setColor(colors[i]);
				g.fillRect(25, 30+i*35, 200*gov[i]/max, 10);
			}
		}
		double total = 0.0D;
		Slice[] slices = new Slice[party];
		for (int i = 0; i < slices.length; i++) {
			slices[i] = new Slice(gov[i],colors[i]);
			total += slices[i].value;
		}
		total = Math.max(total, reps);
		g.setColor(Color.GRAY);
		g.fillOval(50,300,200,200);
		double curValue = 0.0D;
		int startAngle = 0;
		for (int i = 0; i < slices.length; i++) {
			startAngle = (int) Math.round(curValue * 360 / total);
			int arcAngle = (int) Math.round(slices[i].value * 360 / total);
			g.setColor(slices[i].color);
			g.fillArc(50, 300, 200, 200, 
					startAngle, arcAngle);
			curValue += slices[i].value;
		}
	}
}