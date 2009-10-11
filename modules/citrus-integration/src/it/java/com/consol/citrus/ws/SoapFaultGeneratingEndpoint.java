package com.consol.citrus.ws;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;

import com.consol.citrus.exceptions.CitrusRuntimeException;

public class SoapFaultGeneratingEndpoint implements MessageEndpoint {

	public void invoke(MessageContext messageContext) throws Exception {
		throw new CitrusRuntimeException();
	}
}
