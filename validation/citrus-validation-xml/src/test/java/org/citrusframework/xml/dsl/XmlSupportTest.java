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

package org.citrusframework.xml.dsl;

import org.citrusframework.xml.UnitTestSupport;
import org.citrusframework.xml.actons.dsl.TestRequest;
import org.citrusframework.xml.message.builder.MarshallingPayloadBuilder;
import org.citrusframework.api.xml.Marshaller;
import org.citrusframework.base.xml.Jaxb2Marshaller;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class XmlSupportTest extends UnitTestSupport {

    private final Marshaller marshaller = new Jaxb2Marshaller(TestRequest.class);
    private final TestRequest request = new TestRequest("Hello Citrus!");

    @BeforeClass
    public void prepareMarshaller() {
        ((Jaxb2Marshaller) marshaller).setProperty(jakarta.xml.bind.Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
    }

    @Test
    public void shouldMarshal() {
        MarshallingPayloadBuilder builder = XmlSupport.marshal(request, marshaller);

        Assert.assertNotNull(builder);
        Assert.assertEquals(builder.buildPayload(context), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
    }

    @Test
    public void shouldMarshalWithAutoDetect() {
        MarshallingPayloadBuilder builder = XmlSupport.marshal(request);

        Assert.assertNotNull(builder);
    }
}
