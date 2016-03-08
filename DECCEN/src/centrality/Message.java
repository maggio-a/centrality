package centrality;

import java.util.HashMap;
import java.util.Map;

import peersim.core.Node;


public class Message {
	
	public enum Type {
		DISCOVERY, REPORT, MBFS_REPORT
	}
	
	public enum Field {
		SENDER, SOURCE, DESTINATION, SP_COUNT, SP_LENGTH, SC_CONTRIBUTION, BC_CONTRIBUTION
	}
	
	private Map<Field, Object> fields;
	public final Type type;
	
	private Message(Type t) {
		type = t;
		fields = new HashMap<Field, Object>(8);
	}
	
	public static Message createDiscoveryMessage(Node sender, Node source, int count, int length) {
		Message m = new Message(Type.DISCOVERY);
		m.fields.put(Field.SENDER, sender);
		m.fields.put(Field.SOURCE, source);
		m.fields.put(Field.SP_COUNT, count);
		m.fields.put(Field.SP_LENGTH, length);
		return m;
	}
	
	public static Message createReportMessage(Node sender, Node source, Node destination, int count, int length) {
		Message m = new Message(Type.REPORT);
		m.fields.put(Field.SENDER, sender);
		m.fields.put(Field.SOURCE, source);
		m.fields.put(Field.DESTINATION, destination);
		m.fields.put(Field.SP_COUNT, count);
		m.fields.put(Field.SP_LENGTH, length);
		return m;
	}
	
	public static Message createMBFSReportMessage(Node sender, Node source, double bc, long sc, int count) {
		Message m = new Message(Type.MBFS_REPORT);
		m.fields.put(Field.SENDER, sender);
		m.fields.put(Field.SOURCE, source);
		m.fields.put(Field.BC_CONTRIBUTION, bc);
		m.fields.put(Field.SC_CONTRIBUTION, sc);
		m.fields.put(Field.SP_COUNT, count);
		return m;
	}
	
	public <T> T get(Field f, Class<T> c) {
		return c.cast(fields.get(f));
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[type=" + type + "][fields=" + fields + "]";
	}

}
