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

package org.citrusframework.xml;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.variable.VariableExpressionSegmentMatcher;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.util.UUID;

public class XmlPathSegmentVariableExtractorTest extends UnitTestSupport {

    private static final String XML_FIXTURE = "<person><name>Peter</name></person>";

    private final XpathSegmentVariableExtractor unitUnderTest = new XpathSegmentVariableExtractor();

    @Test
    public void testExtractFromXml() {

        String xpath = "//person/name";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(xpath);

        Assert.assertTrue(unitUnderTest.canExtract(context, XML_FIXTURE, matcher));
        Assert.assertEquals(unitUnderTest.extractValue(context, XML_FIXTURE, matcher), "Peter");

        // Assert that xml document was cached
        Object cachedXmlDocument = context.getVariableObject(UUID.nameUUIDFromBytes(XML_FIXTURE.getBytes()).toString());
        Assert.assertTrue(cachedXmlDocument instanceof Document);

        // Assert that another match can be matched
        matcher = matchSegmentExpressionMatcher(xpath);
        Assert.assertTrue(unitUnderTest.canExtract(context, XML_FIXTURE, matcher));
        Assert.assertEquals(unitUnderTest.extractValue(context, XML_FIXTURE, matcher), "Peter");

        // Assert that a XML document can be matched
        matcher = matchSegmentExpressionMatcher(xpath);
        Assert.assertTrue(unitUnderTest.canExtract(context, cachedXmlDocument, matcher));
        Assert.assertEquals(unitUnderTest.extractValue(context, cachedXmlDocument, matcher), "Peter");

    }

    @Test
    public void testExtractFromInvalidXpathExpression() {

        String invalidXpathPath = "name";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(invalidXpathPath);

        Assert.assertFalse(unitUnderTest.canExtract(context, XML_FIXTURE, matcher));
    }

    @Test
    public void testExtractFromXmlExpressionFailure() {

        String invalidXpath = "//$$$";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(invalidXpath);

        Assert.assertTrue(unitUnderTest.canExtract(context, XML_FIXTURE, matcher));
        Assert.assertThrows(() -> unitUnderTest.extractValue(context, XML_FIXTURE, matcher));
    }

    /**
     * Create a variable expression xpath matcher and match the first xpath
     * @param xpath
     * @return
     */
    private VariableExpressionSegmentMatcher matchSegmentExpressionMatcher(String xpath) {
        String variableExpression = String.format("xpath(%s)", xpath);
        VariableExpressionSegmentMatcher matcher = new VariableExpressionSegmentMatcher(variableExpression);
        Assert.assertTrue(matcher.nextMatch());
        return matcher;
    }
}
