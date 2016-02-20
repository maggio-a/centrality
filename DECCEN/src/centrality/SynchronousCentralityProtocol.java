package centrality;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public abstract class SynchronousCentralityProtocol implements CycleBasedTransportSupport<Message> {

	private static class SQEntry implements SendQueueEntry<Message> {
		
		public Message message;
		public CycleBasedTransportSupport<Message> destination;

		public SQEntry(Message m, CycleBasedTransportSupport<Message> d) {
			message = m;
			destination = d;
		}

		@Override
		public Message getMessage() {
			return message;
		}

		@Override
		public CycleBasedTransportSupport<Message> getDestination() {
			return destination;
		}
		
	}
	
	private List<Message> incoming = null;
	private List<SendQueueEntry<Message>> outgoing = null;
	
	public SynchronousCentralityProtocol(String prefix) {
		
	}
	
	@Override
	public Object clone() {
		SynchronousCentralityProtocol smd = null;
		try { 
			smd = (SynchronousCentralityProtocol) super.clone();
		} catch (CloneNotSupportedException e) { e.printStackTrace(); }
		smd.incoming = new LinkedList<Message>();
		smd.outgoing = new LinkedList<SendQueueEntry<Message>>();
		return smd;
	}
	
	public boolean hasOutgoingMessages() {
		return !outgoing.isEmpty();
	}
	
	public void addToSendQueue(Message msg, CycleBasedTransportSupport<Message> destination) {
		outgoing.add(new SQEntry(msg, destination));
	}
	
	@Override
	public void addToIncoming(Message msg) {
		incoming.add(msg);
	}
	
	@Override
	public Iterator<Message> getIncomingMessagesIterator() {
		return incoming.iterator();
	}


	@Override
	public Iterator<SendQueueEntry<Message>> getSendQueueIterator() {
		return outgoing.iterator();
	}
	
	public abstract double getCC();
	
	public abstract double getBC();
	
	public abstract long getSC();

}
