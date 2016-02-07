import java.util.HashSet;
import java.util.Set;

import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class CAInitializer implements Control {
	
	private static String PAR_PROTOCOL = "protocol";
	private static String PAR_FRACTION = "fraction";
	
	private static double defaultFraction = 1.0;
	
	private static Set<Node> sources = new HashSet<Node>();
	
	public static void addSource(Node s) { sources.add(s); }
	public static Set<Node> getSources() { return sources; }
	
	private int protocolID;
	private double fraction;
	
	public CAInitializer(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		fraction = Configuration.getDouble(prefix + "." + PAR_FRACTION, defaultFraction);
		if (fraction < 0.0 || fraction > 1.0)
			throw new IllegalParameterException(prefix + "." + PAR_FRACTION, "out of allowed interval (0.0, 1.0]");
		
	}
	
	@Override
	public boolean execute() {
		int c = 0;
		for (int i = 0; i < Network.size(); ++i) {
			Node node = Network.get(i);
			if (CommonState.r.nextDouble() <= fraction) {
				CentralityApproximation ca = (CentralityApproximation) node.getProtocol(protocolID);
				ca.initAccumulation(node);
				addSource(node);
				c++;
			}
		}
		System.out.println(getClass().getName() + ": accumulating from " + c + " sources");
		return false;
	}

}
