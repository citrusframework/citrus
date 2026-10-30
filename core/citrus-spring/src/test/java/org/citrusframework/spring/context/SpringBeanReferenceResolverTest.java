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

package org.citrusframework.spring.context;

import org.citrusframework.spring.UnitTestSupport;
import org.citrusframework.api.xml.Marshaller;
import org.citrusframework.spring.xml.MarshallerAdapter;
import org.citrusframework.api.xml.Unmarshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpringBeanReferenceResolverTest extends UnitTestSupport {

    private SpringBeanReferenceResolver resolver;

    @Mock
    private org.springframework.oxm.Marshaller marshaller;

    @Mock
    private org.springframework.oxm.Unmarshaller unmarshaller;

    @BeforeEach
    public void setup() {
        resolver = new SpringBeanReferenceResolver(applicationContext);
    }

    @Test
    public void shouldResolveMarshallerAlias() {
        String marshallerName = "springOxmMarshaller";

        Assertions.assertFalse(resolver.isResolvable(Marshaller.class));
        Assertions.assertFalse(resolver.isResolvable(marshallerName, Marshaller.class));
        Assertions.assertEquals(0L, resolver.resolveAll(Marshaller.class).size());

        resolver.bind(marshallerName, marshaller);

        Assertions.assertTrue(resolver.isResolvable(Marshaller.class));
        Assertions.assertTrue(resolver.isResolvable(marshallerName, Marshaller.class));
        Assertions.assertEquals(MarshallerAdapter.class, resolver.resolve(Marshaller.class).getClass());
        Assertions.assertEquals(MarshallerAdapter.class, resolver.resolve(marshallerName, Marshaller.class).getClass());
        Assertions.assertEquals(1L, resolver.resolveAll(Marshaller.class).size());
    }

    @Test
    public void shouldResolveUnmarshallerAlias() {
        String unmarshallerName = "springOxmUnmarshaller";

        Assertions.assertFalse(resolver.isResolvable(Unmarshaller.class));
        Assertions.assertFalse(resolver.isResolvable(unmarshallerName, Unmarshaller.class));
        Assertions.assertEquals(0L, resolver.resolveAll(Unmarshaller.class).size());

        resolver.bind(unmarshallerName, unmarshaller);

        Assertions.assertTrue(resolver.isResolvable(Unmarshaller.class));
        Assertions.assertTrue(resolver.isResolvable(unmarshallerName, Unmarshaller.class));
        Assertions.assertEquals(MarshallerAdapter.class, resolver.resolve(Unmarshaller.class).getClass());
        Assertions.assertEquals(MarshallerAdapter.class, resolver.resolve(unmarshallerName, Unmarshaller.class).getClass());
        Assertions.assertEquals(1L, resolver.resolveAll(Unmarshaller.class).size());
    }
}
