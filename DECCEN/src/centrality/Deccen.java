package centrality;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import centrality.Message.Attachment;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;

public class Deccen extends SynchronousCentralityProtocol implements CDProtocol {
	
	private static class ShortestPathData {
		public final int count;
		public final int length;
		
		public ShortestPathData(int count, int length) {
			this.count = count;
			this.length = length;
		}
	}
	
	private static class Pair<T1,T2> {
		public final T1 first;
		public final T2 second;
		
		public Pair(T1 first, T2 second) {
			this.first = first;
			this.second = second;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (o == null) return false;
			if (o.getClass() == Pair.class) {
				Pair<?,?> p = (Pair<?,?>) o;
				if (Objects.equals(first, p.first) && Objects.equals(second, p.second)) return true;
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(first, second);
		}
	}
	
	private static String PAR_LINKABLE = "lnk";
	private static String PAR_CC_ALT = "useClosenessVariant";
	
	private int linkableProtocolID;
	private boolean useClosenessVariant;
	
	private long stress;
	private long closenessSum;
	private int closenessCount;
	private double betweenness;
	
	private Map<Node, ShortestPathData> shortestPathMap;
	private Set<Pair<Long,Long>> handledReports;
	
	public Deccen(String prefix) {
		super(prefix);
		linkableProtocolID = Configuration.getPid(prefix + "." + PAR_LINKABLE);
		useClosenessVariant = Configuration.contains(prefix + "." + PAR_CC_ALT);
		reset();
	}
	
	protected void reset() {
		stress = 0;
		closenessSum = 0;
		closenessCount = 0;
		betweenness = 0.0;
		shortestPathMap = new HashMap<Node, ShortestPathData>();
		handledReports = new HashSet<Pair<Long,Long>>();
	}
	
	@Override
	public Object clone() {
		Deccen sdp = (Deccen) super.clone();
		sdp.reset();
		return sdp;
	}
	
	private void parseIncomingMessages(Map<Node,List<Message>> probeMap, List<Message> reportList) {
		Iterator<Message> it = getIncomingMessagesIterator();
		while (it.hasNext()) {
			Message m = it.next();
			if (m.type == Message.Type.PROBE) {
				Node s = m.get(Attachment.SOURCE, Node.class);
				List<Message> ml = probeMap.get(s);
				if (ml == null) {
					ml = new LinkedList<Message>();
					probeMap.put(s, ml);
				}
				ml.add(m);
			} else if (m.type == Message.Type.REPORT) {
				reportList.add(m);
			}
			it.remove();
		}
	}
	
	@Override
	public void nextCycle(Node self, int protocolID) {
		Map<Node,List<Message>> probeMap = new HashMap<Node,List<Message>>();
		List<Message> reportList = new LinkedList<Message>();
		parseIncomingMessages(probeMap, reportList);
		if (!probeMap.isEmpty()) processNOSPMessages(self, protocolID, probeMap);
		if (!reportList.isEmpty()) processReportMessages(self, protocolID, reportList);
	}
	
	public void initCount(Node self, int pid) {
		shortestPathMap.put(self, new ShortestPathData(1, 0));
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		for (int i = 0; i < lnk.degree(); ++i) {
			Node n = lnk.getNeighbor(i);
			Deccen sdp = (Deccen) n.getProtocol(pid);
			addToSendQueue(Message.createProbeMessage(self, self, 1, 0), sdp);
		}
	}
	
	private boolean alreadyReported(Node s, Node t) {
		return handledReports.contains(new Pair<Long,Long>(s.getID(), t.getID()));
	}
	
	private void processNOSPMessages(Node self, int pid, Map<Node,List<Message>> messageMap) {
		for (Map.Entry<Node,List<Message>> e : messageMap.entrySet()) {
			Node s = e.getKey();
			if (!shortestPathMap.containsKey(s)) {
				List<Message> ml = e.getValue();
				int spCount = 0;
				int distance = ml.get(0).get(Attachment.SP_LENGTH, Integer.class);
				Set<Node> senders = new HashSet<Node>();
				for (Message m : ml) {
					spCount += m.get(Message.Attachment.SP_COUNT, Integer.class);
					assert distance == m.get(Message.Attachment.SP_LENGTH, Integer.class) : "Distance mismatch";
					senders.add(m.get(Attachment.SENDER, Node.class));
				}
				
				++distance;
				shortestPathMap.put(s, new ShortestPathData(spCount, distance));
				
				// Note: closeness centrality could added computed here
				
				Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
				for (int i = 0; i < lnk.degree(); ++i) {
					Node n = lnk.getNeighbor(i);
					Deccen sdp = (Deccen) n.getProtocol(pid);
					if (!senders.contains(n))
						addToSendQueue(Message.createProbeMessage(self, s, spCount, distance), sdp);
					addToSendQueue(Message.createReportMessage(self, s, self, spCount, distance), sdp);
				}
			}
		}
	}
	
	private void processReportMessages(Node self, int pid, List<Message> messageList) {
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		for (Message m : messageList) {
			Node s = m.get(Attachment.SOURCE, Node.class);
			Node t = m.get(Attachment.DESTINATION, Node.class);
			if (!alreadyReported(s, t)) {
				Node sender = m.get(Attachment.SENDER, Node.class);
				int spCount = m.get(Attachment.SP_COUNT, Integer.class);
				int distance = m.get(Attachment.SP_LENGTH, Integer.class);
				updateCentralitiesFromReport(self, s, t, spCount, distance);
				for (int i = 0; i < lnk.degree(); ++i) {
					Node n = lnk.getNeighbor(i);
					Deccen sdp = (Deccen) n.getProtocol(pid);
					if (n.getID() != sender.getID())
						addToSendQueue(Message.createReportMessage(self, s, t, spCount, distance), sdp);
				}
			}
		}
	}

	private void updateCentralitiesFromReport(Node self, Node s, Node t, int sigma_st, int d_st) {
		assert !alreadyReported(s, t) : "Handling report for (" + s + "," + t + ") twice.";
		
		if (self == s) {
			closenessSum += d_st;
			closenessCount++;
		}
		
		if (self != s && self != t) {
			ShortestPathData pathToSource = shortestPathMap.get(s);
			ShortestPathData pathToDestination = shortestPathMap.get(t);
				
			assert pathToSource.length + pathToDestination.length >= d_st : "Reported a non-minimal length for " + s + "-to-" + t;
			
			if (pathToSource.length + pathToDestination.length == d_st) {
				long sigma_self = pathToSource.count * pathToDestination.count;
				stress += sigma_self;
				betweenness += sigma_self / (double) sigma_st;
			}
		}
		
		handledReports.add(new Pair<Long,Long>(s.getID(), t.getID()));
	}
	
	public double getCC() {
		if (closenessCount == 0) return 0.0;
		else return useClosenessVariant ? (closenessSum / (double) closenessCount) :  (1.0 / closenessSum);
	}
	
	public double getBC() {
		return betweenness;
	}
	
	public long getSC() {
		return stress;
	}

}
