import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.*;

import javax.swing.JFrame;


//simulates an N-day campaign period with a final election result
public class Campaign extends Results{
	public int length;
	
	public Campaign(int length){
		this(length,"ElectionsIn.txt");
	}
	
	public Campaign(int length,String fileName){
		super(fileName);
		this.length = length;
	}
	
	public static void main(String [] args) throws InterruptedException{
		Results sample = new Campaign(50);
		sample.calculate();
		JFrame frame = new JFrame("Elections");
		frame.add(sample.polls);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int num = sample.polls.getParties().size();
		int width = Math.min((int)screenSize.getWidth(),980+60*num);
		int height = Math.min((int)screenSize.getHeight(),540+35*num);
		frame.setSize(width,height);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sample.printResults();
	}
	
	public void calculate(){
		//runs a first simulation on base data
		//then iterates through the rest of the campaign with a randomized shift
		for(int i = 0; i < length; i++){
			polls.results();
			addElection(polls);
			polls.update(0.02);
			polls.reset();
		}
		//run a final election
		polls.results();
		//System.out.println(votes);
		//System.out.println(seats);
	}
	
	public void printResults(){
	    for(String party : seats.keySet()){
	    	System.out.print('\t' + party.substring(0,3).toUpperCase());
	    }
	    System.out.println();
	    for(int t = 0; t < length; t++){
	    	System.out.print("D" + String.format("%05d",(t+1)));
	    	for(String party : seats.keySet()){
	    		System.out.print('\t' + Integer.toString(seats.get(party).get(t)));
		    }
	    	System.out.println();
	    }
	    System.out.println();
	    for(String party : votes.keySet()){
	    	System.out.print('\t' + party.substring(0,3).toUpperCase());
	    }
	    System.out.println();
	    for(int t = 0; t < length; t++){
	    	System.out.print("D" + String.format("%05d",(t+1)));
	    	for(String party : votes.keySet()){
	    		System.out.print('\t' + Long.toString(votes.get(party).get(t)));
		    }
	    	System.out.println();
	    }
	}
	
	//convert the results into a txt file
	public void resultsOut(){
		resultsOut("Polls Output.txt","Votes Output.txt");
	}
		
	public void resultsOut(String p, String v){
		try{
		    PrintWriter writer = new PrintWriter(p, "UTF-8");
		    PrintWriter writer2 = new PrintWriter(v, "UTF-8");
		    for(String party : votes.keySet()){
		    	writer.print('\t' + party.substring(0,3).toUpperCase());
		    	writer2.print('\t' + party.substring(0,3).toUpperCase());
		    }
		    writer.println();
		    writer2.println();
		    for(int t = 0; t < length; t++){
		    	writer.print("D" + String.format("%05d",(t+1)));
		    	writer2.print("D" + String.format("%05d",(t+1)));
		    	for(String party : votes.keySet()){
			    	writer.print('\t' + Integer.toString(seats.get(party).get(t)));
			    	writer2.print('\t' + Long.toString(votes.get(party).get(t)));
			    }
		    	writer.println();
		    	writer2.println();
		    }
		    writer.close();
		    writer2.close();
		} catch (IOException e) {};
	}
}