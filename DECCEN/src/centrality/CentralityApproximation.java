package centrality;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import centrality.Message.Attachment;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;


public class CentralityApproximation extends SynchronousCentralityProtocol implements CDProtocol {
		
	private static class VisitState {
		
		public Set<Node> predecessors;
		public Set<Node> siblings;
		public Set<Node> children;
		public int distanceFromSource;
		
		public int timestamp;
		
		public int sigma;
		public long stressContribution;
		public double betweennessContribution;
		
		public VisitState(int d, int sig, Set<Node> p, int t) {
			distanceFromSource = d;
			sigma = sig;
			predecessors = p;
			timestamp = t;
			siblings = new HashSet<Node>();
			children = new HashSet<Node>();
			stressContribution = 0;
			betweennessContribution = 0.0;
		}
		
		public void accumulate(Node child, double bcc, long scc, int numSP) {
			children.add(child);
			stressContribution += (long) (sigma * (1 + (scc / (double) numSP))); // can safely cast to integer
			betweennessContribution += (sigma / (double) numSP) * (1.0 + bcc);
		}
	}
	
	private static final String PAR_LINKABLE = "lnk";
	private static final String PAR_IGNORE_CORRECT_ESTIMATE = "ignoreCorrectEstimate";
	
	private final int linkableProtocolID;
	private final boolean ignoreCorrectEstimate;
	
	private Map<Node, VisitState> visits;
	private double betweenness;
	private long stress;
	private int closenessSum;
	private int numSamples;
	
	public CentralityApproximation(String prefix) {
		super(prefix);
		linkableProtocolID = Configuration.getPid(prefix + "." + PAR_LINKABLE);
		ignoreCorrectEstimate = Configuration.getBoolean(prefix + "." + PAR_IGNORE_CORRECT_ESTIMATE);
		reset();
	}
	
	protected void reset() {
		visits = new HashMap<Node, VisitState>();
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
		
		Map<Message.Type, List<Message>> mmap = parseIncomingMessages();
		
		if (mmap.containsKey(Message.Type.PROBE)) {
			Map<Node, List<Message>> mbs = groupBySource(mmap.get(Message.Type.PROBE));
			for (Map.Entry<Node, List<Message>> e : mbs.entrySet()) {
				Node s = e.getKey();
				if (isWaiting(s)) {
					Set<Node> predecessors = new HashSet<Node>();
					int sigma = 0;
					int distance = -1;
					for (Message m : e.getValue()) {
						if (distance == -1) distance = m.get(Message.Attachment.SP_LENGTH, Integer.class);
						assert distance == m.get(Message.Attachment.SP_LENGTH, Integer.class) : "distance mismatch";
						sigma += m.get(Message.Attachment.SP_COUNT, Integer.class);
						predecessors.add(m.get(Message.Attachment.SENDER, Node.class));
					}
					VisitState state = new VisitState(distance + 1, sigma, predecessors, CommonState.getIntTime());
					visits.put(s, state);
					for (int i = 0; i < lnk.degree(); ++i) {
						Node n = lnk.getNeighbor(i);
						if (!state.predecessors.contains(n)) {
							CentralityApproximation next = (CentralityApproximation) n.getProtocol(protocolID);
							addToSendQueue(Message.createProbeMessage(self, s, state.sigma, state.distanceFromSource), next);
						}
					}
				} else if (isActive(s)) {
					VisitState state = visits.get(s);
					assert state != null && state.siblings.isEmpty() : "sibling set not empty";
					assert state.timestamp + 1 == CommonState.getIntTime() : "timestamp issue"; 
					for (Message m : e.getValue()) state.siblings.add(m.get(Message.Attachment.SENDER, Node.class));
				}
			}
		}
		
		if (mmap.containsKey(Message.Type.CONTRIBUTION_REPORT)) {
			Map<Node, List<Message>> mbs = groupBySource(mmap.get(Message.Type.CONTRIBUTION_REPORT));
			for (Map.Entry<Node, List<Message>> e : mbs.entrySet()) {
				Node s = e.getKey();
				if (isActive(s)) {
					VisitState state = visits.get(s);
					for (Message m : e.getValue()) {
						Node child = m.get(Attachment.SENDER, Node.class);
						double bcc = m.get(Attachment.BETWEENNESS_CONTRIBUTION, Double.class);
						long scc = m.get(Attachment.STRESS_CONTRIBUTION, Long.class);
						int spc = m.get(Attachment.SP_COUNT, Integer.class);
						state.accumulate(child, bcc, scc, spc);
					}
 				} 
			}
		}
		
		// check for reportable nodes
		for (Map.Entry<Node, VisitState> e : visits.entrySet()) {
			Node s = e.getKey();
			VisitState state = e.getValue();
			if (isActive(s)) {
				boolean canReport = true;
				for (int i = 0; i < lnk.degree() && canReport; ++i) {
					Node n = lnk.getNeighbor(i);
					if (!(state.predecessors.contains(n) || state.siblings.contains(n) || state.children.contains(n)))
						canReport = false;
				}
				if (canReport) {
					numSamples++;
					if (s != self) {
						for (Node p : state.predecessors) {
							CentralityApproximation predecessor = (CentralityApproximation) p.getProtocol(protocolID);
							addToSendQueue(Message.createContributionReportMessage(
									self, s, state.betweennessContribution, state.stressContribution, state.sigma),
									predecessor);
						}
						closenessSum += state.distanceFromSource;
						betweenness += state.betweennessContribution;
						stress += state.stressContribution;
					}
					e.setValue(null);
				}
			}
		}
	}
	
