import java.util.Map;


public class VotingRegionData extends VotingDivisionData {
	private Region region;
	private Map<String, Double> support;
	
	public VotingRegionData(Region r){
		super();
		region = r;
		support = region.getSupport();
	}
	
	public Region getRegion(){
		return region;
	}
	
	/**
	 * Clears election data including any modified support values
	 */
	public void reset(){
		super.reset();
		support = region.getSupport();
	}
	
	/**
	 * Clears election data while keeping any modified support values
	 */
	public void resetResults(){
		
	}
	
	public double getSupport(String party) {
		Double toReturn = support.get(party);
		if(toReturn == null)
			toReturn = 0.0;
		return toReturn;
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
}
