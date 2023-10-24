package org.citrusframework.integration.container;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2011-01-26
 */
public class TemplateVariablesIT extends TestNGCitrusSpringSupport {
    @Test
    @CitrusTestSource(type = TestLoader.SPRING)
    public void TemplateVariablesIT() {}
}
