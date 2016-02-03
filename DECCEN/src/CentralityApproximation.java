import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;


public class CentralityApproximation extends SynchronousTransportLayer<Message> implements CDProtocol {
	
	private static class BFSNode {
		
		public final Node source;
		public List<Node> predecessors;
		public Set<Node> siblings;
		public boolean done;
		public int distanceFromSource;
		
		public int timestamp;
		
		public int sigma;
		public int deltaSC;
		public double deltaBC;
		
		public BFSNode(Node src, int d, int sig, List<Node> p, int t) {
			source = src;
			distanceFromSource = d;
			sigma = sig;
			predecessors = p;
			timestamp = t;
			siblings = new HashSet<Node>();
			done = false;
			deltaSC = -1;
			deltaBC = -1.0;
		}
	}
	
	private static final String PAR_LINKABLE = "lnk";
	
	private final int linkableProtocolID;
	
	private Map<Node, BFSNode> visits = null;
	
	@Override
	public Object clone() {
		CentralityApproximation ca = (CentralityApproximation) super.clone();
		ca.visits = new HashMap<Node, BFSNode>();
		return ca;
	}
	

	public CentralityApproximation(String prefix) {
		super(prefix);
		linkableProtocolID = Configuration.getPid(prefix + "." + PAR_LINKABLE);
	}

	@Override
	public void nextCycle(Node self, int protocolID) {
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		
		Map<Node, List<Message>> mmap = parseIncomingMessages();
		
		for (Map.Entry<Node, List<Message>> e : mmap.entrySet()) {
			Node source = e.getKey();
			List<Message> messageList = e.getValue();
			if (isClosed(source)) {
				LinkedList<Node> pred = new LinkedList<Node>();
				int sigma = 0;
				int distance = -1;
				for (Message m : messageList) {
					if (distance == -1) distance = m.get(Message.Attachment.SP_LENGTH, Integer.class);
					assert distance == m.get(Message.Attachment.SP_LENGTH, Integer.class) : "distance mismatch";
					sigma += m.get(Message.Attachment.SP_COUNT, Integer.class);
					pred.add(m.get(Message.Attachment.SENDER, Node.class));
				}
				BFSNode state = new BFSNode(source, distance, sigma, pred, CommonState.getIntTime());
				visits.put(source, state);
				for (int i = 0; i < lnk.degree(); ++i) {
					Node n = lnk.getNeighbor(i);
					if (!state.predecessors.contains(n)) {
						addToSendQueue(Message.createProbeMessage(self, source, state.sigma, state.distanceFromSource + 1), n);
					}
				}
			} else if (isOpen(source)) {
				BFSNode state = visits.get(source);
				assert state != null && state.siblings.isEmpty() : "sibling set not empty";
				assert state.timestamp + 1 == CommonState.getIntTime() : "timestamp issue"; 
				for (Message m : messageList) state.siblings.add(m.get(Message.Attachment.SENDER, Node.class));
			}
		}
		
		/* FIXME potetntial issue here: if a neighbor that is a sibling in the bf tree
		 * changed state from closed to open and then at the same cycle from open to done,
		 * this node will NOT have it in the siblings set at this iteration, but only at the next.
		 * implemented fix : check only nodes that were created at least 1 cycle ago
		 */
		for (Node source : visits.keySet()) {
			BFSNode state = visits.get(source);
			if (isOpen(source) && CommonState.getIntTime() - state.timestamp >= 1) {
				boolean allDone = true;
				int deltaSC = 0;
				double deltaBC = 0.0;
				for (int i = 0; i < lnk.degree() && allDone; ++i) {
					Node n = lnk.getNeighbor(i);
					if (state.predecessors.contains(n) || state.siblings.contains(n)) continue;
					else {
						CentralityApproximation ca = (CentralityApproximation) n.getProtocol(protocolID);
						BFSNode childState = ca.visits.get(source);
						if (ca.isDone(source)) {
							assert childState.predecessors.contains(self) : "self not in predecessors list";
							deltaSC += (int) state.sigma * (1 + (childState.deltaSC / (double) childState.sigma));
							deltaBC += (state.sigma / (double) childState.sigma) * (1.0 + childState.deltaBC);
						} else allDone = false;
					}
				}
				if (allDone) {
					state.deltaSC = deltaSC;
					state.deltaBC = deltaBC;
					state.done = true;
					System.out.println(self + " done, predecessors: " + state.predecessors);
					if (state.source == self) System.out.println("Reported back to source");
				}
			}
		}
	}
	
	public void probe(Node self) {
		assert !visits.containsKey(self);
		BFSNode state = new BFSNode(self, 0, 1, new LinkedList<Node>(), CommonState.getIntTime());
		visits.put(self, state);
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		for (int i = 0; i < lnk.degree(); ++i) {
			Node n = lnk.getNeighbor(i);
			if (!state.predecessors.contains(n)) {
				addToSendQueue(Message.createProbeMessage(self, self, state.sigma, state.distanceFromSource + 1), n);
			}
		}
	}
	
	public Map<Node, List<Message>> parseIncomingMessages() {
		Map<Node, List<Message>> map = new HashMap<Node, List<Message>>();
		java.util.Iterator<Message> it = this.getIncomingMessageIterator();
		while (it.hasNext()) {
			Message msg = it.next();
			it.remove();
			Node source = msg.get(Message.Attachment.SOURCE, Node.class);
			List<Message> ls = map.get(source);
			if (ls == null) {
				ls = new LinkedList<Message>();
				map.put(source, ls);
			}
			ls.add(msg);
		}
		return map;
	}
	
	// bfs state of this node wrt source (root of the bf tree) 
	public boolean isClosed(Node source) {
		return visits.containsKey(source) == false;
	}
	
	public boolean isOpen(Node source) {
		return visits.containsKey(source) && visits.get(source).done == false;
	}
	
	public boolean isDone(Node source) {
		return visits.containsKey(source) && visits.get(source).done == true;
	}
	
	// FIXME the probability value should not be here !!!! need to be refactored out, bad encapsulation
	public double getApproximatedBetweennessCentralityValue(Node self, double p) {
		if (p > 1.0 || p <= 0.0) {
			throw new IllegalArgumentException("ILLEGAL p");
		}
		double bc = 0.0;
		for (BFSNode state : visits.values()) {
			if (state.source != self && CommonState.r.nextDouble() <= p) bc += state.deltaBC;
		}
		return bc;
	}
	
	public int getApproximatedStressCentralityValue(Node self, double p) {
		if (p > 1.0 || p <= 0.0) {
			throw new IllegalArgumentException("ILLEGAL p");
		}
		int sc = 0;
		for (BFSNode state : visits.values()) {
			if (state.source != self && CommonState.r.nextDouble() <= p) sc += state.deltaSC;
		}
		return sc;
	}

}
