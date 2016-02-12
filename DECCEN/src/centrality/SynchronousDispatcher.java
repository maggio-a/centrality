package centrality;
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

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() {
		for (int i = 0; i < Network.size(); ++i) {
			Node n = Network.get(i);
			SynchronousTransportLayer<Message> stl = (SynchronousTransportLayer<Message>) n.getProtocol(protocolID);
			if (stl.hasOutgoingMessages()) stl.transferOutgoingMessages(protocolID);
		}
		return false;
	}

}
