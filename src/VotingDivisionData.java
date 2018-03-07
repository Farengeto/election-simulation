import java.util.HashMap;
import java.util.Map;


public class VotingDivisionData {
	protected Map<String, Long> votes;
	protected Map<String, Integer> seats;
	
	public VotingDivisionData(){
		votes = new HashMap<>();
		seats = new HashMap<>();
	}
	
	//Checks if division has a complete data record for a party
	public boolean hasData(String party){
		return (votes.containsKey(party) && seats.containsKey(party));
	}
	
	/**
	 * Clears election data
	 */
	public void reset(){
		votes.clear();
		seats.clear();
	}
	
	public long getVotes(String party) {
		Long toReturn = votes.get(party);
		if(toReturn == null)
			toReturn = 0L;
		return toReturn;
	}
	
	public int getSeats(String party) {
		Integer toReturn = seats.get(party);
		if(toReturn == null)
			toReturn = 0;
		return toReturn;
	}
	
	public void setVotes(Map<String, Long> votes) {
		this.votes = votes;
	}
	
	public void setSeats(Map<String, Integer> seats) {
		this.seats = seats;
	}
}
