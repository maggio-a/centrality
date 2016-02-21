package centrality;
import java.util.HashSet;
import java.util.Set;

import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class ApproximationInitializer implements Control {
	
	private static final String PAR_PROTOCOL = "protocol";
	private static final String PAR_FRACTION = "fraction";
	
	private static double defaultFraction = 1.0;
	
	private static Set<Node> sources = null;
	
	public static void addSource(Node s) { sources.add(s); }
	public static Set<Node> getSources() { return sources; }
	
	private int protocolID;
	private double fraction;
	
	public ApproximationInitializer(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		fraction = Configuration.getDouble(prefix + "." + PAR_FRACTION, defaultFraction);
		if (fraction <= 0.0 || fraction > 1.0)
			throw new IllegalParameterException(prefix + "." + PAR_FRACTION, "out of allowed interval (0.0, 1.0]");
		
	}
	
	@Override
	public boolean execute() {
		sources = new HashSet<Node>();
		for (int i = 0; i < Network.size(); ++i) {
			Node node = Network.get(i);
			CentralityApproximation ca = (CentralityApproximation) node.getProtocol(protocolID);
			ca.reset();
			if (CommonState.r.nextDouble() <= fraction) addSource(node);
		}
		System.err.println(getClass().getName() + ": accumulating from " + sources.size() + " sources");
		return false;
	}

}
