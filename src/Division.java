//generic political division superclass
public abstract class Division{
	protected long population;
	protected int seats;
	protected String name;
	
	
	public Division(){
		this("",0,0);
	}
	
	public Division(String name, long pop, int seat){
		this.name = name;
		population = pop;
		seats = seat;
	}
	
	//change the population of the division by an amount
	//ensure division does not have negative population
	//division with zero population allowed
	public void changePopulation(long popChange){
		if(popChange+population < 0){
			population = 0;
		}
		else{
			population += popChange;
		}
	}
	
	//change the seats in a division by an amount
	//ensure division does not have negative seats
	//division with zero seats allowed
	public void changeSeats(int seatChange){
		if(seatChange+seats < 0){
			seats = 0;
		}
		else{
			seats += seatChange;
		}
	}
	
	public String getName(){
		return name;
	}
	
	public long getPopulation(){
		return population;
	}
	
	public int getSeats(){
		return seats;
	}
	
	public String toString(){
		return name + ": Population " + population + ", " + seats + " seats";
	}
}