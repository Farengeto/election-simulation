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
	private Map<String,Double> support;
	
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
		this(name,pop,seat,province,new HashMap<String,Double>());
	}
	
	//create a region with demographics and party support levels in a province
	public Region(String name, long pop, int seat,Province province, Map<String,Double> support){
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
	
	public Map<String,Double> getSupport(){
		return support;
	}
	
	public double getSupport(String p){
		if(support.get(p) == null){
			return 0;
		}
		return support.get(p);
	}
	
	//updated the support values into corrected decimal percentages
	public void rebalance(){
		double count = 0;
		Set<String> parties = support.keySet();
		for(String p : parties){
			count += support.get(p);
		}
		for(String p : parties){
			double d = support.get(p) / count;
			support.put(p,d);
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
			if(support.get(p.getName()) != null){
				double updated = support.get(p.getName()) * natShift.get(p) * proShift.get(p) * regShift.get(p);
				support.put(p.getName(), updated);
			}
		}
		rebalance();
	}
	
	public String toString(){
		return super.toString() + " - " + support.toString();
	}
}