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

package org.citrusframework.message.builder;

import java.util.Collections;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.dsl.TestRequest;
import org.citrusframework.spi.ReferenceResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ObjectMappingHeaderDataBuilderTest extends UnitTestSupport {

    private final ObjectMapper mapper = new ObjectMapper();
    private final TestRequest request = new TestRequest("Hello Citrus!");

    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void shouldBuildHeaderData() {
        when(referenceResolver.resolveAll(ObjectMapper.class)).thenReturn(Collections.singletonMap("mapper", mapper));
        when(referenceResolver.resolve(ObjectMapper.class)).thenReturn(mapper);

        context.setReferenceResolver(referenceResolver);

        ObjectMappingHeaderDataBuilder builder = new ObjectMappingHeaderDataBuilder(request);

        Assert.assertEquals(builder.buildHeaderData(context), "{\"message\":\"Hello Citrus!\"}");
    }

    @Test
    public void shouldBuildHeaderDataWithMapper() {
        ObjectMappingHeaderDataBuilder builder = new ObjectMappingHeaderDataBuilder(request, mapper);

        Assert.assertEquals(builder.buildHeaderData(context), "{\"message\":\"Hello Citrus!\"}");
    }

    @Test
    public void shouldBuildHeaderDataWithMapperName() {
        when(referenceResolver.isResolvable("mapper")).thenReturn(true);
        when(referenceResolver.resolve("mapper", ObjectMapper.class)).thenReturn(mapper);

        context.setReferenceResolver(referenceResolver);

        ObjectMappingHeaderDataBuilder builder = new ObjectMappingHeaderDataBuilder(request, "mapper");

        Assert.assertEquals(builder.buildHeaderData(context), "{\"message\":\"Hello Citrus!\"}");
    }

    @Test
    public void shouldBuildHeaderDataWithVariableSupport() {
        context.setVariable("message", "Hello Citrus!");
        ObjectMappingHeaderDataBuilder builder = new ObjectMappingHeaderDataBuilder(new TestRequest("${message}"), mapper);

        Assert.assertEquals(builder.buildHeaderData(context), "{\"message\":\"Hello Citrus!\"}");
    }
}
