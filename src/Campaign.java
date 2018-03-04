import java.io.*;

//simulates an N-day campaign period with a final election result
public class Campaign extends Results{
	public int length;
	
	//run campaign using a certain number of iterations and the input filename
	public Campaign(int length){
		this(length,"ElectionsIn.txt");
	}
	
	public Campaign(int length,String fileName){
		super(fileName);
		this.length = length;
	}
	
	public static void main(String [] args) throws InterruptedException{
		Campaign sample = new Campaign(50);
		sample.calculate();
		sample.polls.setVisible(true);
		//sample.printResults();
		sample.resultsOut();
	}
	
	public void calculate(){
		//runs a first simulation on base data
		//then iterates through the rest of the campaign with a randomized shift
		for(int i = 0; i < length; i++){
			polls.results(VotingType.PR_HARE);
			addElection(polls);
			polls.update(0.02);
			polls.reset();
		}
		//run a final election
		polls.results(VotingType.PR_HARE);
	}
	
	//print election results to console
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
		resultsOut("Campaign - Polls Output.txt","Campaign - Votes Output.txt");
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