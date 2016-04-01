/*
 * Peer-to-Peer Systems 2015/2016
 * 
 * Final project source code
 * 
 * Author: Andrea Maggiordomo - mggndr89@gmail.com
 */
package centrality;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;


public class MultiBFS extends SynchronousCentralityProtocol {
	private static class VisitState {
		public Set<Node> predecessors;
		public Set<Node> siblings;
		public Set<Node> children;
		public int distanceFromSource;
		public int timestamp;
		public int sigma;
		public long contributionSC;
		public double contributionBC;
		
		public VisitState(int d, int sig, Set<Node> p, int t) {
			distanceFromSource = d;
			sigma = sig;
			predecessors = p;
			timestamp = t;
			siblings = new HashSet<Node>();
			children = new HashSet<Node>();
			contributionSC = 0;
			contributionBC = 0.0;
		}
		
		public void accumulate(Node child, double bcc, long scc, int numSP) {
			children.add(child);
			contributionSC += (long) (sigma * (1 + (scc / (double) numSP))); // can safely cast to integer
			contributionBC += (sigma / (double) numSP) * (1.0 + bcc);
		}
	}
	
	private static final String PAR_LINKABLE = "lnk";
	private static final String PAR_IGNORE_CORRECT_ESTIMATE = "ignoreCorrectEstimate";
	
	private final int linkableProtocolID;
	private final boolean ignoreCorrectEstimate;
	
	private Map<Node,VisitState> activeVisits;
	private Set<Node> completed;
	private double accumulatorBC;
	private long accumulatorSC;
	private int accumulatorCC;
	private int numSamples;
	
	public MultiBFS(String prefix) {
		super(prefix);
		linkableProtocolID = Configuration.getPid(prefix + "." + PAR_LINKABLE);
		ignoreCorrectEstimate = Configuration.getBoolean(prefix + "." + PAR_IGNORE_CORRECT_ESTIMATE);
		reset();
	}
	
	protected void reset() {
		activeVisits = new HashMap<Node,VisitState>();
		completed = new HashSet<Node>();
		accumulatorBC = 0.0;
		accumulatorSC = 0;
		accumulatorCC = 0;
		numSamples = 0;
	}
	
	@Override
	public Object clone() {
		MultiBFS mbfs = (MultiBFS) super.clone();
		mbfs.reset();
		return mbfs;
	}
	
	public void startAccumulation(Node self, int pid) {
		if (activeVisits.containsKey(self)) {
			throw new IllegalStateException("Protocol already started an accumulation");
		}
		VisitState state = new VisitState(0, 1, new HashSet<Node>(0), CommonState.getIntTime());
		activeVisits.put(self, state);
		
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		for (int i = 0; i < lnk.degree(); ++i) {
			Node n = lnk.getNeighbor(i);
			if (!state.predecessors.contains(n)) {
				MultiBFS next = (MultiBFS) n.getProtocol(pid);
				addToSendQueue(Message.createDiscoveryMessage(self, self, state.sigma, state.distanceFromSource), next);
			}
		}
	}
	
	@Override
	public void nextCycle(Node self, int protocolID) {
		Map<Node,List<Message>> discoveryMap = new HashMap<Node,List<Message>>();
		List<Message> reportList = new LinkedList<Message>();
		parseIncomingMessages(discoveryMap, reportList);
		if (!discoveryMap.isEmpty()) processDiscoveryMessages(self, protocolID, discoveryMap);
		if (!reportList.isEmpty()) processReportMessages(reportList);
		reportNewCompleted(self, protocolID);
	}
	
	private void parseIncomingMessages(Map<Node,List<Message>> discoveryMap, List<Message> reportList) {
		Iterator<Message> it = getIncomingMessagesIterator();
		while (it.hasNext()) {
			Message m = it.next();
			if (m.type == Message.Type.DISCOVERY) {
				Node s = m.get(Message.Field.SOURCE, Node.class);
				List<Message> ml = discoveryMap.get(s);
				if (ml == null) {
					ml = new LinkedList<Message>();
					discoveryMap.put(s, ml);
				}
				ml.add(m);
			} else if (m.type == Message.Type.MBFS_REPORT) {
				reportList.add(m);
			}
			it.remove();
		}
	}
	
