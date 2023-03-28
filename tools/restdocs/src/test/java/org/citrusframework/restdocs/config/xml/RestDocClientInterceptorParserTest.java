/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.restdocs.config.xml;

import org.citrusframework.restdocs.http.RestDocClientInterceptor;
import org.citrusframework.restdocs.soap.RestDocSoapClientInterceptor;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocClientInterceptorParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testClientInterceptorParser() {
        Map<String, RestDocClientInterceptor> interceptors = beanDefinitionContext.getBeansOfType(RestDocClientInterceptor.class);
        Assert.assertEquals(interceptors.size(), 1);

        // 1st interceptor
        RestDocClientInterceptor interceptor = interceptors.get("interceptor1");
        Assert.assertNotNull(interceptor.getDocumentationGenerator());

        Map<String, RestDocSoapClientInterceptor> soapInterceptors = beanDefinitionContext.getBeansOfType(RestDocSoapClientInterceptor.class);
        Assert.assertEquals(soapInterceptors.size(), 1);

        // 2nd interceptor
        RestDocSoapClientInterceptor soapInterceptor = soapInterceptors.get("interceptor2");
        Assert.assertNotNull(soapInterceptor.getDocumentationGenerator());
    }
}
