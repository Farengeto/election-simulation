# Election Simulator
The Election Simulator generates a randomized election results.

Elections results are simulated using three levels of political sub-divisions.
These subdivisions allow greater degrees of control over data and results.
Regions are the basic unit of the simulation. Most simulation data is defined at this level.

## Division levels:
* National level is the entirety of the country (i.e. Canada)
* Provinces represent highest level divisions of country (i.e. the province of Ontario)
* Regions represent sub-divisions of the provinces, with a voting population and one or more electoral seats (i.e. the Greater Toronto Area)

## Party Simulation
The simulator uses party-based election systems.
Parties are defined with a name and a colour.
A party's support is defined in each region

## Electoral Systems
Elections can be run using a variety of electoral systems, each with several variations.
 * First-Past-the-Post (FPTP): FPTP is a winner takes all form of election. All seats in the division are allocated to the party with a plurality of votes.
 * Proportional Representation (PR): PR allocates seats proportionally. Seats in the division are allocated proportional to the number of votes. Remainder seats are allocated according to a given Highest Remainder Method.
 * Mixed-Member Majoritarian (MMM): MMM is a hybrid FPTP/PR method. Half of the seats are allocated at regional level using FPTP voting. The other half are allocated at the selected division level using PR.
 * Mixed-Member Proportional (MMP): MMP is a hybrid FPTP/PR method. Half of the seats are allocated at regional level using FPTP voting. The other half are allocated such that the total seat count is proportional to votes. Proportional allocation is calculated using a given highest averages/highest quotient method. Overhang seats are not implemented in this release.
NOTE: Using MMM or MMP doubles the total seat count.

## Proportional Remainder Methods
These methods determine how any remaining seats are divided when using proportional methods
 * Hare Quota: A highest remainder method. Uses quota of (votes) / (seats). Favours smaller parties for the final seats.
 * Droop Quota: A highest remainder method. Uses quota of ((votes) / (seats + 1)) + 1. Favours larger parties for the final seats.
 * D'Hondt/Jefferson Method: A highest averages method. Uses quotient of (votes) / (seats + 1)
 * Webster/Sainte-Laguë Method: A highest quotient method. Uses quotient of (votes) / ((2 * seats) + 1)
 
 ## Authors

* **Travis Ridge** - *Initial work* - [Travis Ridge](https://github.com/TravisRidge)
 
 ## License

fastText is MIT-licensed.
