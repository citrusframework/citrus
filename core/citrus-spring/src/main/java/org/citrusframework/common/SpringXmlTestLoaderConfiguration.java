package org.citrusframework.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Xml test loader annotation used to configure parsers in {@link org.citrusframework.config.handler.CitrusTestCaseNamespaceHandler}
 *
 * @author T. Schlathoelter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SpringXmlTestLoaderConfiguration {

    /**
     * Test case parser configurations to apply to {@link org.citrusframework.config.handler.CitrusTestCaseNamespaceHandler}
     * @return
     */
    BeanDefinitionParserConfiguration[] parserConfigurations() default {};
}
