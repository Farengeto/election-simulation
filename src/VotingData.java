import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class VotingData {
	private ElectionData source;
	private Map<Region, VotingRegionData> regionData;
	private Map<Province, VotingDivisionData> provinceData;
	private VotingDivisionData nationData;
	private VotingType lastVotingType = VotingType.FPTP;
	
	private Map<String, Integer> nationalSeats; //Seats that only exist at a national level
	private Map<Province, Map<String, Integer>> provincialSeats; //Seats that only exist at a provincial level
	
	public VotingData(ElectionData electionSource){
		source = electionSource;
		regionData = new HashMap<>();
		provinceData = new HashMap<>();
		nationalSeats = new HashMap<>();
		provincialSeats = new HashMap<>();
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
	
	/**
	 * Allocates seats and votes based on the input data, using a given voting system.
	 * @param votingType The electoral system to be used
	 */
	public void calculateResults(VotingType votingType, double electionThreshold){
		//clear old results
		provinceData.clear();
		nationData = null;
		//get the vote counts
		allocateVotes();
		//allocate seats based on voting method
		lastVotingType = votingType;
		switch(votingType){
			case PR_HARE:
				proportionalRegional(ProportionalMethod.hareMethod, electionThreshold);
				break;
			case PR_DROOP:
				proportionalRegional(ProportionalMethod.droopMethod, electionThreshold);
				break;
			case PR_HARE_PROVINCE:
				proportionalProvincial(ProportionalMethod.hareMethod, electionThreshold);
				break;
			case PR_DROOP_PROVINCE:
				proportionalProvincial(ProportionalMethod.droopMethod, electionThreshold);
				break;
			case PR_HARE_NATIONAL:
				proportionalNational(ProportionalMethod.hareMethod, electionThreshold);
				break;
			case PR_DROOP_NATIONAL:
				proportionalNational(ProportionalMethod.droopMethod, electionThreshold);
				break;
			case FPTP:
				firstPastThePostRegional();
				break;
			case FPTP_PROVINCE:
				firstPastThePostProvincial();
				break;
			case FPTP_NATIONAL:
				firstPastThePostNational();
				break;
			case MMP_DHONDT_PROVINCE:
				mixedMemberProportionalProvincial(ProportionalMethod.dHondtMethod, electionThreshold);
				break;
			case MMP_SAINTELAGUE_PROVINCE:
				mixedMemberProportionalProvincial(ProportionalMethod.sainteLagueMethod, electionThreshold);
				break;
			case MMP_DHONDT_NATIONAL:
				mixedMemberProportionalNational(ProportionalMethod.dHondtMethod, electionThreshold);
				break;
			case MMP_SAINTELAGUE_NATIONAL:
				mixedMemberProportionalNational(ProportionalMethod.sainteLagueMethod, electionThreshold);
				break;
			case MMM_HARE_PROVINCE:
				mixedMemberMajoritarianProvincial(ProportionalMethod.hareMethod, electionThreshold);
				break;
			case MMM_DROOP_PROVINCE:
				mixedMemberMajoritarianProvincial(ProportionalMethod.droopMethod, electionThreshold);
				break;
			case MMM_HARE_NATIONAL:
				mixedMemberMajoritarianNational(ProportionalMethod.hareMethod, electionThreshold);
				break;
			case MMM_DROOP_NATIONAL:
				mixedMemberMajoritarianNational(ProportionalMethod.droopMethod, electionThreshold);
				break;
		}
	}
	
	/**
	 * Calculates the allocation of votes based on the input data.
	 */
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
	
	/**
	 * Calculates seats allocations according to a proportional representation system.
	 * Allocation is done at the regional level
	 * @param quotaMethod The type of largest remainder method to be used.
	 */
	private void proportionalRegional(ProportionalMethod quotaMethod, double electionThreshold){
		Map<String,Integer> seats;
		Map<String,Long> remainder;
		Map<String,Long> thresholdVotes;
		for(Region r : regionData.keySet()){
			seats = new HashMap<>();
			remainder = new HashMap<>();
			thresholdVotes = new HashMap<>();
			VotingRegionData data = regionData.get(r);
			
			long threshold = (long)(r.getPopulation() * electionThreshold);
			long discardedVotes = 0;
			for(Party p : source.getParties()){
				String pName = p.getName();
				long totalVotes = data.getVotes(pName);
				
				if(totalVotes < threshold){
					discardedVotes += totalVotes;
					thresholdVotes.put(pName, 0L);
				}
				else{
					thresholdVotes.put(pName, totalVotes);
				}
			}
			
			long quota = quotaMethod.getValue(r.getPopulation() - discardedVotes, r.getSeats());
			int remainingSeats = r.getSeats();
			for(String p : thresholdVotes.keySet()){
				long pVotes = thresholdVotes.get(p);
				int pSeats = (int)(pVotes / quota);
				remainingSeats -= pSeats;
				seats.put(p, pSeats);
				remainder.put(p, pVotes % quota);
			}
			
			while(remainingSeats > 0 && !remainder.isEmpty()){
				long max = -1;
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
	
	/**
	 * Calculates seats allocations according to a proportional representation system.
	 * Allocation is done at the provincial level
	 * @param quotaMethod The type of largest remainder method to be used.
	 */
	private void proportionalProvincial(ProportionalMethod quotaMethod, double electionThreshold){
		Map<String,Long> votes;
		Map<String, Integer> provinceSeats;
		Map<String,Long> remainder;
		for(Province pr : source.getProvinces()){
			votes = new HashMap<>();
			provinceSeats = new HashMap<>();
			remainder = new HashMap<>();
			
			long threshold = (long)(pr.getPopulation() * electionThreshold);
			long discardedVotes = 0;
			for(Party p : source.getParties()){
				String pName = p.getName();
				long totalVotes = 0;
				for(Region r : pr.getRegions()){
					totalVotes += regionData.get(r).getVotes(pName);
				}
				
				if(totalVotes < threshold){
					discardedVotes += totalVotes;
					votes.put(pName, 0L);
				}
				else{
					votes.put(pName, totalVotes);
				}
			}
			
			long quota = quotaMethod.getValue(pr.getPopulation() - discardedVotes, pr.getSeats());
			int remainingSeats = pr.getSeats();
			for(String p : votes.keySet()){
				long pVotes = votes.get(p);
				int pSeats = (int)(pVotes / quota);
				remainingSeats -= pSeats;
				provinceSeats.put(p, pSeats);
				remainder.put(p, pVotes % quota);
			}
			
			while(remainingSeats > 0 && !remainder.isEmpty()){
				long max = -1;
				String winner = "";
				
				for(String p : remainder.keySet()){
					long pVotes = remainder.get(p);
					if(pVotes > max){
						max = pVotes;
						winner = p;
					}
				}
				
				provinceSeats.put(winner, provinceSeats.get(winner)+1);
				remainder.remove(winner);
				remainingSeats--;
			}
			
			provincialSeats.put(pr, provinceSeats);
		}
	}
	
	/**
	 * Calculates seats allocations according to a proportional representation system.
	 * Allocation is done at the national level
	 * @param quotaMethod The type of largest remainder method to be used.
	 */
	private void proportionalNational(ProportionalMethod quotaMethod, double electionThreshold){
		Map<String,Long> votes = new HashMap<>();
		Map<String,Long> votesThreshold = new HashMap<>();
		Map<String,Long> remainder = new HashMap<>();
		
		long threshold = (long)(source.getPopulation() * electionThreshold);
		long discardedVotes = 0;
		for(Party p : source.getParties()){
			String pName = p.getName();
			long totalVotes = 0;
			for(Region r : regionData.keySet()){
				totalVotes += regionData.get(r).getVotes(pName);
			}
			
			if(totalVotes < threshold){
				discardedVotes += totalVotes;
				votesThreshold.put(pName, 0L);
			}
			else{
				votesThreshold.put(pName, totalVotes);
			}
			votes.put(pName, totalVotes);
		}
		
		long quota = quotaMethod.getValue(source.getPopulation() - discardedVotes, source.getSeats());
		int remainingSeats = source.getSeats();
		for(String p : votesThreshold.keySet()){
			long pVotes = votesThreshold.get(p);
			int pSeats = (int)(pVotes / quota);
			remainingSeats -= pSeats;
			nationalSeats.put(p, pSeats);
			remainder.put(p, pVotes % quota);
		}
			
		while(remainingSeats > 0 && !remainder.isEmpty()){
			long max = -1;
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
	
	/**
	 * Calculates seats allocations according to a winner-takes-all FPTP system.
	 * Allocation is done at the regional level
	 */
	private void firstPastThePostRegional(){
		for(Region r : regionData.keySet()){
			VotingRegionData data = regionData.get(r);
			Map<String,Integer> seats = new HashMap<>();
			
			long max = -1;
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
	
	/**
	 * Calculates seats allocations according to a winner-takes-all FPTP system.
	 * Allocation is done at the provincial level
	 */
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
			
			long max = -1;
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
	
	/**
	 * Calculates seats allocations according to a winner-takes-all FPTP system.
	 * Allocation is done at the national level
	 */
	private void firstPastThePostNational(){
		Map<String,Long> votes = new HashMap<>();
		long max = -1;
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
	
	/**
	 * Calculates seats allocations according to a mixed member majoritarian system.
	 * Plurality allocation is done at the regional level
	 * Proportional allocation is done at the provincial level
	 * @param quotaMethod The type of largest remainder method to be used.
	 */
	private void mixedMemberMajoritarianProvincial(ProportionalMethod quotaMethod, double electionThreshold){
		Map<String,Integer> seats;
		for(Region r : regionData.keySet()){
			VotingRegionData data = regionData.get(r);
			seats = new HashMap<>();
			
			long max = -1;
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
		
		Map<String,Long> votes;
		Map<String,Long> remainder;
		Map<String, Integer> provinceSeats;
		for(Province pr : source.getProvinces()){
			votes = new HashMap<>();
			remainder = new HashMap<>();
			provinceSeats = new HashMap<>();
			
			long threshold = (long)(pr.getPopulation() * electionThreshold);
			long discardedVotes = 0;
			for(Party p : source.getParties()){
				String pName = p.getName();
				long totalVotes = 0;
				for(Region r : pr.getRegions()){
					totalVotes += regionData.get(r).getVotes(pName);
				}
				
				if(totalVotes < threshold){
					discardedVotes += totalVotes;
					votes.put(pName, 0L);
				}
				else{
					votes.put(pName, totalVotes);
				}
			}
			
			long quota = quotaMethod.getValue(pr.getPopulation() - discardedVotes , pr.getSeats());
			int remainingSeats = pr.getSeats();
			for(String p : votes.keySet()){
				long pVotes = votes.get(p);
				int pSeats = (int)(pVotes / quota);
				remainingSeats -= pSeats;
				provinceSeats.put(p, pSeats);
				remainder.put(p, pVotes % quota);
			}
				
			while(remainingSeats > 0 && !remainder.isEmpty()){
				long max = -1;
				String winner = "";
				
				for(String p : remainder.keySet()){
					long pVotes = remainder.get(p);
					if(pVotes > max){
						max = pVotes;
						winner = p;
					}
				}
					
				provinceSeats.put(winner, provinceSeats.get(winner)+1);
				remainder.remove(winner);
				remainingSeats--;
			}
			
			provincialSeats.put(pr, provinceSeats);
		}
	}
	
	/**
	 * Calculates seats allocations according to a mixed member majoritarian system.
	 * Plurality allocation is done at the regional level
	 * Proportional allocation is done at the national level
	 * @param quotaMethod The type of largest remainder method to be used.
	 */
	private void mixedMemberMajoritarianNational(ProportionalMethod quotaMethod, double electionThreshold){
		Map<String,Integer> seats;
		for(Region r : regionData.keySet()){
			VotingRegionData data = regionData.get(r);
			seats = new HashMap<>();
			
			long max = -1;
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
		
		long threshold = (long)(source.getPopulation() * electionThreshold);
		long discardedVotes = 0;
		Map<String,Long> votes = new HashMap<>();
		for(Party p : source.getParties()){
			String pName = p.getName();
			long totalVotes = 0;
			for(Region r : regionData.keySet()){
				totalVotes += regionData.get(r).getVotes(pName);
			}
			if(totalVotes < threshold){
				discardedVotes += totalVotes;
				votes.put(pName, 0L);
			}
			else{
				votes.put(pName, totalVotes);
			}
		}
		
		long quota = quotaMethod.getValue(source.getPopulation() - discardedVotes, source.getSeats());
		int remainingSeats = source.getSeats();
		Map<String,Long> remainder = new HashMap<>();
		for(String p : votes.keySet()){
			long pVotes = votes.get(p);
			int pSeats = (int)(pVotes / quota);
			remainingSeats -= pSeats;
			nationalSeats.put(p, pSeats);
			remainder.put(p, pVotes % quota);
		}
			
		while(remainingSeats > 0 && !remainder.isEmpty()){
			long max = -1;
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
	}
	
	/**
	 * Calculates seats allocations according to a mixed member proportional system.
	 * Plurality allocation is done at the regional level
	 * Proportional allocation is done at the provincial level
	 * Does not use overhang seats
	 * @param quotientMethod The type of highest quotient method to be used.
	 */
	private void mixedMemberProportionalProvincial(ProportionalMethod quotientMethod, double electionThreshold){
		Map<String,Long> totalVotes;
		Map<String,Integer> totalSeats;
		Map<String,Integer> provinceSeats;
		for(Province pr : source.getProvinces()){
			totalVotes = new HashMap<>();
			totalSeats = new HashMap<>();
			provinceSeats = new HashMap<>();
			
			//Calculate local seats using FPTP
			Map<String,Integer> seats;
			for(Region r : pr.getRegions()){
				VotingRegionData data = regionData.get(r);
				seats = new HashMap<>();
				
				long max = -1;
				String winner = "";
				
				for(Party p : source.getParties()){
					String pName = p.getName();
					long pVotes = data.getVotes(pName);
					if(pVotes > max){
						max = pVotes;
						winner = pName;
					}
					if(totalVotes.containsKey(pName)){
						totalVotes.put(pName, pVotes + totalVotes.get(pName));
					}
					else{
						totalVotes.put(pName, pVotes);
						totalSeats.put(pName, 0);
						provinceSeats.put(pName, 0);
					}
				}
				
				for(Party p : source.getParties()){
					String pName = p.getName();
					int seatsWon = 0;
					
					if(pName.equals(winner)){
						seatsWon = r.getSeats();
					}
					
					seats.put(pName, seatsWon);
					totalSeats.put(pName, seatsWon + totalSeats.get(pName));
				}
				data.setSeats(seats);
			}
			
			//Calculate remaining seats using quotient
			int remainingSeats = pr.getSeats();
			long threshold = (long)(pr.getPopulation() * electionThreshold);
			Map<String,Long> quotients = new HashMap<>();
			for(Party p : source.getParties()){
				String pName = p.getName();
				if(totalVotes.get(pName) >= threshold){
					long quotient = quotientMethod.getValue(totalVotes.get(pName), totalSeats.get(pName));
					quotients.put(pName, quotient);
				}
			}
			
			while(remainingSeats > 0 && !quotients.isEmpty()){
				long max = -1;
				String winner = "";
				
				for(String p : quotients.keySet()){
					long pVotes = quotients.get(p);
					if(pVotes > max){
						max = pVotes;
						winner = p;
					}
				}
					
				provinceSeats.put(winner, provinceSeats.get(winner)+1);
				long newQuotient = quotientMethod.getValue(totalVotes.get(winner), totalSeats.get(winner) + provinceSeats.get(winner));
				quotients.put(winner, newQuotient);
				remainingSeats--;
			}
			
			provincialSeats.put(pr, provinceSeats);
		}
	}
	
	/**
	 * Calculates seats allocations according to a mixed member proportional system.
	 * Plurality allocation is done at the regional level
	 * Proportional allocation is done at the national level
	 * Does not use overhang seats
	 * @param quotientMethod The type of highest quotient method to be used.
	 */
	private void mixedMemberProportionalNational(ProportionalMethod quotientMethod, double electionThreshold){
		Map<String,Long> totalVotes = new HashMap<>();
		Map<String,Integer> totalSeats = new HashMap<>();
		
		//Calculate local seats using FPTP
		for(Region r : regionData.keySet()){
			VotingRegionData data = regionData.get(r);
			Map<String,Integer> seats = new HashMap<>();
			
			long max = -1;
			String winner = "";
			
			for(Party p : source.getParties()){
				String pName = p.getName();
				long pVotes = data.getVotes(pName);
				if(pVotes > max){
					max = pVotes;
					winner = pName;
				}
				if(totalVotes.containsKey(pName)){
					totalVotes.put(pName, pVotes + totalVotes.get(pName));
				}
				else{
					totalVotes.put(pName, pVotes);
					totalSeats.put(pName, 0);
					nationalSeats.put(pName, 0);
				}
			}
			
			for(Party p : source.getParties()){
				String pName = p.getName();
				int seatsWon = 0;
				
				if(pName.equals(winner)){
					seatsWon = r.getSeats();
				}
				
				seats.put(pName, seatsWon);
				totalSeats.put(pName, seatsWon + totalSeats.get(pName));
			}
			data.setSeats(seats);
		}
		
		//Calculate remaining seats using quotient
		int remainingSeats = source.getSeats();
		long threshold = (long)(source.getPopulation() * electionThreshold);
		Map<String,Long> quotients = new HashMap<>();
		for(Party p : source.getParties()){
			String pName = p.getName();
			if(totalVotes.get(pName) >= threshold){
				long quotient = quotientMethod.getValue(totalVotes.get(pName), totalSeats.get(pName));
				quotients.put(pName, quotient);
			}
		}
		
		while(remainingSeats > 0 && !quotients.isEmpty()){
			long max = -1;
			String winner = "";
			
			for(String p : quotients.keySet()){
				long pVotes = quotients.get(p);
				if(pVotes > max){
					max = pVotes;
					winner = p;
				}
			}
				
			nationalSeats.put(winner, nationalSeats.get(winner)+1);
			long newQuotient = quotientMethod.getValue(totalVotes.get(winner), totalSeats.get(winner) + nationalSeats.get(winner));
			quotients.put(winner, newQuotient);
			remainingSeats--;
		}
	}
	
	/**
	 * Creates national level election results
	 */
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
			for(Province pr : source.getProvinces()){
				if(provincialSeats.containsKey(pr)){
					Map<String, Integer> provinceSeats = provincialSeats.get(pr);
					if(provinceSeats.containsKey(pName)){
						totalSeats += provinceSeats.get(pName);
					}
				}
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
	
	/**
	 * Creates provincial level election results
	 */
	private void createProvinceResults(Province pr){
		Map<String,Integer> seats = new HashMap<>();
		Map<String,Long> votes = new HashMap<>();
		for(Party p : source.getParties()){
			String pName = p.getName();
			int totalSeats = 0;
			long totalVotes = 0;
			if(provincialSeats.containsKey(pr)){
				Map<String, Integer> provinceSeats = provincialSeats.get(pr);
				if(provinceSeats.containsKey(pName)){
					totalSeats += provinceSeats.get(pName);
				}
			}
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
	
	/**
	 * Gets the national population, including any modifications to the source value
	 * @return the population value with modifiers
	 */
	public long getVotesTotal(){
		return source.getPopulation();
	}
	
	/**
	 * Gets the national seat count, including any modifications to the source value
	 * @return the national seat count with modifiers
	 */
	public int getSeatsTotal(){
		switch(lastVotingType){
			case MMP_DHONDT_PROVINCE:
			case MMP_SAINTELAGUE_PROVINCE:
			case MMP_DHONDT_NATIONAL:
			case MMP_SAINTELAGUE_NATIONAL:
			case MMM_HARE_PROVINCE:
			case MMM_DROOP_PROVINCE:
			case MMM_HARE_NATIONAL:
			case MMM_DROOP_NATIONAL:
				return source.getSeats()*2;
			default:
				return source.getSeats();
		}
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
	
	/**
	 * Clears any current election data
	 */
	public void resetElection(){
		provinceData = new HashMap<>();
		nationalSeats = new HashMap<>();
		for(Region r : source.getRegions()){
			regionData.get(r).resetResults();;
		}
	}
	
	/**
	 * Clears any current election data and support modifiers
	 */
	public void resetAll(){
		provinceData = new HashMap<>();
		nationalSeats = new HashMap<>();
		for(Region r : source.getRegions()){
			regionData.get(r).reset();
		}
	}

}
