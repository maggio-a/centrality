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
import peersim.core.Linkable;
import peersim.core.Node;


public class DecentralizedBFS extends SynchronousTransportLayer<BFSMessage> implements CDProtocol {
	

	
	private static final String PAR_LINKABLE = "lnk";
	
	private int linkableProtocolID;
	
	//private Map<Node, List<Node>> predecessors;
	private List<Node> predecessors = null;
	private Set<Node> siblings = null;
	
	public State state;
	int depth;
	int sigma;
	int delta;
	
	/*private static final int CLOSED   = 0;
	private static final int OPEN     = 1;
	private static final int VISITING = 2;
	private static final int FRINGE   = 3;
	private static final int SOURCE = 4;
	public static final int REPORTED = 5;*/
	
	public enum State {CLOSED, OPEN, VISITING, FRINGE, SOURCE, REPORTED }
	
	public DecentralizedBFS(String prefix) {
		super(prefix);
		linkableProtocolID = Configuration.getPid(prefix + "." + PAR_LINKABLE);
		state = State.CLOSED;
		depth = -1;
		sigma = 0;
		delta = 0;
	}
	
	@Override
	public Object clone() {
		DecentralizedBFS o = (DecentralizedBFS) super.clone();
		
		//o.predecessors = new HashMap<Node, List<Node>>();
		o.predecessors = new LinkedList<Node>();
		o.siblings = new HashSet<Node>();
		
		return o;
	}

	@Override
	public void nextCycle(Node self, int protocolID) {
		
		if (((MyNode)self).getLabel().equals("40")) {
			System.out.println("");
		}
		
		Linkable lnk = (Linkable) self.getProtocol(linkableProtocolID);
		
		// process incoming messages
		Map<Integer, List<BFSMessage>> mmap = parseIncomingMessages();
		
		if (state == State.SOURCE || (state == State.CLOSED && mmap.containsKey(BFSMessage.PROBE))) {
			if (!(state == State.SOURCE)) {
				List<BFSMessage> messageList = mmap.get(BFSMessage.PROBE);
				for (BFSMessage m : messageList) {
					assert depth == -1 || depth == m.depth : "invalid depth";
					if (depth == -1) depth = m.depth;
					sigma += m.sigma; // TODO double check this
					predecessors.add(m.sender);
				}
			}
			for (int i = 0; i < lnk.degree(); ++i) {
				Node n = lnk.getNeighbor(i);
				if (!predecessors.contains(n)) {
					addToSendQueue(new BFSMessage(self, BFSMessage.PROBE, depth + 1, sigma), n);
				}
			}
			state = State.OPEN;
		} else if (state == State.OPEN) {
			// tutti i vicini sono predecessori -> foglia
			// tutti i vicini che non sono predecessori hanno terminato
			
			List<BFSMessage> ls = mmap.get(BFSMessage.PROBE);
			if (ls != null) {
				assert siblings.isEmpty() : "Siblings set not empty";
				for (BFSMessage m : ls) siblings.add(m.sender);
			}
			
			boolean canReport = true;
			int d = 0;
			for (int i = 0; i < lnk.degree() && canReport; ++i) {
				Node n = lnk.getNeighbor(i);
				if (predecessors.contains(n) || siblings.contains(n)) {
					continue;
				} else {
					DecentralizedBFS dbfs = (DecentralizedBFS) n.getProtocol(protocolID);
					
					if (dbfs.state == State.REPORTED) {
						assert dbfs.predecessors.contains(self) : "self not in predecessor list";
						d += (int) sigma * (1 + (dbfs.delta / (double) dbfs.sigma));
					} else {
						canReport = false;
					}
				}
			}
			if (canReport) { // FIXME
				delta = d;
				state = State.REPORTED;
			}

		} else if (state == State.REPORTED) {
			if (predecessors.contains(self)) {
				System.out.println("Reported back to source.");
			}
		}
	}
	
	private Map<Integer, List<BFSMessage>> parseIncomingMessages() {
		Map<Integer, List<BFSMessage>> map = new HashMap<Integer, List<BFSMessage>>();
		Iterator<BFSMessage> it = getIncomingMessageIterator();
		while (it.hasNext()) {
			BFSMessage msg = (BFSMessage) it.next();
			it.remove();
			List<BFSMessage> ls = map.get(msg.type);
			if (ls == null) {
				ls = new LinkedList<BFSMessage>();
				map.put(msg.type, ls);
			}
			ls.add(msg);
		}
		return map;
	}
	
	
	public void bfs(Node self, int pid) {
		LinkedList<Node> pl = new LinkedList<Node>();
		pl.add(self);
		predecessors = pl;
		depth = 0;
		sigma = 1;
		state = State.SOURCE;
		
	}
	
	public List<Node> getPredecessors() {
		return this.predecessors;
	}
	
	public boolean hasState(State s) {
		return this.state == s;
	}
	
	
}

class BFSMessage {
	public static final int PROBE = 1;
	//private static final int REPORT = 2;
	
	public final Node sender;
	
	public final int type;
	public final int depth;
	public final int sigma;
	
	public BFSMessage(Node s, int t, int d, int sig) {
		sender = s;
		type = t;
		depth = d;
		sigma = sig;
		
	}
}
