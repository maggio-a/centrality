package centrality;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class DeccenInitializer implements Control {
	
	private static String PAR_PROTOCOL = "protocol";
	
	private int protocolID;
	
	public DeccenInitializer(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	}
	
	@Override
	public boolean execute() {
		System.out.println(this.getClass().getName() + ": initializing protocol");
		for (int i = 0; i < Network.size(); ++i) {
			Node n = Network.get(i);
			Deccen sdp = (Deccen) n.getProtocol(protocolID);
			sdp.reset();
			sdp.initCount(n, protocolID);
		}
		
		return false;
	}

}
