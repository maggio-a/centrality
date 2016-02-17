package centrality;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;


public class CentralityApproximation extends SynchronousTransportLayer<Message> implements CDProtocol {
	
	private static class BFSNode {
		
		public final Node source;
		public Set<Node> predecessors;
		public Set<Node> siblings;
		public boolean done;
		public int distanceFromSource;
		
		public int timestamp;
		
		public int sigma;
		public int deltaSC;
		public double deltaBC;
		
		public BFSNode(Node src, int d, int sig, Set<Node> p, int t) {
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
	private static final String PAR_IGNORE_CORRECT_ESTIMATE = "ignoreCorrectEstimate";
	
	private final int linkableProtocolID;
	private final boolean ignoreCorrectEstimate;
	
	private Map<Node, BFSNode> visits;
	private double betweenness;
	private int stress;
	private int closenessSum;
	private int numSamples;
	
	public CentralityApproximation(String prefix) {
		super(prefix);
		linkableProtocolID = Configuration.getPid(prefix + "." + PAR_LINKABLE);
		ignoreCorrectEstimate = Configuration.getBoolean(prefix + "." + PAR_IGNORE_CORRECT_ESTIMATE);
	}
	
	protected void reset() {
		visits = new HashMap<Node, BFSNode>();
		betweenness = 0.0;
		stress = 0;
		closenessSum = 0;
		numSamples = 0;
	}
	
	@Override
	public Object clone() {
		CentralityApproximation ca = (CentralityApproximation) super.clone();
		ca.reset();
		return ca;
	}
	
	@Override
	public void nextCycle(Node self, int protocolID) {
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		
		Map<Node, List<Message>> mmap = parseIncomingMessages();
		
		for (Map.Entry<Node, List<Message>> e : mmap.entrySet()) {
			Node source = e.getKey();
			List<Message> messageList = e.getValue();
			if (isClosed(source)) {
				Set<Node> pred = new HashSet<Node>();
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
					numSamples++;
					
					if (source != self) {
						closenessSum += state.distanceFromSource;
						stress += deltaSC;
						betweenness += deltaBC;
						//System.out.println(self + " done, predecessors: " + state.predecessors);
						//if (state.source == self) System.out.println("Reported back to source");
					}
				}
			}
		}
		
		Iterator<Map.Entry<Node, BFSNode>> vit = visits.entrySet().iterator();
		while (vit.hasNext()) {
			Map.Entry<Node, BFSNode> entry = vit.next();
			if (entry.getKey() != self) {
				BFSNode state = entry.getValue();
				Iterator<Node> pit = state.predecessors.iterator();
				boolean predecessorsDone = true;
				while (predecessorsDone && pit.hasNext()) {
					Node predecessor = pit.next();
					CentralityApproximation ca = (CentralityApproximation) predecessor.getProtocol(protocolID);
					if (!ca.isDone(state.source)) predecessorsDone = false;
				}
				if (predecessorsDone) vit.remove();
			}
		}
	}
	
	public void initAccumulation(Node self) {
		if (visits.containsKey(self)) {
			throw new IllegalStateException("Protocol already initiated accumulation");
		}
		BFSNode state = new BFSNode(self, 0, 1, new HashSet<Node>(0), CommonState.getIntTime());
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
	
	public int parseIncomingMessages2(Map<Node, List<Message>> map ) {
		java.util.Iterator<Message> it = this.getIncomingMessageIterator();
		int c = 0;
		while (it.hasNext()) {
			c++;
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
		//System.out.println(c + " messages received");
		return c;
	}
	
	public int getSC() { 
		return ignoreCorrectEstimate ? stress : (int) ((Network.size()/(double)numSamples)*stress);
	}
	
	public double getBC() {
		return ignoreCorrectEstimate ? betweenness : ((Network.size()/(double)numSamples)*betweenness);
	}
	
	public double getCC() {
		if (closenessSum == 0) return 0.0;
		else return ignoreCorrectEstimate ? (1.0 / closenessSum) : (1.0 / ((Network.size()/(double)numSamples)*closenessSum)); 
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
}
