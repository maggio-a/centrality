import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;


public class SynchronousDeccenProtocol implements CDProtocol {
	
	private static class SendQueueItem {
		public final Node destination;
		public final Message message;
		
		public SendQueueItem(Node d, Message m) {
			destination = d;
			message = m;
		}
		
		public String toString() {
			return getClass().getName() + "[destinationID=" + destination.getID()
					+ "][message=" + message +"]";
		}
	}
	
	private static class ShortestPathData {
		public final int count;
		public final int length;
		
		public ShortestPathData(int c, int l) {
			count = c;
			length = l;
		}
	}
	
	private static String PAR_LINKABLE = "lnk";
	
	private int linkableProtocolID;
	
	// if true nextCycle just delivers messages, if false computes values
	private boolean sendMessages               = false;
	private long    stressCentralityValue      = 0;
	private long    closenessCentralityValue   = 0;
	private int     closenessCount             = 0;
	private double  betweennessCentralityValue = 0.0;
	private int     step                       = 0;
	
	private Map<Node, List<Message>> NOSPMessageCache   = null;
	private List<Message>            REPORTMessageCache = null;
	private Map<Node, ShortestPathData>    sp                 = null;
	private Deque<SendQueueItem>           sendQueue          = null;
	private boolean[][]                    reportMap          = null;
	
	public SynchronousDeccenProtocol(String prefix) {
		linkableProtocolID = Configuration.getPid(prefix + "." + PAR_LINKABLE);
	}
	
	@Override
	public Object clone() {
		SynchronousDeccenProtocol sdp = null;
		try {
			sdp = (SynchronousDeccenProtocol) super.clone();
		} catch (CloneNotSupportedException e) { e.printStackTrace(); }
		sdp.NOSPMessageCache = new HashMap<Node, List<Message>>();
		sdp.REPORTMessageCache = new LinkedList<Message>();
		sdp.sp = new HashMap<Node, ShortestPathData>();
		sdp.sendQueue = new LinkedList<SendQueueItem>();
		return sdp;
	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		if (sendMessages) {
			sendPendingMessages(protocolID);
			sendMessages = false;
		} else {
			step++;
			// (1) process incoming NOSP messages
			// (2) report on newly computed values
			processNOSPMessages(node, protocolID);
			// (3) process incoming report messages to compute the centrality value
			processReportMessages(node, protocolID);
			sendMessages = true;
		}
	}
	
	private void processReportMessages(Node self, int protocolID) {
		Iterator<Message> it = REPORTMessageCache.iterator();
		while (it.hasNext()) {
			Message m = it.next();
			Node s = m.get(Message.Attachment.SOURCE, Node.class);
			Node t = m.get(Message.Attachment.DESTINATION, Node.class);
			int count = m.get(Message.Attachment.SP_COUNT, Integer.class);
			int length = m.get(Message.Attachment.SP_LENGTH, Integer.class);
			if (relevantReport(s, t)) {
				integrateCentralityContribution(self, s, t, count, length);
				Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
				for (int i = 0; i < lnk.degree(); ++i) {
					Node n = lnk.getNeighbor(i);
					if (n.getID() != m.get(Message.Attachment.SENDER, Node.class).getID())
						sendQueue.push(new SendQueueItem(n, Message.createReportMessage(self, s, t, count, length)));
				}
			}
			it.remove();
		}
	}
	
	//FIXME XXX unsafe conversion of Node.getID() from long to int !!!!!!!!!!!!!
	private boolean relevantReport(Node source, Node destination) {
		return !reportMap[(int)source.getID()][(int)destination.getID()];
	}
	
	//FIXME XXX unsafe conversion of Node.getID() from long to int !!!!!!!!!!!!!
	private void integrateCentralityContribution(Node self, Node source, Node destination, int count, int length) {
		assert sp.containsKey(source) : "Path to source unknown";
		assert sp.containsKey(destination) : "Path to destination unknown";
		assert reportMap[(int)source.getID()][(int)destination.getID()] == false : "Report is not relevant";
		
		reportMap[(int)source.getID()][(int)destination.getID()] = true;
		
		// compute stress centrality and betweenness centrality
		
		if (self != source && self != destination) {	
			ShortestPathData pathToSource = sp.get(source);
			ShortestPathData pathToDestination = sp.get(destination);
			
			assert pathToSource.length + pathToDestination.length >= length : "Reported a non-minimal length for " + source + "-to-" + destination;
			if (pathToSource.length + pathToDestination.length == length) {
				long sigma = pathToSource.count * pathToDestination.count;
				stressCentralityValue += sigma;
				betweennessCentralityValue += sigma / (double) count;
			}
		}
		
		// compute closeness centrality
		
		if (self == destination) {
			closenessCentralityValue += length;
			closenessCount++;
		}
		
		
	}
	
