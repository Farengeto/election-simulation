import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

//generic political division superclass
public abstract class Division{
	protected long population;
	protected int seats;
	protected String name;
	protected Map<Party,Long> votes;
	protected Map<Party,Integer> results;
	
	
	public Division(){
		this("",0,0);
	}
	
	public Division(String name, long pop, int seat){
		this.name = name;
		population = pop;
		seats = seat;
		votes = new HashMap<>();
		results = new HashMap<>();
	}
	
	//change the population of the division by an amount
	//ensure division does not have negative population
	//division with zero population allowed
	public void changePopulation(long popChange){
		if(popChange+population < 0){
			population = 0;
		}
		else{
			population += popChange;
		}
	}
	
	//change the seats in a division by an amount
	//ensure division does not have negative seats
	//division with zero seats allowed
	public void changeSeats(int seatChange){
		if(seatChange+seats < 0){
			seats = 0;
		}
		else{
			seats += seatChange;
		}
	}
	
	public String getName(){
		return name;
	}
	
	public long getPopulation(){
		return population;
	}
	
	public int getSeats(){
		return seats;
	}
	
	//get list of parties with voting results
	public Set<Party> getParties(){
		return votes.keySet();
	}
	
	public Map<Party,Long> getVotes(){
		return votes;
	}
	
	public Map<Party,Integer> getResults(){
		return results;
	}
	
	public String toString(){
		return name + ": Population " + population + ", " + seats + " seats - " + results.toString();
	}
	
	//return a sorted list of parties in the region by seats and votes
	public List<Party> partySort(){
		List<Party> parties = new ArrayList<>(results.keySet());
		parties.sort(new DivisionalComparator(this));
		return parties;
	}
	
	//abstract method to determine electoral results
	public abstract void results();
	
	//reset election results
	public void reset(){
		votes = new HashMap<>();
		results = new HashMap<>();
	}
}