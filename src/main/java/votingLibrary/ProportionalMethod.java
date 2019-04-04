package votingLibrary;

/**
 * An interface used to get the values used in a proportional allocation method
 * @author Travis
 *
 */
public interface ProportionalMethod{
	public static final ProportionalMethod hareMethod = (votes, seats) -> votes/seats;
	public static final ProportionalMethod droopMethod = (votes, seats) -> (votes/(seats + 1)) + 1;
	public static final ProportionalMethod dHondtMethod = (votes, seats) -> votes/(seats + 1);
	public static final ProportionalMethod sainteLagueMethod = (votes, seats) -> votes/((2 * seats) + 1);
	
	public long getValue(long votes, int seats);
}
