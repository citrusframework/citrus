/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.consol.citrus.telnet.client;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpointComponent;
import com.consol.citrus.endpoint.Endpoint;

import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Michael Wurmbrand
 * @since 2.6
 */
public class TelnetEndpointComponent extends AbstractEndpointComponent {

	/**
	 * Component creates proper telnet client from endpoint uri resource and parameters.
	 * telnet://<user>:<password>@<host>[:<port>/]
	 */
	@Override
	protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters,
			TestContext context) {
        
		TelnetClient telnetClient = new TelnetClient();
        
        // assume resourcepath has striped protocol
        StringTokenizer tok = new StringTokenizer(resourcePath, ":@");
        telnetClient.getEndpointConfiguration().setUser(tok.nextToken());
        telnetClient.getEndpointConfiguration().setPassword(tok.nextToken());
        telnetClient.getEndpointConfiguration().setHost(tok.nextToken());
        if (tok.hasMoreTokens()) {
        	telnetClient.getEndpointConfiguration().setPort(Integer.parseInt(tok.nextToken()));
        }
        enrichEndpointConfiguration(telnetClient.getEndpointConfiguration(),  getEndpointConfigurationParameters(parameters, TelnetEndpointConfiguration.class), context);
        
        return telnetClient;
	}
}
