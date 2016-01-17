package cycleDrivenFlooding;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class InitExample implements Control
{
	private final int _cdProtocolPid;
	private final int _startingNode;
	private final int _searchNode;
	private final int _maxPropagation;

	public InitExample(final String prefix_)
	{
		_cdProtocolPid = Configuration.getPid(prefix_ + ".cdProtocol");

		_startingNode = Configuration.getInt(prefix_ + ".nodeStart");
		_searchNode = Configuration.getInt(prefix_ + ".nodeSearch");
		_maxPropagation = Configuration.getInt(prefix_ + ".maxPropagation");
	}

	@Override
	public boolean execute()
	{
		final CDProtocolExample cdProtocol = ((CDProtocolExample)Network.get(_startingNode).getProtocol(_cdProtocolPid));

		cdProtocol.propagateSearch(cdProtocol, _searchNode, _maxPropagation);

		return false;
	}
}