	private void sendPendingMessages(int protocolID) {
		while (!sendQueue.isEmpty()) {
			SendQueueItem i = sendQueue.pop();
			SynchronousDeccenProtocol other = (SynchronousDeccenProtocol) i.destination.getProtocol(protocolID);
			other.acceptMessage(i.message);
		}
	}
	
	private void acceptMessage(Message m) {
		if (m.type == Message.Type.NOSP) {
			Node source = m.get(Message.Attachment.SOURCE, Node.class);
			if (!sp.containsKey(source)) {
				List<Message> l = NOSPMessageCache.get(source);
				if (l == null) {
					l = new LinkedList<Message>();
					NOSPMessageCache.put(source, l);
				}
				l.add(m);
			} // otherwise ignore the message (in the synchronous model if we already know the path to a node
			  // from earlier messages, this path will be longer)
		} else if (m.type == Message.Type.REPORT) {
			if (relevantReport(m.get(Message.Attachment.SOURCE, Node.class), m.get(Message.Attachment.DESTINATION, Node.class)))
				REPORTMessageCache.add(m);
		} else {
			System.err.println(this.getClass().getName() + ".acceptMessage: unexpected message " + m);
		}
	}
	
	/**
	 * Initiates the counting process.
	 * @param self The node running this protocol 
	 */
	public void initCount(Node self, int pid, int sznetwork) {
		sendMessages = false;
		stressCentralityValue = 0;
		closenessCentralityValue = 0;
		closenessCount = 0;
		betweennessCentralityValue = 0.0;
		step = 0;
		
		sp.clear();
		NOSPMessageCache.clear();
		REPORTMessageCache.clear();
		sendQueue.clear();
		
		sp.put(self, new ShortestPathData(1, 0));
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		for (int i = 0; i < lnk.degree(); ++i) {
			Node n = lnk.getNeighbor(i);
			sendQueue.push(new SendQueueItem(n, Message.createNOSPMessage(self, self, 1, 0)));
			//SynchronousDeccenProtocol sdp = (SynchronousDeccenProtocol) n.getProtocol(pid);
			//sdp.acceptMessage(DeccenMessage.createNOSPMessage(node, node, 1));
		}
		sendMessages = true;
		reportMap = new boolean[sznetwork][sznetwork];
		for (int i = 0; i < sznetwork; ++i) reportMap[i][i] = true;
	}
	
	// The weight of a report is the length of the shortest path
	private void processNOSPMessages(Node self, int protocolID) {
		Iterator<Map.Entry<Node, List<Message>>> it = NOSPMessageCache.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Node, List<Message>> nextMapping = it.next();
			Node source = nextMapping.getKey();
			if (sp.containsKey(source)) {
				// already processed, ignore
				assert step > sp.get(source).length;
			} else {
				int count = 0;
				int length = step;
				for (Message message : nextMapping.getValue()) {
					assert message.type == Message.Type.NOSP : message.type;
					count += message.get(Message.Attachment.SP_COUNT, Integer.class);
				}
				sp.put(source, new ShortestPathData(count, length));
				
				assert relevantReport(source, self) : "NOSP already reported ????";
				integrateCentralityContribution(self, source, self, count, length);
				
				Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
				for (int i = 0; i < lnk.degree(); ++i) {
					Node n = lnk.getNeighbor(i);
					if (n.getID() != source.getID()) {
						sendQueue.push(new SendQueueItem(n, Message.createNOSPMessage(self, source, count, length)));
					}
					sendQueue.push(new SendQueueItem(n, Message.createReportMessage(self, source, self, count, length)));
				}
			}
			it.remove();
		}
	}
	
	public long getStressCentralityValue() {
		return stressCentralityValue;
	}
	
	public double getClosenessCentralityValue() {
		//return closenessCentralityValue / (double) closenessCount;
		return 1.0 / closenessCentralityValue;
	}
	
	public double getBetweennessCentralityValue() {
		return betweennessCentralityValue;
	}

}
