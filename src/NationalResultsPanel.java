import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;
import java.util.List;

//panel containing the national election results
public class NationalResultsPanel extends ResultsPanel{
	public static final Dimension DEFAULT_SIZE = new Dimension(300,900);
	public static final int DEFAULT_WIDTH = 300;
	
	public NationalResultsPanel(UpdatedVoting voting){
		super(voting,new Dimension(DEFAULT_WIDTH,500+35*voting.getParties().size()));
	}
	
	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		//get current set of results
		List<Party> parties = results.getParties();
		//find the party with the most national seats for scaling
		int max = 1;
		for(Party p : parties){
			max = Math.max(max,p.getSeats());
		}
		int count = 0;
		//draw bars for national parties
		parties.sort(new NationalComparator());
		for(Party p : parties){
			g.setColor(Color.BLACK);
			g.drawString("" + p.getName(),10,25+count*35);
			g.drawString("" + p.getSeats(),240*p.getSeats()/max+15,39+count*35);
			g.setColor(p.getColor());
			g.fillRect(10, 30+count*35, Math.max(240*p.getSeats()/max,1), 10);
			count++;
		}
		//draw pi chart for national seat counts
		g.setColor(Color.BLACK);
		g.drawString("Seat Distribution:",100,35+35*parties.size());
		g.setColor(Color.GRAY);
		g.fillOval(50,40+35*parties.size(),200,200);
		int startAngle = 0;
		for(Party p : parties) {
			int arcAngle = (int) Math.round((double)p.getSeats()/results.getSeats() * 360.0);
			g.setColor(p.getColor());
			g.fillArc(50, 40+35*parties.size(), 200, 200, 
					startAngle, arcAngle);
			startAngle += arcAngle;
		}
		//draw pi chart for national popular voting
		g.setColor(Color.BLACK);
		g.drawString("Vote Distribution:",100,285+35*parties.size());
		g.setColor(Color.GRAY);
		g.fillOval(50,290+35*parties.size(),200,200);
		startAngle = 0;
		for(Party p : parties) {
			int arcAngle = (int) Math.round((double)p.getVotes()/results.getPopulation() * 360.0);
			g.setColor(p.getColor());
			g.fillArc(50, 290+35*parties.size(), 200, 200, 
					startAngle, arcAngle);
			startAngle += arcAngle;
		}
	}
}