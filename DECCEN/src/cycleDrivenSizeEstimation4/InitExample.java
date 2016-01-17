package cycleDrivenSizeEstimation4;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class InitExample implements Control
{
	private final int cdProtocolPid;
	private final int startingNode;

	public InitExample(final String prefix) {
		System.out.println(prefix);
		cdProtocolPid = Configuration.getPid(prefix + "." + "cdProtocol");
		startingNode = Configuration.getInt(prefix + "." + "startingNode");
	}

	@Override
	public boolean execute()
	{
		final CDProtocolExample cdProtocol = ((CDProtocolExample)Network.get(startingNode).getProtocol(cdProtocolPid));
		cdProtocol.startCounting();
		
		return false;
	}
}
