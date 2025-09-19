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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.UUID;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.citrusframework.XmlValidationHelper;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.SegmentEvaluationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.util.IsXmlPredicate;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.variable.SegmentVariableExtractorRegistry;
import org.citrusframework.variable.VariableExpressionSegmentMatcher;
import org.citrusframework.xml.xpath.XPathExpressionResult;
import org.citrusframework.xml.xpath.XPathUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class XpathSegmentVariableExtractor extends
    SegmentVariableExtractorRegistry.AbstractSegmentVariableExtractor {

    @Override
    public boolean canExtract(TestContext testContext, Object object,
        VariableExpressionSegmentMatcher matcher) {
        return object == null || (object instanceof Document
            || (object instanceof String string && IsXmlPredicate.getInstance().test(string))
            && XPathUtils.isXPathExpression(matcher.getSegmentExpression()));
    }

    @Override
    public Object doExtractValue(TestContext testContext, Object object,
        VariableExpressionSegmentMatcher matcher)
        throws SegmentEvaluationException {
        try {
            return (object == null)
                ? null
                : extractXpath(testContext, object, matcher);
        } catch (Exception e) {
            throw new SegmentEvaluationException(e.getMessage(), renderObject(object));
        }
    }

    private Object extractXpath(TestContext testContext, Object xml,
        VariableExpressionSegmentMatcher matcher) {

        Document document = null;
        if (xml instanceof Document) {
            document = (Document) xml;
        } else if (xml instanceof String string) {
            String documentCacheKey = UUID.nameUUIDFromBytes(string.getBytes()).toString();
            document = (Document) testContext.getVariables().get(documentCacheKey);
            if (document == null) {
                document = XMLUtils.parseMessagePayload(string);
                testContext.setVariable(documentCacheKey, document);
            }
        }

        if (document == null) {
            throw new CitrusRuntimeException(
                String.format("Unable to extract xpath from object of type %s", xml.getClass()));
        }

        NamespaceContext namespaceContext = XmlValidationHelper.getNamespaceContextBuilder(
            testContext).buildContext(new DefaultMessage().setPayload(xml), Collections.emptyMap());
        return XPathUtils.evaluate(document, matcher.getSegmentExpression(), namespaceContext,
            XPathExpressionResult.STRING);
    }

    private static String renderObject(Object object) {
        if (object == null) {
            return "null";
        }
        return prettyXml(String.valueOf(object));
    }

    private static String prettyXml(String s) {
        try {
            var dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            var doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(s)));

            var xslt = """
                <xsl:stylesheet version="1.0"
                  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
                  <xsl:strip-space elements="*"/>
                  <xsl:template match="@*|node()">
                    <xsl:copy>
                      <xsl:apply-templates select="@*|node()"/>
                    </xsl:copy>
                  </xsl:template>
                </xsl:stylesheet>
                """;

            var tf = TransformerFactory.newInstance();
            var t = tf.newTransformer(
                new javax.xml.transform.stream.StreamSource(new StringReader(xslt)));

            try {
                t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            } catch (Exception ignored) {
                // fall through
            }

            var w = new StringWriter();
            t.transform(new DOMSource(doc), new StreamResult(w));
            return w.toString();
        } catch (Exception e) {
            return s; // fallback
        }
    }

}