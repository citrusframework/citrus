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

package org.citrusframework.endpoint;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusEndpointAnnotations;
import org.citrusframework.annotations.CitrusEndpointProperty;
import org.citrusframework.context.TestContextFactory;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class AbstractEndpointBuilderTest extends UnitTestSupport {

    @CitrusEndpoint(
            name = "fooEndpoint",
            properties = {
                    @CitrusEndpointProperty(name = "message", value = "Hello from Citrus!"),
                    @CitrusEndpointProperty(name = "number", value = "1", type = int.class),
                    @CitrusEndpointProperty(name = "person", value = "testPerson", type = TestEndpointBuilder.Person.class)
            }
    )
    private Endpoint injected;

    private TestEndpointBuilder endpointBuilder = new TestEndpointBuilder();
    private TestEndpointBuilder.Person person = new TestEndpointBuilder.Person("Peter", 29);

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory contextFactory = super.createTestContextFactory();
        contextFactory.getReferenceResolver().bind("testBuilder", endpointBuilder);
        contextFactory.getReferenceResolver().bind("testPerson", person);
        return contextFactory;
    }

    @Test
    public void buildFromEndpointProperties() {
        CitrusEndpointAnnotations.injectEndpoints(this, context);

        assertEquals(injected, endpointBuilder.mockEndpoint);
        assertEquals(endpointBuilder.message, "Hello from Citrus!");
        assertEquals(endpointBuilder.number, 1);
        assertEquals(endpointBuilder.person, person);
    }

    public static final class TestEndpointBuilder extends AbstractEndpointBuilder<Endpoint> {

        String message;
        Person person;
        int number = 0;

        Endpoint mockEndpoint = Mockito.mock(Endpoint.class);

        @Override
        protected Endpoint getEndpoint() {
            return mockEndpoint;
        }

        @Override
        public boolean supports(Class<?> endpointType) {
            return true;
        }

        public TestEndpointBuilder message(String message) {
            this.message = message;
            return this;
        }

        public TestEndpointBuilder number(int number) {
            this.number = number;
            return this;
        }

        public TestEndpointBuilder person(Person person) {
            this.person = person;
            return this;
        }

        private record Person(String name, int age) {
        }
    }
}
