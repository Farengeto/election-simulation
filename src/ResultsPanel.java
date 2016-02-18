import java.awt.Dimension;
import javax.swing.JPanel;

//superclass for result panels
public abstract class ResultsPanel extends JPanel{
	protected UpdatedVoting results;
	
	public ResultsPanel(UpdatedVoting voting,Dimension dim){
		results = voting;
		this.setSize(dim);
		this.setPreferredSize(dim);
		Dimension min = new Dimension((int)this.getSize().getWidth(),1);
		this.setMinimumSize(min);
	}
}