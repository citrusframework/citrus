package com.consol.citrus.dsl;

import java.util.Arrays;
import java.util.List;

import com.consol.citrus.actions.StartServerAction;
import com.consol.citrus.server.Server;

public class StartServerActionDefinition extends AbstractActionDefinition<StartServerAction> {

	public StartServerActionDefinition(StartServerAction action) {
	    super(action);
    }

	public StartServerActionDefinition serverList(List<Server> serverList) {
		action.setServerList(serverList);
		return this;
	}
	
	public StartServerActionDefinition serverList(Server... servers) {
		return serverList(Arrays.asList(servers));
	}
	
	public StartServerActionDefinition server(Server server) {
		action.setServer(server);
		return this;
	}
}
