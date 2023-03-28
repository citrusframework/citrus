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
import org.citrusframework.message.DefaultMessage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SoapActionMappingKeyExtractorTest {

    @Test
    public void testExtractMappingKey() throws Exception {
        SoapActionMappingKeyExtractor extractor = new SoapActionMappingKeyExtractor();

        Assert.assertEquals(extractor.extractMappingKey(new DefaultMessage("Foo")
                .setHeader("citrus_soap_action", "foo")
                .setHeader("Bar", "bar")), "foo");
    }

    @Test
    public void testExtractNoMappingFound() throws Exception {
        SoapActionMappingKeyExtractor extractor = new SoapActionMappingKeyExtractor();

        try {
            extractor.extractMappingKey(new DefaultMessage("Foo")
                    .setHeader("Foo", "foo")
                    .setHeader("Bar", "bar"));
            Assert.fail("Missing exception due to unknown header");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to find header 'citrus_soap_action'"));
        }
    }
}
