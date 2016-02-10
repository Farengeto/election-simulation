import java.awt.Color;

public class Party {
	private String name;
	private Color color;
	private double approval;
	private long votes;
	private int seats;
	
	public Party(){
		this("",new Color(128,128,128),0);
	}
	
	public Party(String name,Color color,double approval){
		this.name = name;
		this.color = color;
		this.approval = approval;
		votes = 0;
		seats = 0;
	}
	
	public void setName(String newName){
		name = newName;
	}
	
	public void setColor(Color newColor){
		color = newColor;
	}
	
	public void setColor(int r, int g, int b){
		color = new Color(r,g,b);
	}
	
	public void setApproval(double newRating){
		approval = newRating;
	}
	
	public void setResults(long vote,int seat){
		votes = vote;
		seats = seat;
	}
	
	public String getName(){
		return name;
	}
	
	public Color getColor(){
		return color;
	}
	
	public long getVotes(){
		return votes;
	}
	
	public int getSeats(){
		return seats;
	}
	
	public double getApproval(){
		return approval;
	}
	
	public String getResults(){
		return (name + " " + color.getRed() + "," + color.getBlue() + "," + color.getGreen() + " " + approval*100 + "% - " + seats + " seats, " + votes + " votes");
	}
	
	public String toString(){
		return name;
	}
}