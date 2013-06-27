/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.adapter.handler.mapping;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.integration.Message;

/**
 * Extractor searches for SOAP action header name in request headers for mapping name identification.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class SoapActionMappingKeyExtractor extends HeaderMappingKeyExtractor {

    /**
     * Default constructor using Citrus soap action header name.
     */
    public SoapActionMappingKeyExtractor() {
        super("soap_action");
    }
}
