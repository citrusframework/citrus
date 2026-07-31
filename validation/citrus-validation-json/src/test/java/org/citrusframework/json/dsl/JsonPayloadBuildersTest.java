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

package org.citrusframework.json.dsl;

import org.citrusframework.json.UnitTestSupport;
import org.citrusframework.json.actions.dsl.TestRequest;
import org.citrusframework.message.DefaultPayloadBuilders;
import org.citrusframework.message.MessagePayloadBuilder;
import org.citrusframework.message.PayloadBuilders;
import org.testng.Assert;
import org.testng.annotations.Test;
import tools.jackson.databind.json.JsonMapper;

public class JsonPayloadBuildersTest extends UnitTestSupport {

    private final JsonMapper mapper = JsonMapper.shared();
    private final TestRequest request = new TestRequest("Hello Citrus!");

    @Test
    public void shouldMarshalWithMapper() {
        PayloadBuilders builders = new DefaultPayloadBuilders();

        MessagePayloadBuilder payloadBuilder = builders.json().marshal(request, mapper);

        Assert.assertNotNull(payloadBuilder);
        Assert.assertEquals(payloadBuilder.buildPayload(context), "{\"message\":\"Hello Citrus!\"}");
    }

    @Test
    public void shouldMarshalWithAutoDetect() {
        PayloadBuilders builders = new DefaultPayloadBuilders();

        MessagePayloadBuilder payloadBuilder = builders.json().marshal(request);

        Assert.assertNotNull(payloadBuilder);
    }
}
