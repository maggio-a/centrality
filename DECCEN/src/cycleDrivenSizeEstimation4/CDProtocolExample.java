package cycleDrivenSizeEstimation4;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;

public class CDProtocolExample implements CDProtocol
{
	private int _linkableProtocolId;
	private int cacheCapacity;
	
	private boolean beginCount;
	private boolean counting;
	private Deque<P2PMessage> msgQueue;
	private HashMap<Long,MessageAggregator> msgCache;

	public CDProtocolExample(final String prefix_)
	{
		_linkableProtocolId = Configuration.getPid(prefix_ + ".linkable");
		cacheCapacity = Math.max(Configuration.getInt(prefix_ + "." + "cacheCapacity"), 10);
		
		beginCount = false;
		counting = false;
		msgQueue = new LinkedList<P2PMessage>();
		msgCache = new HashMap<Long, MessageAggregator>();
	}

	@Override
	public Object clone()
	{
		CDProtocolExample ip = null;
		try {
			ip = (CDProtocolExample) super.clone();
		}
		catch (final CloneNotSupportedException e) {
			System.out.println("HELP");
		}
		
		ip._linkableProtocolId = _linkableProtocolId;
		ip.cacheCapacity = cacheCapacity;

		ip.beginCount = false;
		ip.counting = false;
		ip.msgQueue = new LinkedList<P2PMessage>();
		ip.msgCache = new HashMap<Long,MessageAggregator>();
		
		return ip;
	}
	
	public void startCounting() {
		beginCount = true;
	}
	
	public void stopCounting() {
		beginCount = false;
	}
	
	private boolean hasToCount() {
		return beginCount;
	}
	
	public boolean counting() {
		return counting || hasToCount();
	}
	
	//FIXME need to find a way to get the id of the receiver
	public void respond(long msgId, Node sender, int value) {
		if (msgCache.containsKey(msgId)) {
			msgCache.get(msgId).setRepliedValue(sender, value);
		} else {
			//System.err.println("Warning: Message ID " + msgId + " not in the cache. Lost reply from node " +
			//				   sender.getID() + " with value " + value + ".");
		}
	}
	
	public void addToMessageQueue(P2PMessage msg) {
		msgQueue.addLast(msg);
	}
	
	private void printData(P2PMessage msg, long nodeId, int value) {
		System.out.print("[Node " + nodeId + "] MsgID " + msg.getId()  + ": " + value + " peers counted. ");
		System.out.print("Actual network size: " + Network.size() + ". ");
		double err = Math.abs(value - Network.size()) / (double) Network.size();
		System.out.println("Relative error: " + err + ".");
	}
	
