The Election Simulator generates a randomized election results.

Elections results are simulated using three levels of political sub-divisions.
These subdivisions allow greater degrees of control over data and results.
Regions are the basic unit of the simulation. Most simulation data is defined at this level.

Division levels:
 * National level is the entirety of the country (i.e. Canada)
 * Provinces represent highest level divisions of country (i.e. the province of Ontario)
 * Regions represent sub-divisions of the provinces (i.e. the Greater Toronto Area, or a single electoral riding)

The simulator uses party-based election systems.
Parties are defined with a name and a colour.
The Party approval value has been deprecated.
A party's support is defined in each region

Voting Methods:
Elections can be run using a variety of electoral systems, each with several variations.
  * First-Past-the-Post (FPTP): FPTP is a winner takes all form of election.
        All seats in the division are allocated to the party with a plurality of votes.
  * Proportional Representation (PR): PR allocates seats proportionally.
        Seats in the division are allocated proportional to the number of votes.
	Remainder seats are allocated according to a given Highest Remainder Method.
  * Mixed-Member Majoritarian (MMM): MMM is a hybrid FPTP/PR method.
        Half of the seats are allocated at regional level using FPTP voting.
	The other half are allocated at the selected division level using PR.
  * Mixed-Member Proportional (MMP): MMP is a hybrid FPTP/PR method.
        Half of the seats are allocated at regional level using FPTP voting.
	The other half are allocated such that the total seat count is proportional to votes.
	Proportional allocation is calculated using a given highest averages/highest quotient method.
	Overhang seats are not implemented at this time.
NOTE: Using MMM or MMP doubles the total seat count entered.

Proportional methods:
When using proportional voting, these are used to allocate any remaining fractional seats
  * Hare Quota: A highest remainder method.
    Uses a quota of (votes) / (seats).
    Favours smaller parties for the final seats.
  * Droop Quota: A highest remainder method.
    Uses a quota of ((votes) / (seats + 1)) + 1.
    Favours larger parties for the final seats.
  * D'Hondt/Jefferson Method: A highest averages method.
    Uses quotient of (votes) / (seats + 1)
  * Webster/Sainte-Laguë Method: A highest quotient method.
    Uses quotient of (votes) / ((2 * seats) + 1)

Simulator modes:
  * Election: Simulates a single election using provided data
        Results are randomized based on the given Margin of Error.
        Results screen provides a graphical breakdown of the results in each level of the simulation.
        If using a Seat Threshold, when using proportional methods parties recieving less than the threshold will not be allocated seats.
  * Campaign: Simulates the results of a projection over an electoral campaign period
        Seat and vote estimates are updated for the new data in each poll.
        Each unit of the campaign will change the projections by a random amount using the margin of error
  * Range Calculator: Simulates projected result ranges using a Monte Carlo simulation
        Runs a set of many elections using the data, and estimates the seat and vote ranges for each party
        Displays the median result, and the computed confidence bounds at 1 sigma (~68%) and 2 sigma (~95%)