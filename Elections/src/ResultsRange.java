import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.swing.JFrame;

//Runs the election simulator repeatedly and outputs the estimated election results to 2 standard deviations
public class ResultsRange {
	public int tests = 0;
	public int maxTests = 50;
	public int parties = 9;
	public double[] max       = new double[parties];
	public double[] high      = new double[parties];
	public double[] average   = new double[parties];
	public double[] low       = new double[parties];
	public double[] min       = new double[parties];
	public int[] lastTest  = new int[9];
	public int[][] results = new int[maxTests][parties];
	public double[] vmax       = new double[parties];
	public double[] vhigh      = new double[parties];
	public double[] vaverage   = new double[parties];
	public double[] vlow       = new double[parties];
	public double[] vmin       = new double[parties];
	public long[][] vresults = new long[maxTests][parties];
	public UpdatedVoting polls;
	//election results output
	public static String pollsOutName = "pollsOut.txt";
	public static File pollsOut = new File(pollsOutName);
	
	public ResultsRange(){
		polls = new UpdatedVoting();
		parties = polls.getPartyNum();
		reset();
	}
	
	public static void main(String [] args) throws InterruptedException{
		ResultsRange sample = new ResultsRange();
		sample.calcCampaign();
		sample.singleElection();
		sample.reset();
	}
	
	public void calcRanges(){
		for(tests = 0; tests < maxTests; tests++){
			polls = new UpdatedVoting();
			polls.update(0.02);
			polls.voting();
			results[tests] = polls.getSeats();
			vresults[tests] = polls.getVotes();
			//printElection();
		}
		resultsOut();
	}
	
	public void calcCampaign(){
		polls.voting();
		results[0] = polls.getSeats();
		vresults[0] = polls.getVotes();
		//printElection();
		for(tests = 1; tests < maxTests; tests++){
			polls.update(0.02);
			polls.voting();
			results[tests] = polls.getSeats();
			vresults[tests] = polls.getVotes();
			//printElection();
		}
		resultsOut();
	}
	
	public void singleElection(){
		JFrame frame = new JFrame("Elections");
		frame.add(polls);
		frame.setSize(1500, 900);
		frame.setVisible(true);
		polls.update(0.02);
		polls.voting();
		polls.repaint();
	}
	
	public void reset(){
		max       = new double[parties];
		high      = new double[parties];
		average   = new double[parties];
		low       = new double[parties];
		min       = new double[parties];
		results = new int[maxTests][parties];
		vmax       = new double[parties];
		vhigh      = new double[parties];
		vaverage   = new double[parties];
		vlow       = new double[parties];
		vmin       = new double[parties];
		vresults = new long[maxTests][parties];
		tests = 0;
	}
	
	public void ranges(){
		for(int p = 0; p < parties; p++){
			for(int t = 0; t < tests; t++){
				average[p] += (double)results[t][p];
				vaverage[p] += (double)vresults[t][p];
			}
			average[p] = average[p]/(double)tests;
			vaverage[p] = vaverage[p]/(double)tests;
		}
		for(int p = 0; p < parties; p++){
			double dev = 0;
			double vdev = 0;
			for(int t = 0; t < tests; t++){
				dev  += Math.pow(results[t][p]-average[p], 2);
				vdev += Math.pow(vresults[t][p]-vaverage[p], 2);
			}
			dev = Math.sqrt(dev/(double)tests);
			max[p]     = Math.max(0,Math.round(average[p] + 2*dev));
			high[p]    = Math.max(0,Math.round(average[p] + dev));
			low[p]     = Math.max(0,Math.round(average[p] - dev));
			min[p]     = Math.max(0,Math.round(average[p] - 2*dev));
			average[p] = Math.max(0,Math.round(average[p]));
			vdev = Math.sqrt(vdev/(double)tests);
			vmax[p]     = Math.max(0,Math.round(vaverage[p] + 2*vdev));
			vhigh[p]    = Math.max(0,Math.round(vaverage[p] + vdev));
			vlow[p]     = Math.max(0,Math.round(vaverage[p] - vdev));
			vmin[p]     = Math.max(0,Math.round(vaverage[p] - 2*vdev));
			vaverage[p] = Math.max(0,Math.round(vaverage[p]));
		}
	}
	
	public void printResults(){
		//print seat counts
		for(int p = 0; p < 9; p++){
			System.out.println(String.format("%1$3s",(int)min[p]) + "/" + String.format("%1$3s",(int)low[p]) + "/" + String.format("%1$3s",(int)average[p]) + "/" + String.format("%1$3s",(int)high[p]) + "/" + String.format("%1$3s",(int)max[p]));
		}
		//print total votes
		for(int p = 0; p < 9; p++){
			System.out.println(String.format("%1$8s",(int)vmin[p]) + "/" + String.format("%1$8s",(int)vlow[p]) + "/" + String.format("%1$8s",(int)vaverage[p]) + "/" + String.format("%1$8s",(int)vhigh[p]) + "/" + String.format("%1$8s",(int)vmax[p]));
		}
	}
	
	public void printElection(){
		System.out.print("Election #" + (tests+1));
		for(int i = 0; i < 9; i++){
			System.out.print(" - " + results[tests][i]);
			//System.out.print(" - " + vresults[tests][i]);
		}
		System.out.println();
	}
	
	public void resultsOut(){
		try{
			String s1 = "Polls Output.txt";
			String s2 = "Votes Output.txt";
		    PrintWriter writer = new PrintWriter(s1, "UTF-8");
		    PrintWriter writer2 = new PrintWriter(s2, "UTF-8");
		    for(int p = 0; p < polls.getPartyNum(); p++){
		    	writer.print('\t' + polls.getPartyName(p).substring(0,3).toUpperCase());
		    	writer2.print('\t' + polls.getPartyName(p).substring(0,3).toUpperCase());
		    }
		    writer.println();
		    writer2.println();
		    for(int t = 0; t < maxTests; t++){
		    	writer.print("E" + String.format("%05d",(t+1)));
		    	writer2.print("E" + String.format("%05d",(t+1)));
		    	for(int p = 0; p < polls.getPartyNum(); p++){
			    	writer.print('\t' + Integer.toString(results[t][p]));
			    	writer2.print('\t' + Long.toString(vresults[t][p]));
			    }
		    	writer.println();
		    	writer2.println();
		    }
		    writer.close();
		    writer2.close();
		} catch (IOException e) {};
	}
}
