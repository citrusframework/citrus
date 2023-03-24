package org.citrusframework.endpoint;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusEndpointAnnotations;
import org.citrusframework.annotations.CitrusEndpointProperty;
import org.citrusframework.context.TestContextFactory;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
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

        Assert.assertEquals(injected, endpointBuilder.mockEndpoint);
        Assert.assertEquals(endpointBuilder.message, "Hello from Citrus!");
        Assert.assertEquals(endpointBuilder.number, 1);
        Assert.assertEquals(endpointBuilder.person, person);
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

        private static class Person {
            final String name;
            final int age;

            private Person(String name, int age) {
                this.name = name;
                this.age = age;
            }
        }
    }
}
