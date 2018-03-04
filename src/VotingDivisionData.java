import java.util.HashMap;
import java.util.Map;


public class VotingDivisionData {
	private Map<String, Long> votes;
	private Map<String, Integer> seats;
	
	public VotingDivisionData(){
		votes = new HashMap<>();
		seats = new HashMap<>();
	}
	
	//Checks if division has a complete data record for a party
	public boolean hasData(String party){
		return (votes.containsKey(party) && seats.containsKey(party));
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
