/*
 * Peer-to-Peer Systems 2015/2016
 * 
 * Final project source code
 * 
 * Author: Andrea Maggiordomo - mggndr89@gmail.com
 */
package centrality;

import java.util.ArrayList;
import java.util.List;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;


public abstract class CentralityProtocol implements EDProtocol, CDProtocol {
	
	/**
	 * The {@code Transport} protocol used in the simulation.
	 */
	private static String PAR_TRANSPORT = "transport";
	
	private int transportProtocolID;
	
	private List<Message> incoming;
	
	public CentralityProtocol(String prefix) {
		transportProtocolID = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
		incoming = new ArrayList<Message>();
	}
	
	@Override
	public Object clone() {
		CentralityProtocol cp = null;
		try { 
			cp = (CentralityProtocol) super.clone();
		} catch (CloneNotSupportedException e) { e.printStackTrace(); }
		cp.incoming = new ArrayList<Message>();
		return cp;
	}
	
	@Override
	public void processEvent(Node node, int pid, Object event) {
		incoming.add((Message) event);
		CentralitySimulation.newMessage();
	}
	
	protected void send(Node from, Node to, Message m, int pid) {
		Transport tr = (Transport) from.getProtocol(transportProtocolID);
		tr.send(from, to, m, pid);
	}
	
	protected List<Message> getIncomingMessages() {
		return incoming;
	}
	
	@Override
	public abstract void nextCycle(Node self, int protocolID);

	public abstract double getCC();
	
	public abstract double getBC();
	
	public abstract long getSC();

}
