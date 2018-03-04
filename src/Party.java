import java.awt.Color;
/*
 * Class containing the data for a political party
 * Defines a party name, colour and approval rating
 */
public class Party {
	private String name;
	private Color color;
	private double approval;
	
	public Party(){
		this("",new Color(128,128,128),0);
	}
	
	public Party(String name,Color color){
		this.name = name;
		this.color = color;
		this.approval = 0;
	}
	
	public Party(String name,Color color,double approval){
		this.name = name;
		this.color = color;
		this.approval = approval;
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
	
	public String getName(){
		return name;
	}
	
	public Color getColor(){
		return color;
	}
	
	public double getApproval(){
		return approval;
	}
	
	public String getResults(){
		return (name + " " + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + " " + approval*100 + "%");
	}
	
	public String toString(){
		return name;
	}
}