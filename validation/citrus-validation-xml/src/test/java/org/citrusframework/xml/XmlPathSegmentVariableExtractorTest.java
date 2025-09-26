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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.variable.VariableExpressionSegmentMatcher;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

public class XmlPathSegmentVariableExtractorTest extends UnitTestSupport {

    private static final String XML_FIXTURE = """
        <?xml version="1.0" encoding="UTF-8"?>
        <person xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
          <name>Peter</name>
          <married>true</married>
          <wife>
            <name>Linda</name>
            <married>true</married>
            <pets xsi:nil="true"/>
          </wife>
          <children>
            <child>
              <name>Paul</name>
              <married>true</married>
              <pets xsi:nil="true"/>
            </child>
            <child>
              <name>Laura</name>
              <married>false</married>
              <pets xsi:nil="true"/>
            </child>
          </children>
          <pets xsi:nil="true"/>
        </person>
        """;

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
     */
    private VariableExpressionSegmentMatcher matchSegmentExpressionMatcher(String xpath) {
        String variableExpression = String.format("xpath(%s)", xpath);
        VariableExpressionSegmentMatcher matcher = new VariableExpressionSegmentMatcher(variableExpression);
        Assert.assertTrue(matcher.nextMatch());
        return matcher;
    }

    @Test
    public void testExtractNullFromXml() {

        String jsonPath = "//person/pets";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(jsonPath);

        assertThat(unitUnderTest.canExtract(context, XML_FIXTURE, matcher)).isTrue();
        assertThat(unitUnderTest.extractValue(context, XML_FIXTURE, matcher)).isEqualTo("");
    }
    @Test
    public void failsToExtractNonExistingPath() {

        String jsonPath = "//person/wife/sex";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(jsonPath);

        assertThatThrownBy(() -> unitUnderTest.extractValue(context, XML_FIXTURE, matcher))
            .isInstanceOf(CitrusRuntimeException.class)
            .extracting(Throwable::getMessage, STRING)
            .isEqualToIgnoringWhitespace("""
                Unable to extract value using expression 'xpath(//person/wife/sex)'
                Reason: No result for XPath expression: '//person/wife/sex'
                From object (java.lang.String):
                <person xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                    <name>Peter</name>
                    <married>true</married>
                    <wife>
                     <name>Linda</name>
                     <married>true</married>
                     <pets xsi:nil="true"/>
                    </wife>
                    <children>
                     <child>
                       <name>Paul</name>
                       <married>true</married>
                       <pets xsi:nil="true"/>
                     </child>
                     <child>
                       <name>Laura</name>
                       <married>false</married>
                       <pets xsi:nil="true"/>
                     </child>
                    </children>
                    <pets xsi:nil="true"/>
                </person>
                """);

    }
}
