package centrality;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


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
			Deccen sdp = (Deccen) node.getProtocol(protocolID);
			System.out.printf("%s: SC = %6d, CC = %12.10f, BC = %16.11f \n", node.toString(), sdp.getSC(),
					sdp.getCC(), sdp.getBC());
		}
		System.out.println("##############################################");
		return false;
	}

}
