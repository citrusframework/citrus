/*
 * Copyright the original author or authors.
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

package org.citrusframework.base.endpoint.adapter.mapping;

import org.citrusframework.endpoint.adapter.mapping.SoapActionMappingKeyExtractor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SoapActionMappingKeyExtractorTest {

    @Test
    public void testExtractMappingKey() {
        SoapActionMappingKeyExtractor extractor = new SoapActionMappingKeyExtractor();

        Assertions.assertEquals("foo", extractor.extractMappingKey(new DefaultMessage("Foo")
                .setHeader("citrus_soap_action", "foo")
                .setHeader("Bar", "bar")));
    }

    @Test
    public void testExtractNoMappingFound() {
        SoapActionMappingKeyExtractor extractor = new SoapActionMappingKeyExtractor();

        try {
            extractor.extractMappingKey(new DefaultMessage("Foo")
                    .setHeader("Foo", "foo")
                    .setHeader("Bar", "bar"));
            Assertions.fail("Missing exception due to unknown header");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().startsWith("Unable to find header 'citrus_soap_action'"));
        }
    }
}
