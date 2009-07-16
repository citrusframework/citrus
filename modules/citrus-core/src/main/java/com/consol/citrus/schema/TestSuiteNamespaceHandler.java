package com.consol.citrus.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * NamespaceHandler registers all BeanDefinitionParser
 * for the top-level elements in custom Spring 2.0 schema.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 */
public class TestSuiteNamespaceHandler extends NamespaceHandlerSupport {

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
     */
    public void init() {
        registerBeanDefinitionParser("testcase", new TestCaseParser());
        registerBeanDefinitionParser("template", new TemplateParser());
    }
}
