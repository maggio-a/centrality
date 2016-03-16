/*
 * Peer-to-Peer Systems 2015/2016
 * 
 * Final project source code
 * 
 * Author: Andrea Maggiordomo - mggndr89@gmail.com
 */
package centrality;

import java.util.Iterator;

import centrality.CycleBasedTransportSupport.SendQueueEntry;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;


public class CycleBasedTransport implements Control {
	
	/** The {@link CycleBasedTransportSupport} protocol this transport operates on. */
	private static final String PAR_PROTOCOL = "protocol";
	
	/** If defined, this control will print statistics about the messages transfered. */
	private static final String PAR_STATISTICS = "printStatistics";
	
	/** If defined, this control stops the simulation if no messages are sent during a cycle. */
	private static final String PAR_TERMINATE = "terminateOnEmptyQueues";
	
	private int protocolID;
	private boolean stats;
	private boolean terminate;
	private int numMessages;
	
	public CycleBasedTransport(String prefix) {
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
			CycleBasedTransportSupport<Message> tr = (CycleBasedTransportSupport<Message>) n.getProtocol(protocolID);
			Iterator<SendQueueEntry<Message>> it = tr.getSendQueueIterator();
			while (it.hasNext()) {
				SendQueueEntry<Message> e = it.next();
				it.remove();
				e.getDestination().addToIncoming(e.getMessage());
				nm++;
			}
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
