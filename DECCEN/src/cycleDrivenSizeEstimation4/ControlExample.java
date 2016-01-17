package cycleDrivenSizeEstimation4;

import java.util.Random;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;

public class ControlExample implements Control
{
	private final int cdProtocolPid;
	private final int countingPeers;
	private Random generator;

	public ControlExample(final String prefix_)
	{
		cdProtocolPid = Configuration.getPid(prefix_ + ".cdProtocol");
		countingPeers = Configuration.getInt(prefix_ + ".countingPeers");
		generator = new Random();
	}

	@Override
	public boolean execute()
	{
		System.out.println("Network size at cycle " + CommonState.getTime() + ": " + Network.size());
		
		for (int i = 0; i < countingPeers; i++) {
			int k = generator.nextInt(Network.size());
			CDProtocolExample cdProtocol = (CDProtocolExample) Network.get(k).getProtocol(cdProtocolPid);
			if (!cdProtocol.counting())
				cdProtocol.startCounting();
		}
		
		return false;
	}
}
