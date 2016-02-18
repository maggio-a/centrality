package centrality;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class SynchronousDispatcher implements Control {
	
	private static final String PAR_PROTOCOL = "protocol";
	private static final String PAR_STATISTICS = "statistics";
	
	private int protocolID;
	private boolean stats;
	private int numMessages;
	
	public SynchronousDispatcher(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		stats = Configuration.getBoolean(prefix + "." + PAR_STATISTICS, false);
		numMessages = 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute() {
		int nm = 0;
		for (int i = 0; i < Network.size(); ++i) {
			Node n = Network.get(i);
			SynchronousTransportLayer<Message> stl = (SynchronousTransportLayer<Message>) n.getProtocol(protocolID);
			if (stl.hasOutgoingMessages()) nm += stl.transferOutgoingMessages(protocolID);
		}
		numMessages += nm;
		if (stats) {
			System.out.println("# Message statistics");
			System.out.println(nm + " messages transfered at the end of this cycle");
			System.out.println(numMessages + " messages transfered since beginning the simulation");
		}
		return false;
	}

}
