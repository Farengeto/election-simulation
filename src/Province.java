import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
/*
 * Second level subdivision of UpdatedVoting simulation
 * Defines the Region's Province and is Party support levels for each Party
 */
public class Province extends Division{
	private List<Region> regions;
	
	//blank province
	public Province(){
		this("");
	}
	
	//province with name only
	public Province(String name){
		this(name,new ArrayList<Region>());
	}
	
	//province with name and list of regions, pop and seat count generated from it
	public Province(String name, List<Region> regions){
		super(name,0,0);
		long pop = 0;
		int seat = 0;
		for(Region region : regions){
			pop += region.getPopulation();
			seat += region.getSeats();
		}
		this.population = pop;
		this.seats = seat;
		this.regions = regions;
	}
	
	//province with name and list of regions, pop and seat count given (beware mathematical errors)
	public Province(String name, long pop, int seat, List<Region> regions){
		super(name,pop,seat);
		this.regions = regions;
	}
	
	public void addRegion(Region r){
		regions.add(r);
		population += r.getPopulation();
		seats += r.getSeats();
	}
	
	public void removeRegion(Region r){
		regions.remove(r);
		population -= r.getPopulation();
		seats -= r.getSeats();
	}
	
	public List<Region> getRegions(){
		return regions;
	}
	
	public void results(){
		for(Party p : votes.keySet()){
			votes.put(p, 0L);
			results.put(p, 0);
		}
		for(Region r : regions){
			//calculate the region's electoral results
			r.results();
			//add each region's votes and seats to the provincial total
			Set<Party> parties = r.getParties();
			for(Party p : parties){
				if(votes.get(p) != null){
					votes.put(p, votes.get(p) + r.getVotes().get(p));
					results.put(p, results.get(p) + r.getResults().get(p));
				}
				else{
					votes.put(p, r.getVotes().get(p));
					results.put(p, r.getResults().get(p));
				}
			}
		}
	}
	
	//updates support with new levels, based on shift margin and shifts of higher level divisions
	public void update(double shiftMargin,Map<Party,Double> natShift){
		Random random = new Random();
		Map<Party,Double> proShift = new HashMap<>();
		Set<Party> parties = natShift.keySet();
		for(Party p : parties){
			proShift.put(p, Math.max(1.0 + random.nextGaussian()*shiftMargin,0.0));
		}
		for(Region r : regions){
			r.update(shiftMargin,natShift,proShift);
		}
	}
	
	public String toString(){
		String s = "Province " + super.toString();
		for(Region r : regions){
			s = s + "\n" + r.toString();
		}
		return s;
	}
}