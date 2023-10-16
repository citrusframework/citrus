/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.jms.endpoint.resolver;

import java.util.Map;

import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class DynamicDestinationNameResolver implements EndpointUriResolver {

    /** Static header entry name specifying the dynamic destination name */
    public static final String DESTINATION_HEADER_NAME = MessageHeaders.PREFIX + "jms_destination_name";

    /** Default fallback destination name */
    private String defaultDestinationName;

    /**
     * Get the endpoint uri according to message header entry with fallback default uri.
     */
    public String resolveEndpointUri(Message message, String defaultName) {
        Map<String, Object> headers = message.getHeaders();

        String destinationName;
        if (headers.containsKey(DESTINATION_HEADER_NAME)) {
            destinationName = headers.get(DESTINATION_HEADER_NAME).toString();
        } else if (StringUtils.hasText(defaultName)) {
            destinationName = defaultName;
        } else {
            destinationName = defaultDestinationName;
        }

        if (destinationName == null) {
            throw new CitrusRuntimeException("Unable to resolve dynamic destination name! Neither header entry '" +
                    DESTINATION_HEADER_NAME + "' nor default destination name is set");
        }

        return destinationName;
    }

    /**
     * Gets the defaultDestinationName.
     *
     * @return
     */
    public String getDefaultDestinationName() {
        return defaultDestinationName;
    }

    /**
     * Sets the defaultDestinationName.
     *
     * @param defaultDestinationName
     */
    public void setDefaultDestinationName(String defaultDestinationName) {
        this.defaultDestinationName = defaultDestinationName;
    }
}
