/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.adapter.common.endpoint;

import org.springframework.integration.Message;

/**
 * Resolves endpoint uri so we can send messages to dynamic endpoints. Resolver works on request message and 
 * chooses the target message endpoint according to message headers or payload.
 *  
 * @author Christoph Deppisch
 */
public interface EndpointUriResolver {
    
    /**
     * Get the dedicated message endpoint uri for this message.
     * @param message the request message to send.
     * @return the endpoint uri String representation
     */
    public String resolveEndpointUri(Message<?> message);
    
    /**
     * Get the dedicated message endpoint uri for this message.
     * @param message the request message to send.
     * @param defaultUri the fallback uri in case no mapping was found.
     * @return the endpoint uri String representation
     */
    public String resolveEndpointUri(Message<?> message, String defaultUri);
}
