import java.util.Set;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class CAObserver implements Control {
	
	public static String PAR_PROTOCOL = "protocol";
	public static String PAR_PRINT = "printIndices";
	
	private int protocolID;
	private boolean printIndices;
	
	public CAObserver(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		printIndices = Configuration.contains(prefix + "." + PAR_PRINT);
	}

	@Override
	public boolean execute() {
		if (printIndices) {
			System.out.println("## Approximated Betweenness Centrality indices at time " + CommonState.getIntTime() + " ####");
			for (int i = 0; i < Network.size(); ++i) {
				Node node = Network.get(i);
				CentralityApproximation ca = (CentralityApproximation) node.getProtocol(protocolID);
				String s = "%12s: SC = %6d, BC = %16.11f\n";
				System.out.printf(s, node.toString(), ca.getApproximatedStressCentralityValue(node),
					ca.getApproximatedBetweennessCentralityValue(node));
			}
			System.out.println("##############################################");
		}
		int c = 0;
		Set<Node> sources = CAInitializer.getSources();
		for (Node s : sources) {
			CentralityApproximation ca = (CentralityApproximation) s.getProtocol(protocolID);
			if (ca.isDone(s)) c++;
		}
		if (c == sources.size()) {
			System.out.println("All sources completed the accumulation, stopping the simulation");
			return true;
		} else {
			System.out.println(c + " out of " + sources.size() + " sources completed the accumulation");
			return false;
		}
	}

}
