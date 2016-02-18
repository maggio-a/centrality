package centrality;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import peersim.core.Node;
import peersim.core.Protocol;

//FIXME interface
public abstract class SynchronousTransportLayer<T> implements Protocol {

	private static class SendQueueItem<T> {
		public final T message;
		public final Node destination;

		public SendQueueItem(T m, Node d) {
			message = m;
			destination = d;
		}

		public String toString() {
			return getClass().getName() + "[destinationID=" + destination.getID()
					+ "][message=" + message +"]";
		}
	}
	
	private List<T> incoming = null;
	private List<SendQueueItem<T>> outgoing = null;
	
	public SynchronousTransportLayer(String prefix) {
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		SynchronousTransportLayer<T> smd = null;
		try { 
			smd = (SynchronousTransportLayer<T>) super.clone();
		} catch (CloneNotSupportedException e) { e.printStackTrace(); }
		smd.incoming = new LinkedList<T>();
		smd.outgoing = new LinkedList<SendQueueItem<T>>();
		return smd;
	}
	
	public boolean hasOutgoingMessages() {
		return !outgoing.isEmpty();
	}
	
	public void addToSendQueue(T msg, Node destination) {
		outgoing.add(new SendQueueItem<T>(msg, destination));
	}
	
	@SuppressWarnings("unchecked")
	public int transferOutgoingMessages(int protocolID) {
		int n = 0;
		Iterator<SendQueueItem<T>> it = outgoing.iterator();
		while (it.hasNext()) {
			SendQueueItem<T> sqi = it.next();
			SynchronousTransportLayer<T> receiver = (SynchronousTransportLayer<T>) sqi.destination.getProtocol(protocolID);
			receiver.receive(sqi.message);
			n++;
			it.remove();
		}
		return n;
	}
	
	private void receive(T msg) {
		incoming.add(msg);
	}
	
	public Iterator<T> getIncomingMessageIterator() {
		return incoming.iterator();
	}

}
