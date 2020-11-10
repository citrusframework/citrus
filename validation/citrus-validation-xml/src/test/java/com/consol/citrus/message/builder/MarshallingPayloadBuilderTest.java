/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.message.builder;

import java.util.Collections;

import com.consol.citrus.UnitTestSupport;
import com.consol.citrus.actions.dsl.TestRequest;
import com.consol.citrus.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class MarshallingPayloadBuilderTest extends UnitTestSupport {

    private final XStreamMarshaller marshaller = new XStreamMarshaller();
    private final TestRequest request = new TestRequest("Hello Citrus!");

    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @BeforeClass
    public void prepareMarshaller() {
        marshaller.getXStream().processAnnotations(TestRequest.class);
    }

    @Test
    public void shouldBuildPayload() {
        when(referenceResolver.resolveAll(Marshaller.class)).thenReturn(Collections.singletonMap("marshaller", marshaller));
        when(referenceResolver.resolve(Marshaller.class)).thenReturn(marshaller);

        context.setReferenceResolver(referenceResolver);

        MarshallingPayloadBuilder builder = new MarshallingPayloadBuilder(request);

        Assert.assertEquals(builder.buildPayload(context), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
    }

    @Test
    public void shouldBuildPayloadWithMapper() {
        MarshallingPayloadBuilder builder = new MarshallingPayloadBuilder(request, marshaller);

        Assert.assertEquals(builder.buildPayload(context), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
    }

    @Test
    public void shouldBuildPayloadWithMapperName() {
        when(referenceResolver.isResolvable("marshaller")).thenReturn(true);
        when(referenceResolver.resolve("marshaller", Marshaller.class)).thenReturn(marshaller);

        context.setReferenceResolver(referenceResolver);

        MarshallingPayloadBuilder builder = new MarshallingPayloadBuilder(request, "marshaller");

        Assert.assertEquals(builder.buildPayload(context), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
    }

    @Test
    public void shouldBuildPayloadWithVariableSupport() {
        context.setVariable("message", "Hello Citrus!");
        MarshallingPayloadBuilder builder = new MarshallingPayloadBuilder(new TestRequest("${message}"), marshaller);

        Assert.assertEquals(builder.buildPayload(context), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
    }
}
