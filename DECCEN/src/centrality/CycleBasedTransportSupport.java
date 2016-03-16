/*
 * Peer-to-Peer Systems 2015/2016
 * 
 * Final project source code
 * 
 * Author: Andrea Maggiordomo - mggndr89@gmail.com
 */
package centrality;

import java.util.Iterator;


public interface CycleBasedTransportSupport<T> {
	
	interface SendQueueEntry<T> {
		T getMessage();
		CycleBasedTransportSupport<T> getDestination();
	}
	
	void addToSendQueue(T message, CycleBasedTransportSupport<T> destination);
	
	boolean hasOutgoingMessages();
	
	void addToIncoming(T message);
	
	Iterator<T> getIncomingMessagesIterator();
	
	Iterator<SendQueueEntry<T>> getSendQueueIterator();

}
