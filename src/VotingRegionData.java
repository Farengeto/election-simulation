import java.util.HashMap;
import java.util.Map;


public class VotingRegionData {
	private Region region;
	private Map<String, Double> support;
	private Map<String, Long> votes;
	private Map<String, Integer> seats;
	
	public VotingRegionData(Region r){
		region = r;
		support = region.getSupport();
		votes = new HashMap<>();
		seats = new HashMap<>();
	}
	
	public double getSupport(String party) {
		Double toReturn = support.get(party);
		if(toReturn == null)
			toReturn = 0.0;
		return toReturn;
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
	
	public Region getRegion(){
		return region;
	}
	
	public void setSupport(Map<String, Double> support) {
		//rebalance support values
		double sum = 0.0;
		for(String p : support.keySet()){
			sum += support.get(p);
		}
		if(sum != 1.0){
			for(String p : support.keySet()){
				support.put(p, support.get(p) / sum);
			}
		}
		
		this.support = support;
	}
	
	public void setVotes(Map<String, Long> votes) {
		this.votes = votes;
	}
	
	public void setSeats(Map<String, Integer> seats) {
		this.seats = seats;
	}
}
