import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

//abstract class to 
public abstract class Results {
	protected Map<Party,Integer> lastSeats;
	protected Map<Party,Long> lastVotes;
	protected UpdatedVoting polls;
	//protected Map<Party,List<Integer>> seats;
	//protected Map<Party,List<Long>> votes; 
	protected Map<String,List<Integer>> seats;
	protected Map<String,List<Long>> votes; 
	protected String file;
	
	public Results(){
		this("ElectionsIn.txt");
	}
	
	public Results(String fileName){
		lastSeats = new HashMap<>();
		lastVotes = new HashMap<>();
		polls = new UpdatedVoting(fileName);
		seats = new HashMap<>();
		votes = new HashMap<>();
		file = fileName;
	}
	
	public abstract void calculate();
	
	public void addElection(UpdatedVoting result){
		Map<Party,Long> v = result.getVotes();
		Map<Party,Integer> s = result.getResults();
		for(Party pr : v.keySet()){
			String p = pr.getName();
			List<Long> newV;
			if(votes.get(p) != null){
				newV = votes.get(p);
			}
			else{
				newV = new ArrayList<>();
			}
			newV.add(v.get(pr));
			votes.put(p, newV);
			List<Integer> newS;
			if(seats.get(p) != null){
				newS = seats.get(p);
			}
			else{
				newS = new ArrayList<>();
			}
			newS.add(s.get(pr));
			seats.put(p, newS);
		}
	}
	
	public abstract void printResults();
	
	public abstract void resultsOut(String p, String v);
}