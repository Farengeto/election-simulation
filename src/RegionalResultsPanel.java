import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;

public class RegionalResultsPanel extends ResultsPanel{
	
	public RegionalResultsPanel(UpdatedVoting voting){
		super(voting,new Dimension(300 + (60 * voting.getParties().size()),40+20*voting.getRegions().size()));
	}
	
	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		//get current set of results
		List<Party> parties = results.getParties();
		List<Region> regions = results.getRegions();
		
		//resort parties 
		parties.sort(new NationalComparator());
		//top line contains list of parties
		int count = 0;
		g.setColor(Color.BLACK);
		g.drawString("Province",10,15);
		g.drawString("Region",110,15);
		g.drawString("Population",210,15);
		for(Party p : parties){
			g.drawString("" + p.getName().substring(0,3).toUpperCase(),300+60*count,15);
			count++;
		}
		//print each region's name, demographics and votes
		count = 0;
		for(Region r : regions){
			g.setColor(Color.BLACK);
			g.drawString(r.getProvince().getName(),10,40+20*count);
			g.drawString(r.getName(),110,40+20*count);
			g.drawString(Long.toString(r.getPopulation()),210,40+20*count);
			int pCount = 0;
			for(Party p : parties){
				g.drawString("" + r.getResults().get(p),300+60*pCount,40+20*count);
				g.drawString(Math.round(1000.0 * (double)r.getVotes().get(p) / (double)r.getPopulation())/10.0 + "%",320+60*pCount,40+20*count);
				pCount++;
			}
			count++;
		}
	}
}