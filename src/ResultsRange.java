import java.io.*;
import java.util.*;

//run a set of elections to predict the range of results
//returns the median, 50% and 95% intervals for results
public class ResultsRange extends Results{
	private int tests;
	private Map<String,Integer> sMin;
	private Map<String,Integer> sLow;
	private Map<String,Integer> sAvg;
	private Map<String,Integer> sHig;
	private Map<String,Integer> sMax;
	private Map<String,Long> vMin;
	private Map<String,Long> vLow;
	private Map<String,Long> vAvg;
	private Map<String,Long> vHig;
	private Map<String,Long> vMax;
	
	public ResultsRange(int tests){
		this(tests,"ElectionsIn.txt");
	}
	
	public ResultsRange(int tests,String fileName){
		super(fileName);
		this.tests = tests;
		//initialize the result lists
		sMin = new HashMap<>();
		sLow = new HashMap<>();
		sAvg = new HashMap<>();
		sHig = new HashMap<>();
		sMax = new HashMap<>();
		vMin = new HashMap<>();
		vLow = new HashMap<>();
		vAvg = new HashMap<>();
		vHig = new HashMap<>();
		vMax = new HashMap<>();
	}
	
	public static void main(String [] args) throws InterruptedException{
		ResultsRange sample = new ResultsRange(1000);
		sample.calculate();
		//sample.printResults();
		sample.resultsOut();
	}
	
	public void calculate(){
		//get data set for given number of tests
		for(int t = 0; t < tests; t++){
			//run simulated election after randomized shift
			UpdatedVoting pResult = new UpdatedVoting(file);
			pResult.update(0.10);
			pResult.results();
			//add votes and seats to the list
			addElection(pResult);
		}
		//calculate intervals
		//hard-coded for median, 50% and 95% intervals for now
		for(String p : votes.keySet()){
			List<Integer> s = seats.get(p);
			Collections.sort(s);
			sMin.put(p,(s.get((int)Math.ceil((double)tests*0.025))+s.get((int)Math.floor((double)tests*0.025)))/2);
			sLow.put(p,(s.get((int)Math.ceil((double)tests*0.25)) +s.get((int)Math.floor((double)tests*0.25)) )/2);
			sAvg.put(p,(s.get((int)Math.ceil((double)tests*0.5))  +s.get((int)Math.floor((double)tests*0.5))  )/2);
			sHig.put(p,(s.get((int)Math.ceil((double)tests*0.75)) +s.get((int)Math.floor((double)tests*0.75)) )/2);
			sMax.put(p,(s.get((int)Math.ceil((double)tests*0.975))+s.get((int)Math.floor((double)tests*0.975)))/2);
			List<Long> v = votes.get(p);
			Collections.sort(v);
			vMin.put(p,(v.get((int)Math.ceil((double)tests*0.025))+v.get((int)Math.floor((double)tests*0.025)))/2);
			vLow.put(p,(v.get((int)Math.ceil((double)tests*0.25)) +v.get((int)Math.floor((double)tests*0.25)) )/2);
			vAvg.put(p,(v.get((int)Math.ceil((double)tests*0.5))  +v.get((int)Math.floor((double)tests*0.5))  )/2);
			vHig.put(p,(v.get((int)Math.ceil((double)tests*0.75)) +v.get((int)Math.floor((double)tests*0.75)) )/2);
			vMax.put(p,(v.get((int)Math.ceil((double)tests*0.975))+v.get((int)Math.floor((double)tests*0.975)))/2);
		}
	}
	
	public void printResults(){
		System.out.println("\tMIN\tLOW\tAVG\tHIG\tMAX");
	    for(String party : votes.keySet()){
	    	System.out.println(party.substring(0,3).toUpperCase()
	    			+ '\t' + sMin.get(party) + '\t' + sLow.get(party) + '\t' + sAvg.get(party) + '\t' + sHig.get(party) + '\t' + sMax.get(party));
	    }
	    System.out.println();
	    System.out.println("\tMIN\tLOW\tAVG\tHIG\tMAX");
	    for(String party : votes.keySet()){
	    	System.out.println(party.substring(0,3).toUpperCase()
	    			+ '\t' + vMin.get(party) + '\t' + vLow.get(party) + '\t' + vAvg.get(party) + '\t' + vHig.get(party) + '\t' + vMax.get(party));
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
		    writer.println("\tMIN\tLOW\tAVG\tHIG\tMAX");
		    writer2.println("\tMIN\tLOW\tAVG\tHIG\tMAX");
		    for(String party : votes.keySet()){
		    	writer.println(party.substring(0,3).toUpperCase()
		    			+ '\t' + sMin.get(party) + '\t' + sLow.get(party) + '\t' + sAvg.get(party) + '\t' + sHig.get(party) + '\t' + sMax.get(party));
		    	writer2.println(party.substring(0,3).toUpperCase()
		    			+ '\t' + vMin.get(party) + '\t' + vLow.get(party) + '\t' + vAvg.get(party) + '\t' + vHig.get(party) + '\t' + vMax.get(party));
		    }
		    writer.close();
		    writer2.close();
		} catch (IOException e) {};
	}
}