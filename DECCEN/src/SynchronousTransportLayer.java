import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import peersim.core.Node;
import peersim.core.Protocol;


public abstract class SynchronousTransportLayer implements Protocol {

	public static class SendQueueItem {
		public final Message message;
		public final Node destination;

		public SendQueueItem(Message m, Node d) {
			message = m;
			destination = d;
		}

		public String toString() {
			return getClass().getName() + "[destinationID=" + destination.getID()
					+ "][message=" + message +"]";
		}
	}
	
	private List<Message> incoming = null;
	private List<SendQueueItem> outgoing = null;
	
	public SynchronousTransportLayer(String prefix) {
		
	}
	
	@Override
	public Object clone() {
		SynchronousTransportLayer smd = null;
		try { 
			smd = (SynchronousTransportLayer) super.clone();
		} catch (CloneNotSupportedException e) { e.printStackTrace(); }
		smd.incoming = new LinkedList<Message>();
		smd.outgoing = new LinkedList<SendQueueItem>();
		return smd;
	}
	
	public void addToSendQueue(Message msg, Node destination) {
		outgoing.add(new SendQueueItem(msg, destination));
	}
	
	public void transferOutgoingMessages(int protocolID) {
		Iterator<SendQueueItem> it = outgoing.iterator();
		while (it.hasNext()) {
			SendQueueItem sqi = it.next();
			SynchronousTransportLayer receiver = (SynchronousTransportLayer) sqi.destination.getProtocol(protocolID);
			receiver.receive(sqi.message);
			it.remove();
		}
	}
	
	private void receive(Message msg) {
		incoming.add(msg);
	}
	
	public Iterator<Message> getIncomingMessageIterator() {
		return incoming.iterator();
	}

}
