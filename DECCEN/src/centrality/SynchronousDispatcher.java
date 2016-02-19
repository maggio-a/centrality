package centrality;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class SynchronousDispatcher implements Control {
	
	private static final String PAR_PROTOCOL = "protocol";
	private static final String PAR_STATISTICS = "printStatistics";
	private static final String PAR_TERMINATE = "terminateOnEmptyQueues";
	
	private int protocolID;
	private boolean stats;
	private boolean terminate;
	private int numMessages;
	
	public SynchronousDispatcher(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		stats = Configuration.contains(prefix + "." + PAR_STATISTICS);
		terminate = Configuration.contains(prefix + "." + PAR_TERMINATE);
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
		if (terminate && nm == 0) {
			System.out.println(getClass().getName() + ": no message to transfer, stopping the simulation.");
			return true;
		}
		return false;
	}

}
