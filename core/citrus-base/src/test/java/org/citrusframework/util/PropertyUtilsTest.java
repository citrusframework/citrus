/*
 * Copyright 2006-2010 the original author or authors.
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

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class PropertyUtilsTest extends UnitTestSupport {
    @Test
    public void testPropertyReplacementSingleProperty() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");

        String content = "This test has the name @test.name@!";

        String result = PropertyUtils.replacePropertiesInString(content, props);

        Assert.assertEquals("This test has the name MyTest!", result);
    }

    @Test
    public void testPropertyReplacementStartingWithProperty() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");

        String content = "@test.name@ is the test's name!";

        String result = PropertyUtils.replacePropertiesInString(content, props);

        Assert.assertEquals("MyTest is the test's name!", result);
    }

    @Test
    public void testPropertyReplacementMultipleProperties() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        props.put("test.author", "Mickey Mouse");

        String content = "This test has the name @test.name@ and its author is @test.author@";

        String result = PropertyUtils.replacePropertiesInString(content, props);

        Assert.assertEquals("This test has the name MyTest and its author is Mickey Mouse", result);
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

        Assert.assertEquals("This test has the name MyTest and its author is Mickey Mouse (mail:mickey@mouse.de) and Donald Duck (mail:donald@duck.de)", result);
    }

    @Test
    public void testPropertyMarkerEscapingInContent() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        props.put("test.author", "Mickey Mouse");

        String content = "This \\@test\\@ has the name @test.name@ and its author is @test.author@";

        String result = PropertyUtils.replacePropertiesInString(content, props);

        Assert.assertEquals("This @test@ has the name MyTest and its author is Mickey Mouse", result);
    }

    @Test
    public void testPropertyMarkerEscapingInDirectNeighbourhood() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        props.put("test.author", "Mickey Mouse");

        String content = "This \\@test\\@ has the name \\@@test.name@\\@ and its author is @test.author@";

        String result = PropertyUtils.replacePropertiesInString(content, props);

        Assert.assertEquals("This @test@ has the name @MyTest@ and its author is Mickey Mouse", result);
    }
}
