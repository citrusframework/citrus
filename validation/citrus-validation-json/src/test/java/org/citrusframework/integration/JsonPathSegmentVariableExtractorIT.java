package org.citrusframework.integration;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

public class JsonPathSegmentVariableExtractorIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.SPRING)
    public void JsonPathSegmentVariableExtractorIT() {}
}
