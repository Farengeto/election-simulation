import java.util.Random;

//Runs the election simulator repeatedly and outputs the estimated election results to 2 standard deviations
public class ResultsRange {
	public int tests = 0;
	public int maxTests = 1000;
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
	public int[][] vresults = new int[maxTests][parties];
	
	public ResultsRange(){
	}
	
	public static void main(String [] args){
		ResultsRange sample = new ResultsRange();
		while(sample.tests < sample.maxTests){
			StatsUpdate voting = new StatsUpdate();
			voting.election();
			sample.results[sample.tests] = voting.exportSeats();
			sample.vresults[sample.tests] = voting.exportPolls();
			//sample.printElection();
			sample.tests++;
		}
		sample.ranges();
		sample.printResults();
		sample.reset();
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
		vresults = new int[maxTests][parties];
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
		System.out.print("Election #" + tests);
		for(int i = 0; i < 9; i++){
			System.out.print(" - " + results[tests][i]);
		}
		System.out.println();
	}
}
