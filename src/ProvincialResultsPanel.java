import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

public class ProvincialResultsPanel extends ResultsPanel{
	public static final Dimension DEFAULT_SIZE = new Dimension(400,900);
	public static final int DEFAULT_WIDTH = 400;
	
	public ProvincialResultsPanel(UpdatedVoting voting){
		super(voting,new Dimension(DEFAULT_WIDTH,125*voting.getProvinces().size()));
	}

	
	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		//get current set of results
		List<Province> provinces = results.getProvinces();
		List<Party> parties = results.getParties();
		
		int count = 0;
		int startAngle = 0;
		for(Province pr : provinces){
			g.setColor(Color.BLACK);
			//g.drawRect(0,125*count,DEFAULT_WIDTH-1,125);
			g.drawLine(0,125+125*count,DEFAULT_WIDTH,125+125*count);
			parties.sort(new DivisionalComparator((Division)pr));
			//draw pi chart for regional popular voting
			g.setColor(Color.GRAY);
			g.fillOval(25,20+125*count,100,100);
			startAngle = 0;
			for (Party p : parties) {
				int arcAngle = (int) Math.round((double)pr.getVotes().get(p)/pr.population * 360.0);
				g.setColor(p.getColor());
				g.fillArc(25,20+125*count, 100, 100, 
						startAngle, arcAngle);
				startAngle += arcAngle;
			}
			g.setColor(Color. BLACK);
			g.drawString(pr.getName(), 50, 15+125*count);
			//draw bar charts for party seats in each province
			int max = 1;
			for(Party p : parties){
				max = Math.max(max,pr.getResults().get(p));
			}
			int pCount = 0;
			for(Party p : parties){
				//limit to the 10 parties with the most seats and votes, for size purposes
				//parties with no seats are not list
				if(pCount < 10 && pr.getResults().get(p) > 0){
					g.setColor(Color.BLACK);
					g.drawString("" + pr.getResults().get(p),155+200*pr.getResults().get(p)/max,29+125*count+10*pCount);
					g.setColor(p.getColor());
					g.fillRect(150, 20+125*count+10*pCount, Math.max(200*pr.getResults().get(p)/max,1), 10);
				}
				pCount++;
			}
			count++;
		}
	}
}
