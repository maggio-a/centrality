package centrality;
import java.util.Objects;

import peersim.core.GeneralNode;


public class MyNode extends GeneralNode {
	
	private String label;

	public MyNode(String prefix) {
		super(prefix);
		label = "DEFAULT_LABEL";
	}
	
	public void setLabel(String l) {
		label = l;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String toString() {
		return getLabel() + "[ID=" + getID() + "]";
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (getClass() == o.getClass()) {
			MyNode n = (MyNode) o;
			if (Objects.equals(getID(), n.getID())) return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getID());
	}

}
