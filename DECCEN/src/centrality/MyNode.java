package centrality;
import peersim.core.GeneralNode;


public class MyNode extends GeneralNode {
	
	private String label;

	public MyNode(String prefix) {
		super(prefix);
		label = "DEFAULT_LABEL";
	}
	
	public void setLabel(String l) { label = l; }
	
	public String getLabel() { return label; }
	
	public String toString() { return getLabel() + "[ID=" + getID() + "]"; }

}