	@Override
	public void nextCycle(final Node node_, final int protocolID)
	{
		final LinkableExample linkableProtocol = ((LinkableExample) node_.getProtocol(_linkableProtocolId));
		
		if (!counting && hasToCount()) { // do only if no counting process is already active
			counting = true;
			P2PMessage msg = P2PMessage.getNewMessage(this);
			MessageAggregator ma = new MessageAggregator(msg);
			
			for (int i = 0; i < linkableProtocol.degree(); i++) {
				Node neighbor = linkableProtocol.getNeighbor(i);
				
				if (!neighbor.isUp()) continue; // We can skip this peer since it's down
				                                // and won't be up again (dynamic network)
					
				CDProtocolExample neighborCdProtocol = (CDProtocolExample) neighbor.getProtocol(protocolID);
				neighborCdProtocol.addToMessageQueue(msg);
				ma.addCallbackNode(neighbor);
			}
			
			msgCache.put(msg.getId(), ma);			
			stopCounting();
		}
		
		// Process pending messages from the queue
		while (!msgQueue.isEmpty()) {
			P2PMessage incoming = msgQueue.removeFirst();
			
			if (msgCache.containsKey(incoming.getId())) {
				// Message was already received, respond with 0
				CDProtocolExample source = incoming.getSource();
				source.respond(incoming.getId(), node_, 0);
			} else {
				// Set up a MessageAggregator and forward the request to the neighbors
				P2PMessage msg = P2PMessage.getForwardMessage(incoming, this);
				MessageAggregator ma = new MessageAggregator(incoming);
			
				for (int i = 0; i < linkableProtocol.degree(); i++) {
					Node neighbor = linkableProtocol.getNeighbor(i);
					
					if (!neighbor.isUp()) continue; // skip
					
					CDProtocolExample neighborCdProtocol = (CDProtocolExample) neighbor.getProtocol(protocolID);
					
					if (neighborCdProtocol != incoming.getSource()) {
						neighborCdProtocol.addToMessageQueue(msg);
						ma.addCallbackNode(neighbor);
					}
				}
				
				msgCache.put(incoming.getId(), ma);
			}
		}
		
		// Check if any result is available
		for (MessageAggregator ma : msgCache.values()) {
			if (ma.active() && ma.hasReceivedEveryReply()) {
				P2PMessage msg = ma.getMessage();
				CDProtocolExample source = msg.getSource();
				int value = ma.getAggregateValue() + 1; // +1 for "this" node
				
				if (source == this) {
					// We initiated the count, so we output the value obtained
					printData(msg, node_.getID(), value);
					counting = false;
				} else {
					// Someone else queried us, we respond
					source.respond(msg.getId(), node_, value);
				}
				
				ma.setActiveFlag(false);
			}
		}
		
		// Manage the cache
		while (msgCache.size() >= cacheCapacity) {
			Iterator<MessageAggregator> it = msgCache.values().iterator();
			MessageAggregator old = it.next();
			
			while (it.hasNext()) {
				MessageAggregator current = it.next();
				if (current.getCreationTime() < old.getCreationTime())
					old = current;
			}

			P2PMessage msg = old.getMessage();
			msgCache.remove(msg.getId());
			
			if (old.active()) {
				// We must reply with what we can get
				CDProtocolExample source = msg.getSource();
				int value = old.getAggregateValue() + 1;
				if (source == this) {
					printData(msg, node_.getID(), value);
					counting = false;
				} else {
					source.respond(msg.getId(), node_, value);
				}
				//System.err.println("[Node " + node_.getID() + "] Warning: dropped message " +
				//                   msg.getId() + " from the cache.");
			}
		}
	}
	
	/*
	 * MessageAggregator
	 * Helper class that keeps track of the values replied by nodes to which a count request has been sent
	 */
	private static class MessageAggregator {
		private boolean active;
		private P2PMessage msg;
		Map<Node, Integer> replies;
		Long creationTime;
		
		public MessageAggregator(P2PMessage msg) {
			this.msg = msg;
			this.replies = new HashMap<Node, Integer>();
			this.active = true;
			this.creationTime = CommonState.getTime();
		}
		
		public Long getCreationTime() {
			return creationTime;
		}

		public void addCallbackNode(Node node) {
			replies.put(node, -1);
		}
		
		public void setRepliedValue(Node node, Integer value) {
			replies.put(node, value);
		}
		
		public boolean active() {
			return active;
		}
		
		public void setActiveFlag(boolean val) {
			active = val;
		}
		
		public P2PMessage getMessage() {
			return msg;
		}
		
		/*
		 * Returns true if all the (alive) nodes we contacted have replied
		 */
		public boolean hasReceivedEveryReply() {
			boolean ready = true;
			
			for (Map.Entry<Node, Integer> e : replies.entrySet()) {
				//either we received a reply or the node went down
				ready &= (e.getValue() > -1) || (e.getKey().isUp() == false);
				if (!ready) break;
			}
			
			return ready;
		}
		
		public int getAggregateValue() {
			int val = 0;
			
			for (Map.Entry<Node, Integer> e : replies.entrySet()) {
				// We go with what we have if the caller didn't check that hasReceivedEveryReply()
				val += Math.max(0, e.getValue());
			}
			
			return val;
		}
	}

}