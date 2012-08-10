package com.consol.citrus.dsl;

import java.util.Arrays;
import java.util.List;

import com.consol.citrus.actions.StopServerAction;
import com.consol.citrus.server.Server;

public class StopServerActionDefinition extends AbstractActionDefinition<StopServerAction> {

	public StopServerActionDefinition(StopServerAction action) {
	    super(action);
    }

	public StopServerActionDefinition serverList(List<Server> serverList) {
		action.setServerList(serverList);
		return this;
	}
	
	public StopServerActionDefinition serverList(Server... servers) {
		return serverList(Arrays.asList(servers));
	}
	
	public StopServerActionDefinition server(Server server) {
		action.setServer(server);
		return this;
	}
}
