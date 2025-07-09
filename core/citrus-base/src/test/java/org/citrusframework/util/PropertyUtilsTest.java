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

package org.citrusframework.util;

import java.util.Properties;

import org.citrusframework.TestActor;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.log.LogModifier;
import org.citrusframework.message.DefaultMessageQueue;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PropertyUtilsTest extends UnitTestSupport {

    @Test
    public void testPropertyReplacementSingleProperty() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");

        String content = "This test has the name @test.name@!";

        String result = PropertyUtils.replacePropertiesInString(content, props);

        Assert.assertEquals(result, "This test has the name MyTest!");
    }

    @Test
    public void testPropertyReplacementStartingWithProperty() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");

        String content = "@test.name@ is the test's name!";

        String result = PropertyUtils.replacePropertiesInString(content, props);

        Assert.assertEquals(result, "MyTest is the test's name!");
    }

    @Test
    public void testPropertyReplacementMultipleProperties() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        props.put("test.author", "Mickey Mouse");

        String content = "This test has the name @test.name@ and its author is @test.author@";

        String result = PropertyUtils.replacePropertiesInString(content, props);

        Assert.assertEquals(result, "This test has the name MyTest and its author is Mickey Mouse");
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testPropertyReplacementUnknownProperty() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");

        String content = "This test has the name @test.name@ and its author is @test.author@";

        PropertyUtils.replacePropertiesInString(content, props);
    }

    @Test
    public void testPropertyMarkerEscaping() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        props.put("test.author", "Mickey Mouse (mail:mickey@mouse.de)");
        props.put("test.coauthor", "Donald Duck (mail:donald@duck.de)");

        String content = "This test has the name @test.name@ and its author is @test.author@ and @test.coauthor@";

        String result = PropertyUtils.replacePropertiesInString(content, props);

        Assert.assertEquals(result, "This test has the name MyTest and its author is Mickey Mouse (mail:mickey@mouse.de) and Donald Duck (mail:donald@duck.de)");
    }

    @Test
    public void testPropertyMarkerEscapingInContent() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        props.put("test.author", "Mickey Mouse");

        String content = "This \\@test\\@ has the name @test.name@ and its author is @test.author@";

        String result = PropertyUtils.replacePropertiesInString(content, props);

        Assert.assertEquals(result, "This @test@ has the name MyTest and its author is Mickey Mouse");
    }

    @Test
    public void testPropertyMarkerEscapingInDirectNeighbourhood() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        props.put("test.author", "Mickey Mouse");

        String content = "This \\@test\\@ has the name \\@@test.name@\\@ and its author is @test.author@";

        String result = PropertyUtils.replacePropertiesInString(content, props);

        Assert.assertEquals(result, "This @test@ has the name @MyTest@ and its author is Mickey Mouse");
    }

    @Test
    public void shouldBindEndpointConfigurationProperties() {
        System.setProperty("citrus.endpoint.config.foo.queueName", "fooQueue");
        System.setProperty("citrus.endpoint.config.foo.timeout", "100");

        DirectEndpoint endpoint = new DirectEndpoint();
        context.getReferenceResolver().bind("foo", endpoint);

        PropertyUtils.configure("foo", endpoint, context.getReferenceResolver());

        Assert.assertEquals(endpoint.getEndpointConfiguration().getQueueName(), "fooQueue");
        Assert.assertEquals(endpoint.getEndpointConfiguration().getTimeout(), 100L);
    }

    @Test
    public void shouldBindEndpointBeanReference() {
        System.setProperty("citrus.endpoint.bar.actor", "#bean:testActor");
        System.setProperty("citrus.endpoint.config.bar.queue", "#bean:fooQueue");

        DirectEndpoint endpoint = new DirectEndpoint();
        context.getReferenceResolver().bind("bar", endpoint);
        context.getReferenceResolver().bind("fooQueue", new DefaultMessageQueue("fooQueue"));
        context.getReferenceResolver().bind("testActor", new TestActor("testActor"));

        PropertyUtils.configure("bar", endpoint, context.getReferenceResolver());

        Assert.assertEquals(endpoint.getEndpointConfiguration().getQueue(), context.getReferenceResolver().resolve("fooQueue"));
        Assert.assertEquals(endpoint.getActor(), context.getReferenceResolver().resolve("testActor"));
    }

    @Test
    public void shouldBindProperties() {
        System.setProperty("citrus.component.foo.text", "Citrus rocks!");
        System.setProperty("citrus.component.foo.number", "1000");
        System.setProperty("citrus.component.foo.longNumber", "5000");
        FooComponent component = new FooComponent();
        context.getReferenceResolver().bind("foo", component);

        PropertyUtils.configure("foo", component, context.getReferenceResolver());

        Assert.assertEquals(component.getText(), "Citrus rocks!");
        Assert.assertEquals(component.getNumber(), 1000);
        Assert.assertEquals(component.getLongNumber(), 5000L);
        Assert.assertEquals(component.getConstant(), "unchanged");
    }

    @Test
    public void shouldBindBeanReference() {
        System.setProperty("citrus.component.myBean.logModifier", "#bean:modifier");

        FooComponent component = new FooComponent();
        context.getReferenceResolver().bind("myBean", component);
        context.getReferenceResolver().bind("modifier", context.getLogModifier());

        PropertyUtils.configure("myBean", component, context.getReferenceResolver());

        Assert.assertEquals(component.getLogModifier(), context.getLogModifier());
    }

    @Test
    public void shouldBindBeanReferenceDashStyleProperty() {
        System.setProperty("citrus.component.anotherBean.log-modifier", "#bean:modifier");
        System.setProperty("citrus.component.anotherBean.long-number", "1000");

        FooComponent component = new FooComponent();
        context.getReferenceResolver().bind("anotherBean", component);
        context.getReferenceResolver().bind("modifier", context.getLogModifier());

        PropertyUtils.configure("anotherBean", component, context.getReferenceResolver());

        Assert.assertEquals(component.getLogModifier(), context.getLogModifier());
        Assert.assertEquals(component.getLongNumber(), 1000L);
    }

    private static class FooComponent {

        private String text;
        private int number;
        private Long longNumber;
        private LogModifier logModifier;

        private String constant = "unchanged";

        public void setText(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public int getNumber() {
            return number;
        }

        public void setLongNumber(Long longNumber) {
            this.longNumber = longNumber;
        }

        public Long getLongNumber() {
            return longNumber;
        }

        public void setLogModifier(LogModifier logModifier) {
            this.logModifier = logModifier;
        }

        public LogModifier getLogModifier() {
            return logModifier;
        }

        public String getConstant() {
            return constant;
        }
    }
}
