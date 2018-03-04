import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class VotingData {
	private ElectionData source;
	private Map<Region, VotingRegionData> regionData;
	private Map<Province, VotingDivisionData> provinceData;
	private VotingDivisionData nationData;
	
	private Map<String, Integer> nationalSeats; //Seats that only exist at a national level
	
	public VotingData(ElectionData electionSource){
		source = electionSource;
		regionData = new HashMap<>();
		provinceData = new HashMap<>();
		nationalSeats = new HashMap<>();
		for(Region r : source.getRegions()){
			regionData.put(r, new VotingRegionData(r));
		}
	}
	
	public void modifySupport(double stdDev){
		Random random = new Random();
		double stdDevDiv = stdDev / Math.sqrt(3.0); //factor to have a combined effect add to stdDev
		
		Map<String, Double> nationalShift = generateShift(stdDevDiv, random);
		for(Province pr : source.getProvinces()){
			Map<String, Double> provincialShift = generateShift(stdDevDiv, random);
			for(Region r : pr.getRegions()){
				Map<String, Double> regionalShift = generateShift(stdDevDiv, random);
				
				VotingRegionData rData = regionData.get(r);
				Map<String, Double> newSupport = new HashMap<>();
				for(Party p : source.getParties()){
					String pName = p.getName();
					double totalShift = 1 + nationalShift.get(pName)
							+ provincialShift.get(pName) + regionalShift.get(pName);
					double newValue = Math.max(rData.getSupport(pName) * totalShift, 0.0);
					newSupport.put(pName, newValue);
				}
				rData.setSupport(newSupport);
			}
		}
	}
	
	public Map<String, Double> generateShift(double stdDev, Random random){
		Map<String, Double> shift = new HashMap<>();
		for(Party p : source.getParties()){
			shift.put(p.getName(), random.nextGaussian()*stdDev);
		}
		return shift;
	}
	
	public void calculateResults(VotingType votingType){
		//clear old results
		provinceData.clear();
		nationData = null;
		//get the vote counts
		allocateVotes();
		//allocate seats based on voting method
		switch(votingType){
			case PR_HARE:
				proportionalHare();
				break;
			case PR_DROOP:
				proportionalDroop();
				break;
			case PR_HARE_NATIONAL:
				proportionalHareNational();
				break;
			case PR_DROOP_NATIONAL:
				proportionalDroopNational();
				break;
			case FPTP:
				firstPastThePost();
				break;
			case FPTP_PROVINCE:
				firstPastThePostProvincial();
				break;
			case FPTP_NATIONAL:
				firstPastThePostNational();
				break;
			default:
				break;
		}
	}
	
	private void allocateVotes(){
		for(Region r : regionData.keySet()){
			Map<String,Long> votes = new HashMap<>();
			//value used to track and correct rounding errors
			//keeps error within half a vote
			double rounding = 0.0;
			for(Party p : source.getParties()){
				String pName = p.getName();
				double approximate = r.getPopulation() * regionData.get(r).getSupport(pName);
				long actual = Math.round(approximate);
				
				//correct rounding errors
				rounding += actual - approximate;
				if(rounding >= 0.5 && actual > 0){
	                actual--;
	                rounding -= 1;
	            }
	            else if (rounding <= -0.5){
	            	actual++;
	                rounding += 1;
	            }
				
				votes.put(pName, actual);
			}
			regionData.get(r).setVotes(votes);
		}
	}
	
	//Calculates seats allocations according to a proportional representation.
	//Allocation is done at the regional level
	//Uses the Hare quota for largest remainder method
	private void proportionalHare(){
		for(Region r : regionData.keySet()){
			VotingRegionData data = regionData.get(r);
			long quota = r.getPopulation()/r.getSeats();
			int remainingSeats = r.getSeats();
			Map<String,Integer> seats = new HashMap<>();
			Map<String,Long> remainder = new HashMap<>();
			for(Party p : source.getParties()){
				String pName = p.getName();
				long pVotes = data.getVotes(pName);
				int pSeats = (int)(pVotes / quota);
				remainingSeats -= pSeats;
				seats.put(pName, pSeats);
				remainder.put(pName, pVotes % quota);
			}
			
			while(remainingSeats > 0){
				long max = 0;
				String winner = "";
				
				for(String p : remainder.keySet()){
					long pVotes = remainder.get(p);
					if(pVotes > max){
						max = pVotes;
						winner = p;
					}
				}
				
				seats.put(winner, seats.get(winner)+1);
				remainder.remove(winner);
				remainingSeats--;
			}
			
			regionData.get(r).setSeats(seats);
		}
	}
	
	//Calculates seats allocations according to a proportional representation.
	//Allocation is done at the regional level
	//Uses the Hare quota for largest remainder method
	private void proportionalDroop(){
		for(Region r : regionData.keySet()){
			VotingRegionData data = regionData.get(r);
			long quota = (r.getPopulation()/(r.getSeats() + 1)) + 1;
			int remainingSeats = r.getSeats();
			Map<String,Integer> seats = new HashMap<>();
			Map<String,Long> remainder = new HashMap<>();
			for(Party p : source.getParties()){
				String pName = p.getName();
				long pVotes = data.getVotes(pName);
				int pSeats = (int)(pVotes / quota);
				remainingSeats -= pSeats;
				seats.put(pName, pSeats);
				remainder.put(pName, pVotes % quota);
			}
			
			while(remainingSeats > 0){
				long max = 0;
				String winner = "";
				
				for(String p : remainder.keySet()){
					long pVotes = remainder.get(p);
					if(pVotes > max){
						max = pVotes;
						winner = p;
					}
				}
				
				seats.put(winner, seats.get(winner)+1);
				remainder.remove(winner);
				remainingSeats--;
			}
			
			regionData.get(r).setSeats(seats);
		}
	}
	
	//Calculates seats allocations according to a proportional representation.
	//Allocation is done at the national level
	//Uses the Hare quota for largest remainder method
	private void proportionalHareNational(){
		Map<String,Long> votes = new HashMap<>();
		for(Party p : source.getParties()){
			String pName = p.getName();
			long totalVotes = 0;
			for(Region r : regionData.keySet()){
				totalVotes += regionData.get(r).getVotes(pName);
			}
			votes.put(pName, totalVotes);
		}
		
		long quota = source.getPopulation()/source.getSeats();
		int remainingSeats = source.getSeats();
		Map<String,Long> remainder = new HashMap<>();
		for(Party p : source.getParties()){
			String pName = p.getName();
			long pVotes = votes.get(pName);
			int pSeats = (int)(pVotes / quota);
			remainingSeats -= pSeats;
			nationalSeats.put(pName, pSeats);
			remainder.put(pName, pVotes % quota);
		}
			
		while(remainingSeats > 0){
			long max = 0;
			String winner = "";
			
			for(String p : remainder.keySet()){
				long pVotes = remainder.get(p);
				if(pVotes > max){
					max = pVotes;
					winner = p;
				}
			}
				
			nationalSeats.put(winner, nationalSeats.get(winner)+1);
			remainder.remove(winner);
			remainingSeats--;
		}

		//Since we've already calculated it, set the nationData now to save time
		nationData = new VotingDivisionData();
		nationData.setSeats(nationalSeats);
		nationData.setVotes(votes);
	}
	
	//Calculates seats allocations according to a proportional representation.
	//Allocation is done at the national level
	//Uses the Hare quota for largest remainder method
	private void proportionalDroopNational(){
		Map<String,Long> votes = new HashMap<>();
		for(Party p : source.getParties()){
			String pName = p.getName();
			long totalVotes = 0;
			for(Region r : regionData.keySet()){
				totalVotes += regionData.get(r).getVotes(pName);
			}
			votes.put(pName, totalVotes);
		}
		
		long quota = (source.getPopulation()/(source.getSeats() + 1)) + 1;
		int remainingSeats = source.getSeats();
		Map<String,Long> remainder = new HashMap<>();
		for(Party p : source.getParties()){
			String pName = p.getName();
			long pVotes = votes.get(pName);
			int pSeats = (int)(pVotes / quota);
			remainingSeats -= pSeats;
			nationalSeats.put(pName, pSeats);
			remainder.put(pName, pVotes % quota);
		}
			
		while(remainingSeats > 0){
			long max = 0;
			String winner = "";
			
			for(String p : remainder.keySet()){
				long pVotes = remainder.get(p);
				if(pVotes > max){
					max = pVotes;
					winner = p;
				}
			}
				
			nationalSeats.put(winner, nationalSeats.get(winner)+1);
			remainder.remove(winner);
			remainingSeats--;
		}
		
		//Since we've already calculated it, set the nationData now to save time
		nationData = new VotingDivisionData();
		nationData.setSeats(nationalSeats);
		nationData.setVotes(votes);
	}
	
	//Calculates seats allocations according to a winner-takes-all FPTP system.
	//Allocation is done at the regional level
	//In the event of a tie, the party that comes first in the list wins.
	private void firstPastThePost(){
		for(Region r : regionData.keySet()){
			VotingRegionData data = regionData.get(r);
			Map<String,Integer> seats = new HashMap<>();
			
			long max = 0;
			String winner = "";
			
			for(Party p : source.getParties()){
				String pName = p.getName();
				long pVotes = data.getVotes(pName);
				if(pVotes > max){
					max = pVotes;
					winner = pName;
				}
			}
			
			for(Party p : source.getParties()){
				String pName = p.getName();
				int seatsWon = 0;
				
				if(pName.equals(winner)){
					seatsWon = r.getSeats();
				}
				
				seats.put(pName, seatsWon);
			}
			data.setSeats(seats);
		}
	}
	
	//Calculates seats allocations according to a winner-takes-all FPTP system.
	//Allocation is done at the provincial level
	//In the event of a tie, the party that comes first in the list wins.
	private void firstPastThePostProvincial(){
		for(Province pr : source.getProvinces()){
			Map<String,Long> votes = new HashMap<>();
			for(Party p : source.getParties()){
				String pName = p.getName();
				long pVotes = 0;
				for(Region r : pr.getRegions()){
					pVotes += regionData.get(r).getVotes(pName);
				}
				votes.put(pName, pVotes);
			}
			
			long max = 0;
			String winner = "";
			
			for(String p : votes.keySet()){
				long pVotes = votes.get(p);
				if(pVotes > max){
					max = pVotes;
					winner = p;
				}
			}
			
			
			for(Region r : pr.getRegions()){
				Map<String,Integer> seats = new HashMap<>();
				for(String p : votes.keySet()){
					int seatsWon = 0;
					if(p.equals(winner)){
						seatsWon = r.getSeats();
					}
					
					seats.put(p, seatsWon);
				}
				regionData.get(r).setSeats(seats);
			}
		}
	}
	
	//Calculates seats allocations according to a winner-takes-all FPTP system.
	//Allocation is done at the national level
	//In the event of a tie, the party that comes first in the list wins.
	private void firstPastThePostNational(){
		Map<String,Long> votes = new HashMap<>();
		long max = 0;
		String winner = "";
		
		for(Party p : source.getParties()){
			String pName = p.getName();
			long totalVotes = 0;
			for(Region r : regionData.keySet()){
				totalVotes += regionData.get(r).getVotes(pName);
			}
			if(totalVotes > max){
				max = totalVotes;
				winner = pName;
			}
			votes.put(pName, totalVotes);
		}
		for(String p : votes.keySet()){
			if(p.equals(winner)){
				nationalSeats.put(p, source.getSeats());
			}
			else{
				nationalSeats.put(p, 0);
			}
		}
		
		//Since we've already calculated it, set the nationData now to save time
		nationData = new VotingDivisionData();
		nationData.setSeats(nationalSeats);
		nationData.setVotes(votes);
	}
	
	private void createNationResults(){
		Map<String,Integer> seats = new HashMap<>();
		Map<String,Long> votes = new HashMap<>();
		for(Party p : source.getParties()){
			String pName = p.getName();
			int totalSeats = 0;
			long totalVotes = 0;
			if(nationalSeats.containsKey(pName)){
				totalSeats += nationalSeats.get(pName);
			}
			for(Region r : regionData.keySet()){
				totalSeats += regionData.get(r).getSeats(pName);
				totalVotes += regionData.get(r).getVotes(pName);
			}
			seats.put(pName, totalSeats);
			votes.put(pName, totalVotes);
		}
		
		nationData = new VotingDivisionData();
		nationData.setSeats(seats);
		nationData.setVotes(votes);
	}
	
	private void createProvinceResults(Province pr){
		Map<String,Integer> seats = new HashMap<>();
		Map<String,Long> votes = new HashMap<>();
		for(Party p : source.getParties()){
			String pName = p.getName();
			int totalSeats = 0;
			long totalVotes = 0;
			for(Region r : pr.getRegions()){
				totalSeats += regionData.get(r).getSeats(pName);
				totalVotes += regionData.get(r).getVotes(pName);
			}
			seats.put(pName, totalSeats);
			votes.put(pName, totalVotes);
		}
		
		VotingDivisionData data = new VotingDivisionData();
		data.setSeats(seats);
		data.setVotes(votes);
		provinceData.put(pr, data);
	}
	
	public long getVotesNation(Party p){
		if(nationData == null)
			createNationResults();
		return nationData.getVotes(p.getName());
	}
	public int getSeatsNation(Party p){
		if(nationData == null)
			createNationResults();
		return nationData.getSeats(p.getName());
	}
	
	public long getVotesProvince(Province pr, Party p){
		if(!provinceData.containsKey(pr))
			createProvinceResults(pr);
		return provinceData.get(pr).getVotes(p.getName());
	}
	public int getSeatsProvince(Province pr, Party p){
		if(!provinceData.containsKey(pr))
			createProvinceResults(pr);
		return provinceData.get(pr).getSeats(p.getName());
	}
	
	public long getVotesRegion(Region r, Party p){
		return regionData.get(r).getVotes(p.getName());
	}
	public int getSeatsRegion(Region r, Party p){
		return regionData.get(r).getSeats(p.getName());
	}
}
