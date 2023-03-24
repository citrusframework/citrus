package org.citrusframework.common;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.xml.BeanDefinitionParser;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BeanDefinitionParserConfiguration {

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
