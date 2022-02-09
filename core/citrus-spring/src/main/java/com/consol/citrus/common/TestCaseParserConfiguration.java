package com.consol.citrus.common;


import org.springframework.beans.factory.xml.BeanDefinitionParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestCaseParserConfiguration {

    /**
     * The name of the bean for which to override the parser
     * @return the name
     */
    String name() default "";

    /**
     * The parser override
     * @return the parser
     */
    Class<? extends BeanDefinitionParser> parser() default BeanDefinitionParser.class;
}