	private Map<Node, List<Message>> groupBySource(List<Message> list) {
		Map<Node, List<Message>> msgBySource= new HashMap<Node, List<Message>>();
		for (Message m : list) {
			Node s = m.get(Attachment.SOURCE, Node.class);
			List<Message> ml = msgBySource.get(s);
			if (ml == null) {
				ml = new LinkedList<Message>();
				msgBySource.put(s, ml);
			}
			ml.add(m);
		}
		return msgBySource;
	}
	
	private Map<Message.Type, List<Message>> parseIncomingMessages() {
		Map<Message.Type, List<Message>> map = new HashMap<Message.Type, List<Message>>();
		java.util.Iterator<Message> it = this.getIncomingMessagesIterator();
		while (it.hasNext()) {
			Message m = it.next();
			it.remove();
			List<Message> ml = map.get(m.type);
			if (ml == null) {
				ml = new LinkedList<Message>();
				map.put(m.type, ml);
			}
			ml.add(m);
		}
		return map;
	}
	
	public void initAccumulation(Node self, int pid) {
		if (visits.containsKey(self)) {
			throw new IllegalStateException("Protocol already initiated accumulation");
		}
		VisitState state = new VisitState(0, 1, new HashSet<Node>(0), CommonState.getIntTime());
		visits.put(self, state);
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		for (int i = 0; i < lnk.degree(); ++i) {
			Node n = lnk.getNeighbor(i);
			if (!state.predecessors.contains(n)) {
				CentralityApproximation next = (CentralityApproximation) n.getProtocol(pid);
				addToSendQueue(Message.createProbeMessage(self, self, state.sigma, state.distanceFromSource), next);
			}
		}
	}
	
	public long getSC() { 
		return ignoreCorrectEstimate ? stress : (long) ((Network.size()/(double)numSamples)*stress);
	}
	
	public double getBC() {
		return ignoreCorrectEstimate ? betweenness : ((Network.size()/(double)numSamples)*betweenness);
	}
	
	public double getCC() {
		if (closenessSum == 0) return 0.0;
		else return ignoreCorrectEstimate ? (1.0 / closenessSum) : (1.0 / ((Network.size()/(double)numSamples)*closenessSum)); 
	}
	
	// bfs state of this node wrt source (root of the bf tree) 
	public boolean isWaiting(Node source) {
		return visits.containsKey(source) == false;
	}
	
	public boolean isActive(Node source) {
		return visits.containsKey(source) && visits.get(source) != null;
	}
	
	public boolean isCompleted(Node source) {
		return visits.containsKey(source) && visits.get(source) == null;
	}
}
