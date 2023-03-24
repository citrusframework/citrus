/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.endpoint.adapter.mapping;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;

/**
 * Extractor searches for header name in request headers for mapping name identification.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class HeaderMappingKeyExtractor extends AbstractMappingKeyExtractor {

    /** Header name to search for */
    private String headerName = "";

    /**
     * Default constructor.
     */
    public HeaderMappingKeyExtractor() {
        super();
    }

    /**
     * Constructor using header name field.
     * @param headerName
     */
    public HeaderMappingKeyExtractor(String headerName) {
        this.headerName = headerName;
    }

    @Override
    public String getMappingKey(Message request) {
        if (request.getHeader(headerName) != null) {
            return request.getHeader(headerName).toString();
        } else {
            throw new CitrusRuntimeException(String.format("Unable to find header '%s' in request message", headerName));
        }
    }

    /**
     * Sets the header name.
     * @param headerName
     */
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
}
