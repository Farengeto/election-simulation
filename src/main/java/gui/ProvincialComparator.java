package gui;

import election.VotingData;
import nation.Party;
import nation.Province;

import java.util.Comparator;

//Comparator for parties on provincial level
//Sort by seat count, then by vote count
public class ProvincialComparator implements Comparator<Party> {
	private Province div;
	private VotingData results;

	public ProvincialComparator(Province d, VotingData results) {
		div = d;
		this.results = results;
	}

	public int compare(Party p1, Party p2) {
		//find difference in seat counts
		int diff = results.getSeatsProvince(div, p2) - results.getSeatsProvince(div, p1);
		//if seat count is equal, sort by votes
		if (diff == 0) {
			return (int) Math.signum(results.getVotesProvince(div, p2) - results.getVotesProvince(div, p1));
		}
		//else sort by seat count
		else {
			return diff;
		}
	}
}