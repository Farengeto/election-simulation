import java.util.Comparator;

//Comparator for parties on divisional level
//Sort by seat count, then by vote count
public class DivisionalComparator implements Comparator<Party>{
	private Division div;
	
	public DivisionalComparator(Division d){
		div = d;
	}
	
	public int compare(Party p1, Party p2){
		//find difference in seat counts
    	int diff = div.getResults().get(p2) - div.getResults().get(p1);
    	//if seat count is equal, sort by votes
    	if(diff == 0){
    		return (int)Math.signum(div.getVotes().get(p2) - div.getVotes().get(p1));
    	}
    	//else sort by seat count
    	else{
    		return diff;
    	}
	}
}