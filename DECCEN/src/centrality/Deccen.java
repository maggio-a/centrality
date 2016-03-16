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
import java.util.Objects;
import java.util.Set;

import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;


public class Deccen extends SynchronousCentralityProtocol {
	
	private static class ShortestPathData {
		public final int count;
		public final int length;
		
		public ShortestPathData(int count, int length) {
			this.count = count;
			this.length = length;
		}
	}
	
	private static class OrderedPair<T1,T2> {
		public final T1 first;
		public final T2 second;
		
		public OrderedPair(T1 first, T2 second) {
			this.first = first;
			this.second = second;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (o == null) return false;
			if (o.getClass() == OrderedPair.class) {
				OrderedPair<?,?> p = (OrderedPair<?,?>) o;
				if (Objects.equals(first, p.first) && Objects.equals(second, p.second)) return true;
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(first, second);
		}
	}
	
	private static final String PAR_LINKABLE = "lnk";
	
	private int linkableProtocolID;
	
	private long accumulatorSC;
	private long accumulatorCC;
	private int nCC;
	private double accumulatorBC;
	
	private Map<Node,ShortestPathData> shortestPathMap;
	private Set<OrderedPair<Long,Long>> handledReports;
	
	public Deccen(String prefix) {
		super(prefix);
		linkableProtocolID = Configuration.getPid(prefix + "." + PAR_LINKABLE);
		reset();
	}
	
	protected void reset() {
		accumulatorSC = 0;
		accumulatorCC = 0;
		nCC = 0;
		accumulatorBC = 0.0;
		shortestPathMap = new HashMap<Node,ShortestPathData>();
		handledReports = new HashSet<OrderedPair<Long,Long>>();
	}
	
	@Override
	public Object clone() {
		Deccen sdp = (Deccen) super.clone();
		sdp.reset();
		return sdp;
	}
	
	public void startCount(Node self, int pid) {
		shortestPathMap.put(self, new ShortestPathData(1, 0));
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		for (int i = 0; i < lnk.degree(); ++i) {
			Node n = lnk.getNeighbor(i);
			Deccen sdp = (Deccen) n.getProtocol(pid);
			addToSendQueue(Message.createDiscoveryMessage(self, self, 1, 0), sdp);
		}
	}
	
	@Override
	public void nextCycle(Node self, int protocolID) {
		Map<Node,List<Message>> discoveryMap = new HashMap<Node,List<Message>>();
		List<Message> reportList = new LinkedList<Message>();
		parseIncomingMessages(discoveryMap, reportList);
		if (!discoveryMap.isEmpty()) processDiscoveryMessages(self, protocolID, discoveryMap);
		if (!reportList.isEmpty()) processReportMessages(self, protocolID, reportList);
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
			} else if (m.type == Message.Type.REPORT) {
				reportList.add(m);
			}
			it.remove();
		}
	}
	
	private void processDiscoveryMessages(Node self, int pid, Map<Node, List<Message>> messageMap) {
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		for (Map.Entry<Node,List<Message>> e : messageMap.entrySet()) {
			Node s = e.getKey();
			if (!shortestPathMap.containsKey(s)) {
				List<Message> ml = e.getValue();
				int spCount = 0;
				int distance = -1;
				Set<Node> senders = new HashSet<Node>();
				for (Message m : ml) {
					assert m.type == Message.Type.DISCOVERY;
					if (distance == -1) distance = m.get(Message.Field.SP_LENGTH, Integer.class);
					assert distance == m.get(Message.Field.SP_LENGTH, Integer.class) : "Distance mismatch";
					spCount += m.get(Message.Field.SP_COUNT, Integer.class);
					senders.add(m.get(Message.Field.SENDER, Node.class));
				}
				++distance;
				shortestPathMap.put(s, new ShortestPathData(spCount, distance));
				
				for (int i = 0; i < lnk.degree(); ++i) {
					Node n = lnk.getNeighbor(i);
					Deccen sdp = (Deccen) n.getProtocol(pid);
					if (!senders.contains(n))
						addToSendQueue(Message.createDiscoveryMessage(self, s, spCount, distance), sdp);
					addToSendQueue(Message.createReportMessage(self, s, self, spCount, distance), sdp);
				}
			}
		}
	}
	
	private void processReportMessages(Node self, int pid, List<Message> messageList) {
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		for (Message m : messageList) {
			assert m.type == Message.Type.REPORT;
			Node s = m.get(Message.Field.SOURCE, Node.class);
			Node t = m.get(Message.Field.DESTINATION, Node.class);
			if (!alreadyReported(s, t)) {
				Node sender = m.get(Message.Field.SENDER, Node.class);
				int spCount = m.get(Message.Field.SP_COUNT, Integer.class);
				int distance = m.get(Message.Field.SP_LENGTH, Integer.class);
				
				boolean updated = updateCentralities(self, s, t, spCount, distance);
				// Optimization: propagate the report only if the node lies on a 
				// shortest path between s and t.
				if (updated) {
					for (int i = 0; i < lnk.degree(); ++i) {
						Node n = lnk.getNeighbor(i);
						Deccen sdp = (Deccen) n.getProtocol(pid);
						if (n.getID() != sender.getID())
							addToSendQueue(Message.createReportMessage(self, s, t, spCount, distance), sdp);
					}
				}
			}
		}
	}
	
	private boolean alreadyReported(Node s, Node t) {
		return handledReports.contains(new OrderedPair<Long,Long>(s.getID(), t.getID()));
	}

	/* 
	 * Updates the centrality accumulators using the information obtained
	 * from a report message. Returns true if the parameter self lies on a
	 * shortest path between s and t, false otherwise.
	 */
	private boolean updateCentralities(Node self, Node s, Node t, int sigma_st, int d_st) {
		assert !alreadyReported(s, t) : "Handling report for (" + s + "," + t + ") twice.";
		
		if (self == s) {
			accumulatorCC += d_st;
			nCC++;
		}
		
		boolean updated = false;
		
		if (self != s && self != t) {
			ShortestPathData pathToSource = shortestPathMap.get(s);
			ShortestPathData pathToDestination = shortestPathMap.get(t);
				
			assert pathToSource.length + pathToDestination.length >= d_st : "Reported a non-minimal length for " + s + "-to-" + t;
			
			if (pathToSource.length + pathToDestination.length == d_st) {
				long sigma_self = pathToSource.count * pathToDestination.count;
				accumulatorSC += sigma_self;
				accumulatorBC += sigma_self / (double) sigma_st;
				updated = true;
			}
		}
		
		handledReports.add(new OrderedPair<Long,Long>(s.getID(), t.getID()));
		return updated;
	}
	
	@Override
	public double getCC() {
		if (nCC == 0) return 0.0;
		else return (accumulatorCC / (double) nCC);
	}
	
	@Override
	public double getBC() {
		return accumulatorBC;
	}
	
	@Override
	public long getSC() {
		return accumulatorSC;
	}

}
