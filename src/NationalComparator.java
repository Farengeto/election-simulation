import java.util.Comparator;

//Comparator for parties on national level
//Sort by seat count, then by vote count
public class NationalComparator implements Comparator<Party>{
	private VotingData results;
	
	public NationalComparator(VotingData results){
		this.results = results;
	}

    public int compare(Party p1, Party p2){
    	//find difference in seat counts
    	int diff = results.getSeatsNation(p2) - results.getSeatsNation(p1);
    	//if seat count is equal, sort by votes
    	if(diff == 0){
    		return (int)Math.signum(results.getVotesNation(p2) - results.getVotesNation(p1));
    	}
    	//else sort by seat count
    	else{
    		return diff;
    	}
    }
}