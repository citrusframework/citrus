package org.citrusframework.endpoint.direct.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citrusframework.annotations.CitrusEndpointConfig;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "direct.async")
public @interface DirectEndpointConfig {

    /**
     * Destination name.
     * @return
     */
    String queueName() default "";

    /**
     * Destination reference.
     * @return
     */
    String queue() default "";

    /**
     * Timeout.
     * @return
     */
    long timeout() default 5000L;

    /**
     * Test actor.
     * @return
     */
    String actor() default "";
}
