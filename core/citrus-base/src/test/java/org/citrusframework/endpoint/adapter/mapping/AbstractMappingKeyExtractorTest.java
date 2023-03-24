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

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class AbstractMappingKeyExtractorTest {

    @Test
    public void testMappingKeyPrefixSuffix() {
        AbstractMappingKeyExtractor mappingKeyExtractor = new AbstractMappingKeyExtractor() {
            @Override
            protected String getMappingKey(Message request) {
                return "key";
            }
        };

        mappingKeyExtractor.setMappingKeyPrefix("pre_");
        Assert.assertEquals(mappingKeyExtractor.extractMappingKey(new DefaultMessage("")), "pre_key");

        mappingKeyExtractor.setMappingKeySuffix("_end");
        Assert.assertEquals(mappingKeyExtractor.extractMappingKey(new DefaultMessage("")), "pre_key_end");

        mappingKeyExtractor.setMappingKeyPrefix("");
        Assert.assertEquals(mappingKeyExtractor.extractMappingKey(new DefaultMessage("")), "key_end");
    }
}
