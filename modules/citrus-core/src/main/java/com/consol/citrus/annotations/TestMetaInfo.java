package com.consol.citrus.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sudeep.r on 22/11/2016.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface TestMetaInfo {
    String description() default "";
    String requirementID() default "";
    String scenario() default "";
    String author() default "";
}