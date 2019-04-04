package election;

import election.VotingData;
import nation.Party;
import nation.Region;

import java.util.Comparator;

//Comparator for parties on regional level
//Sort by seat count, then by vote count
public class RegionalComparator implements Comparator<Party> {
	private Region div;
	private VotingData results;

	public RegionalComparator(Region d, VotingData results) {
		div = d;
		this.results = results;
	}

	public int compare(Party p1, Party p2) {
		//find difference in seat counts
		int diff = results.getSeatsRegion(div, p2) - results.getSeatsRegion(div, p1);
		//if seat count is equal, sort by votes
		if (diff == 0) {
			return (int) Math.signum(results.getVotesRegion(div, p2) - results.getVotesRegion(div, p1));
		}
		//else sort by seat count
		else {
			return diff;
		}
	}
}