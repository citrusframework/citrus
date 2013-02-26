/*
 * Copyright 2006-2011 the original author or authors.
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

package com.consol.citrus.samples.flightbooking.header;

import org.springframework.http.HttpHeaders;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;

/**
 * @author Christoph Deppisch
 */
public class HttpHeaderMapper extends DefaultHttpHeaderMapper {

    @Override
    public void fromHeaders(MessageHeaders headers, HttpHeaders target) {
        super.fromHeaders(headers, target);

        //Fix issue with default header mapper not mapping non-string header values
        target.add("X-sequenceNumber", headers.getSequenceNumber().toString());
        target.add("X-sequenceSize", headers.getSequenceSize().toString());
    }
    
}
