package org.citrusframework.xml;

import org.citrusframework.XmlValidationHelper;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.util.IsXmlPredicate;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.variable.SegmentVariableExtractorRegistry;
import org.citrusframework.variable.VariableExpressionSegmentMatcher;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.citrusframework.xml.xpath.XPathExpressionResult;
import org.citrusframework.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;

import javax.xml.namespace.NamespaceContext;
import java.util.Collections;
import java.util.UUID;

/**
 * @author Thorsten Schlathoelter
 */
public class XpathSegmentVariableExtractor extends SegmentVariableExtractorRegistry.AbstractSegmentVariableExtractor {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XpathSegmentVariableExtractor.class);

    @Override
    public boolean canExtract(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher) {
        return object == null  || (object instanceof  Document
                || (object instanceof  String && IsXmlPredicate.getInstance().test((String)object))
                        && XPathUtils.isXPathExpression(matcher.getSegmentExpression()));
    }

    @Override
    public Object doExtractValue(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher) {
        return object == null ? null : extractXpath(testContext, object, matcher);
    }

    private Object extractXpath(TestContext testContext, Object xml, VariableExpressionSegmentMatcher matcher) {

        Document document = null;
        if (xml instanceof  Document) {
            document = (Document) xml;
        } else if (xml instanceof String) {
            String documentCacheKey = UUID.nameUUIDFromBytes(((String)xml).getBytes()).toString();
            document = (Document)testContext.getVariables().get(documentCacheKey);
            if (document == null) {
                document = XMLUtils.parseMessagePayload((String)xml);
                testContext.setVariable(documentCacheKey, document);
            }
        }

        if (document == null) {
            throw new CitrusRuntimeException(String.format("Unable to extract xpath from object of type %s", xml.getClass()));
        }

        NamespaceContext namespaceContext = XmlValidationHelper.getNamespaceContextBuilder(testContext).buildContext(new DefaultMessage().setPayload(xml), Collections.emptyMap());
        return XPathUtils.evaluate(document, matcher.getSegmentExpression(), namespaceContext, XPathExpressionResult.STRING);
    }
}
