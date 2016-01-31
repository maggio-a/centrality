import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class SynchronousDispatcher implements Control {
	
	private static final String PAR_PROTOCOL = "protocol";
	
	private int protocolID;
	
	public SynchronousDispatcher(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	}

	@Override
	public boolean execute() {
		for (int i = 0; i < Network.size(); ++i) {
			Node n = Network.get(i);
			SynchronousTransportLayer stl = (SynchronousTransportLayer) n.getProtocol(protocolID);
			if (stl.hasOutgoingMessages()) {
				System.out.println("transfering messages from " + n);
				stl.transferOutgoingMessages(protocolID);
			}
		}
		return false;
	}

}
