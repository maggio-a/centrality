
import java.util.ArrayList;

import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;

public class LinkableImplementation implements Linkable, Protocol {
	
	private ArrayList<Node> neighbors;

	public LinkableImplementation(String prefix) { neighbors = new ArrayList<Node>(); }
	
	@Override
	public Object clone() {
		LinkableImplementation lnk = null;
		try {
			lnk = (LinkableImplementation) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		lnk.neighbors = new ArrayList<Node>();
		return lnk;
	}

	@Override
	public void onKill() { neighbors = null; }

	@Override
	public int degree() { return neighbors.size(); }

	@Override
	public Node getNeighbor(int i) { return neighbors.get(i); }

	@Override
	public boolean addNeighbor(Node n) {
		if ((n != null) && (!this.contains(n))) {
			neighbors.add(n);
			return true;
		} else return false;
	}

	@Override
	public boolean contains(Node n) { return neighbors.contains(n); }
	
	@Override
	public void pack() { neighbors.trimToSize(); }
}
