package centrality;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 *  TODO report on the different kinds of centrality computed by the protcol
 * @author Andrea
 *
 */
public class CentralityObserver implements Control {
	
	public static String PAR_PROTOCOL = "protocol";
	
	private int protocolID;
	
	public CentralityObserver(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	}

	@Override
	public boolean execute() {
		System.out.println("## Centrality indices at time " + CommonState.getTime() + " ####");
		for (int i = 0; i < Network.size(); ++i) {
			Node node = Network.get(i);
			SynchronousDeccenProtocol sdp = (SynchronousDeccenProtocol) node.getProtocol(protocolID);
			//System.out.println(node + ": SC = " + sdp.getStressCentralityValue()
			//		+ ", CC = " + sdp.getClosenessCentralityValue()
			//		+ ", BC = " + sdp.getBetweennessCentralityValue());
			System.out.printf("%s: SC = %6d, CC = %12.10f, BC = %16.11f \n", node.toString(), sdp.getStressCentralityValue(),
					sdp.getClosenessCentralityValue(), sdp.getBetweennessCentralityValue());
		}
		System.out.println("##############################################");
		return false;
	}

}
