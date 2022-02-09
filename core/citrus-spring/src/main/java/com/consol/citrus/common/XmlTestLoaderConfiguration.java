package com.consol.citrus.common;

import com.consol.citrus.config.handler.CitrusTestCaseNamespaceHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Xml test loader annotation used to configure parsers in {@link CitrusTestCaseNamespaceHandler}
 * 
 * @author T. Schlathoelter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XmlTestLoaderConfiguration {

    /**
     * Test case parser configurations to apply to {@link CitrusTestCaseNamespaceHandler}
     * @return
     */
    TestCaseParserConfiguration[] parserConfigurations() default {};
}
