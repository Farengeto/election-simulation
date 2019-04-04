package nation;

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
public class Province extends Division {
	private List<Region> regions;

	//blank province
	public Province() {
		this("");
	}

	//province with name only
	public Province(String name) {
		this(name, new ArrayList<Region>());
	}

	//province with name and list of regions, pop and seat count generated from it
	public Province(String name, List<Region> regions) {
		super(name, 0, 0);
		long pop = 0;
		int seat = 0;
		for (Region region : regions) {
			pop += region.getPopulation();
			seat += region.getSeats();
		}
		this.population = pop;
		this.seats = seat;
		this.regions = regions;
	}

	public void addRegion(Region r) {
		if (regions.add(r)) {
			population += r.getPopulation();
			seats += r.getSeats();
		}
	}

	public void removeRegion(Region r) {
		if (regions.remove(r)) {
			population -= r.getPopulation();
			seats -= r.getSeats();
		}
	}

	public List<Region> getRegions() {
		return regions;
	}

	public String toString() {
		String s = "Province " + super.toString();
		for (Region r : regions) {
			s = s.concat("\n" + r.toString());
		}
		return s;
	}
}