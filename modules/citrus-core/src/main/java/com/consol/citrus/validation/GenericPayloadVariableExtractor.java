package com.consol.citrus.validation;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;
import com.consol.citrus.validation.xml.XpathMessageValidationContext;
import com.consol.citrus.validation.xml.XpathPayloadVariableExtractor;
import com.consol.citrus.variable.VariableExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * Created by Simon Hofmann on 18.07.17.
 */

import java.util.HashMap;
import java.util.Map;

/**
 * Generic extractor implementation which reads messages via JSONPath or XPath
 *
 * @author Simon Hofmann
 * @since 2.7.3
 */
public class GenericPayloadVariableExtractor implements VariableExtractor {

    /** Map defines path expressions and target variable names */
    private Map<String, String> pathExpressions = new HashMap<>();

    private Map<String, String> namespaces = new HashMap<>();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(GenericPayloadVariableExtractor.class);

    @Override
    public void extractVariables(Message message, TestContext context) {
        if (CollectionUtils.isEmpty(pathExpressions)) {return;}

        if (log.isDebugEnabled()) {
            log.debug("Reading path elements.");
        }

        JsonPathVariableExtractor jsonPathVariableExtractor = new JsonPathVariableExtractor();
        XpathPayloadVariableExtractor xpathPayloadVariableExtractor = new XpathPayloadVariableExtractor();

        if(!this.namespaces.isEmpty()) {
            xpathPayloadVariableExtractor.setNamespaces(this.namespaces);
        }

        Map<String, String> jsonPath = new HashMap<>();
        Map<String, String> xPath = new HashMap<>();

        for(Map.Entry<String, String> pathExpression : pathExpressions.entrySet()) {
            final String path = context.replaceDynamicContentInString(pathExpression.getKey());
            final String variable = context.replaceDynamicContentInString(pathExpression.getValue());

            if (JsonPathMessageValidationContext.isJsonPathExpression(path)) {
                jsonPath.put(path, variable);
            } else {
                xPath.put(path, variable);
            }
        }

        try {
            if(!jsonPath.isEmpty()) {
                jsonPathVariableExtractor.setJsonPathExpressions(jsonPath);
                jsonPathVariableExtractor.extractVariables(message, context);
            }
            if(!xPath.isEmpty()) {
                xpathPayloadVariableExtractor.setXpathExpressions(xPath);
                xpathPayloadVariableExtractor.extractVariables(message, context);
            }
        } catch (CitrusRuntimeException e) {
            throw e;
        }
    }

    /**
     * Sets the JSONPath / XPath expressions.
     * @param pathExpressions
     */
    public void setPathExpressions(Map<String, String> pathExpressions) {
        this.pathExpressions = pathExpressions;
    }

    /**
     * Gets the JSONPath / XPath expressions.
     * @return
     */
    public Map<String, String> getPathExpressions() {
        return pathExpressions;
    }

    /**
     * Gets the XPath namespaces
     * @return the namespaces
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Sets the namespaces
     * @param namespaces the namespaces
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

}
