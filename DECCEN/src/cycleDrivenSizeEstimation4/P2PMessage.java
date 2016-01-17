package cycleDrivenSizeEstimation4;

public class P2PMessage {

	private static long nextId = 0;
	
	private long id;
	private CDProtocolExample source;

	// New message
	public static P2PMessage getNewMessage(CDProtocolExample source) {
		return new P2PMessage(source);
	}
	
	// New message with the same id
	public static P2PMessage getForwardMessage(P2PMessage m, CDProtocolExample source) {
		return new P2PMessage(m.getId(), source);
	}
	
	private P2PMessage(CDProtocolExample source) {
		this.id = nextId++;
		this.source = source;
	}
	
	private P2PMessage(long id, CDProtocolExample source) {
		this.id = id;
		this.source = source;
	}
	
	public long getId() {
		return id;
	}
	
	public CDProtocolExample getSource() {
		return source;
	}
}
