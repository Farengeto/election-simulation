import java.util.Comparator;

//Comparator for parties on national level
//Sort by seat count, then by vote count
public class NationalComparator implements Comparator<Party>{
	public NationalComparator(){}

    public int compare(Party p1, Party p2){
    	//find difference in seat counts
    	int diff = p2.getSeats() - p1.getSeats();
    	//if seat count is equal, sort by votes
    	if(diff == 0){
    		return (int)Math.signum(p2.getVotes() - p1.getVotes());
    	}
    	//else sort by seat count
    	else{
    		return diff;
    	}
    }
}