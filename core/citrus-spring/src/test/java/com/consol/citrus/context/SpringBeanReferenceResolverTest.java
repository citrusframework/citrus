/*
 * Copyright 2022 the original author or authors.
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

package com.consol.citrus.context;

import com.consol.citrus.UnitTestSupport;
import com.consol.citrus.xml.Marshaller;
import com.consol.citrus.xml.MarshallerAdapter;
import com.consol.citrus.xml.Unmarshaller;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SpringBeanReferenceResolverTest extends UnitTestSupport {

    private SpringBeanReferenceResolver resolver;

    @Mock
    private org.springframework.oxm.Marshaller marshaller;

    @Mock
    private org.springframework.oxm.Unmarshaller unmarshaller;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.openMocks(this);
        resolver = new SpringBeanReferenceResolver(applicationContext);
    }

    @Test
    public void shouldResolveMarshallerAlias() {
        String marshallerName = "springOxmMarshaller";

        Assert.assertFalse(resolver.isResolvable(Marshaller.class));
        Assert.assertFalse(resolver.isResolvable(marshallerName, Marshaller.class));
        Assert.assertEquals(resolver.resolveAll(Marshaller.class).size(), 0L);

        resolver.bind(marshallerName, marshaller);

        Assert.assertTrue(resolver.isResolvable(Marshaller.class));
        Assert.assertTrue(resolver.isResolvable(marshallerName, Marshaller.class));
        Assert.assertEquals(resolver.resolve(Marshaller.class).getClass(), MarshallerAdapter.class);
        Assert.assertEquals(resolver.resolve(marshallerName, Marshaller.class).getClass(), MarshallerAdapter.class);
        Assert.assertEquals(resolver.resolveAll(Marshaller.class).size(), 1L);
    }

    @Test
    public void shouldResolveUnmarshallerAlias() {
        String unmarshallerName = "springOxmUnmarshaller";

        Assert.assertFalse(resolver.isResolvable(Unmarshaller.class));
        Assert.assertFalse(resolver.isResolvable(unmarshallerName, Unmarshaller.class));
        Assert.assertEquals(resolver.resolveAll(Unmarshaller.class).size(), 0L);

        resolver.bind(unmarshallerName, unmarshaller);

        Assert.assertTrue(resolver.isResolvable(Unmarshaller.class));
        Assert.assertTrue(resolver.isResolvable(unmarshallerName, Unmarshaller.class));
        Assert.assertEquals(resolver.resolve(Unmarshaller.class).getClass(), MarshallerAdapter.class);
        Assert.assertEquals(resolver.resolve(unmarshallerName, Unmarshaller.class).getClass(), MarshallerAdapter.class);
        Assert.assertEquals(resolver.resolveAll(Unmarshaller.class).size(), 1L);
    }

}
