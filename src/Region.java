import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
/*
 * Second level subdivision of UpdatedVoting simulation
 * Defines the Region's Province and is Party support levels for each Party
 */
public class Region extends Division{
	private Province province;
	private Map<Party,Double> support;
	
	//create blank region
	public Region(){
		this(new Province());
	}
	
	//create blank region within a province
	public Region(Province province){
		this("",0,0,province);
	}
	
	//create region with demographics in a province
	public Region(String name, long pop, int seat,Province province){
		this("",0,0,province,new HashMap<Party,Double>());
	}
	
	//create a region with demographics and party support levels in a province
	public Region(String name, long pop, int seat,Province province, Map<Party,Double> support){
		super(name,pop,seat);
		this.province = province;
		this.province.addRegion(this);
		//this.province.changePopulation(population);
		this.support = support;
		this.rebalance();
	}
	
	//change regional population by an amount
	public void changePopulation(long popChange){
		//prevent negative population
		popChange = Math.max(popChange, -population);
		super.changePopulation(popChange);
		//update population of the province to reflect change
		province.changePopulation(popChange);
	}
	
	//swap Province to new one
	public void changeProvince(Province newProvince){
		province.removeRegion(this);
		province = newProvince;
		province.addRegion(this);
	}
	
	public Province getProvince(){
		return province;
	}
	
	public double getSupport(Party p){
		if(support.get(p) == null){
			return 0;
		}
		return support.get(p);
	}
	
	//updated the support values into corrected decimal percentages
	public void rebalance(){
		double count = 0;
		Set<Party> parties = support.keySet();
		for(Party p : parties){
			count += support.get(p);
		}
		for(Party p : parties){
			double d = support.get(p) / count;
			support.put(p,d);
		}
	}
	
	public void results(){
		reset();
		long count = population; //holds unallocated votes, if negative votes are overallocated
		Set<Party> parties = support.keySet();
		//calculate votes
		for(Party p : parties){
			long vote = (long)Math.round(support.get(p) * population);
			count -= vote;
			votes.put(p,vote);
		}
		//remove excess votes or add missing votes
		while(count != 0)
		for(Party p : parties){
			if(count > 0){
				votes.put(p,votes.get(p)+1);
				count--;
			}
			if(count < 0){
				votes.put(p,votes.get(p)-1);
				count++;
			}
		}
		
		//Define quote for the largest remainder method
		//Uses Hare quota, biased towards smaller parties
		long quota = population/seats;
		//Allocate seats according to the quota
		int remainingSeats = seats;
		Map<Party,Long> remainder = new HashMap<>();
		for(Party p : parties){
			results.put(p , (int)(votes.get(p) / quota));
			remainingSeats -= results.get(p);
			remainder.put(p, votes.get(p) % quota);
		}
		//allocate remaining seats using highest remainder method
		while(remainingSeats > 0){
			Party max = null;
			//find party with largest remainder
			for(Party p : parties){
				if(max != null){
					if(remainder.get(p) > remainder.get(max)){
						max = p;
					}
					//if remainder is equal, assign to party with highest seat count first
					else if(remainder.get(p) == remainder.get(max) && results.get(p) > results.get(max)){
						max = p;
					}
				}
				else{
					max = p;
				}
			}
			//give party with largest remainder an additional seat
			//then set parties' remainder to zero
			results.put(max, results.get(max)+1);
			remainder.put(max, 0L);
			remainingSeats--;
		}
	}
	
	//updates support with new levels, based on shift margin and shifts of higher level divisions
	public void update(double shiftMargin,Map<Party,Double> natShift,Map<Party,Double> proShift){
		Random random = new Random();
		Map<Party,Double> regShift = new HashMap<>();
		Set<Party> parties = natShift.keySet();
		for(Party p : parties){
			regShift.put(p, Math.max(1.0 + random.nextGaussian()*shiftMargin,0.0));
			//ensure party actually exists in region, then update
			if(support.get(p) != null){
				double updated = support.get(p) * natShift.get(p) * proShift.get(p) * regShift.get(p);
				support.put(p, updated);
			}
		}
		rebalance();
	}
	
	public void checkValues(){
		int sum = 0;
		long pops = 0;
		for(Party p : support.keySet()){
			sum += results.get(p);
			pops += votes.get(p);
		}
		System.out.println(sum + "/" + seats + " - " + pops + "/" + population);
		if(sum != seats || pops != population){
			double s = 0;
			for(Party p : support.keySet()){
				s += support.get(p);
			}
			System.out.println(s + " " + support);
			System.out.println(results);
			System.out.println(votes);
		}
	}
	
	public String toString(){
		return super.toString() + " - " + support.toString();
	}
}