	private void processDiscoveryMessages(Node self, int pid, Map<Node, List<Message>> messageMap) {
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		for (Map.Entry<Node,List<Message>> e : messageMap.entrySet()) {
			Node s = e.getKey();
			if (isWaiting(s)) {
				Set<Node> predecessors = new HashSet<Node>();
				int sigma = 0;
				int distance = -1;
				for (Message m : e.getValue()) {
					assert m.type == Message.Type.DISCOVERY;
					if (distance == -1) distance = m.get(Message.Field.SP_LENGTH, Integer.class);
					assert distance == m.get(Message.Field.SP_LENGTH, Integer.class) : "distance mismatch";
					sigma += m.get(Message.Field.SP_COUNT, Integer.class);
					predecessors.add(m.get(Message.Field.SENDER, Node.class));
				}
				VisitState state = new VisitState(distance + 1, sigma, predecessors, CommonState.getIntTime());
				activeVisits.put(s, state);
				
				for (int i = 0; i < lnk.degree(); ++i) {
					Node n = lnk.getNeighbor(i);
					if (!state.predecessors.contains(n)) {
						MultiBFS next = (MultiBFS) n.getProtocol(pid);
						addToSendQueue(Message.createDiscoveryMessage(self, s, state.sigma, state.distanceFromSource), next);
					}
				}
			} else if (isActive(s)) {
				VisitState state = activeVisits.get(s);
				assert state != null && state.siblings.isEmpty() : "sibling set not empty";
				assert state.timestamp + 1 == CommonState.getIntTime() : "timestamp issue"; 
				for (Message m : e.getValue()) state.siblings.add(m.get(Message.Field.SENDER, Node.class));
			}
		}
	}
	
	private void processReportMessages(List<Message> reportList) {
		for (Message m : reportList) {
			assert m.type == Message.Type.MBFS_REPORT;
			Node s = m.get(Message.Field.SOURCE, Node.class);
			if (isActive(s)) {
				VisitState state = activeVisits.get(s);
				Node child = m.get(Message.Field.SENDER, Node.class);
				double bcc = m.get(Message.Field.BC_CONTRIBUTION, Double.class);
				long scc = m.get(Message.Field.SC_CONTRIBUTION, Long.class);
				int spc = m.get(Message.Field.SP_COUNT, Integer.class);
				state.accumulate(child, bcc, scc, spc);
			}
		}
	}
	
	private void reportNewCompleted(Node self, int pid) {
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		Iterator<Map.Entry<Node,VisitState>> it = activeVisits.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Node, VisitState> e = it.next();
			Node s = e.getKey();
			VisitState state = e.getValue();
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
						MultiBFS predecessor = (MultiBFS) p.getProtocol(pid);
						addToSendQueue(Message.createMBFSReportMessage(
								self, s, state.contributionBC, state.contributionSC, state.sigma),
								predecessor);
					}
					accumulatorCC += state.distanceFromSource;
					accumulatorBC += state.contributionBC;
					accumulatorSC += state.contributionSC;
				}
				it.remove();
				completed.add(s);
			}
		}
	}
	
	public boolean isWaiting(Node source) {
		return activeVisits.containsKey(source) == false && completed.contains(source) == false;
	}
	
	public boolean isActive(Node source) {
		return activeVisits.containsKey(source);
	}
	
	public boolean isCompleted(Node source) {
		return completed.contains(source);
	}
	
	@Override
	public long getSC() { 
		return ignoreCorrectEstimate ? accumulatorSC : (long) ((Network.size()/(double)numSamples)*accumulatorSC);
	}
	
	@Override
	public double getBC() {
		return ignoreCorrectEstimate ? accumulatorBC : ((Network.size()/(double)numSamples)*accumulatorBC);
	}
	
	@Override
	public double getCC() {
		if (accumulatorCC == 0) return 0.0;
		else {
			double sx = (Network.size() / ((double) Network.size() - 1)) * accumulatorCC;
			return sx / numSamples;
		}
	}
}
