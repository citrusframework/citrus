/*
 * Copyright the original author or authors.
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

import org.citrusframework.restdocs.http.CitrusRestDocConfigurer;
import org.citrusframework.restdocs.http.RestDocClientInterceptor;
import org.citrusframework.restdocs.soap.CitrusRestDocSoapConfigurer;
import org.citrusframework.restdocs.soap.RestDocSoapClientInterceptor;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * @since 2.6
 */
public class RestDocDocumentationParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testConfigurerParser() {
        Map<String, CitrusRestDocConfigurer> configurers = beanDefinitionContext.getBeansOfType(CitrusRestDocConfigurer.class);
        Assertions.assertEquals(1, configurers.size());
        Map<String, RestDocClientInterceptor> interceptors = beanDefinitionContext.getBeansOfType(RestDocClientInterceptor.class);
        Assertions.assertEquals(1, interceptors.size());

        // 1st configurer
        CitrusRestDocConfigurer configurer = configurers.get("documentation1Configurer");
        Assertions.assertNotNull(configurer.getContextProvider());
        // 1st interceptor
        RestDocClientInterceptor interceptor = interceptors.get("documentation1Interceptor");
        Assertions.assertNotNull(interceptor.documentationGenerator());

        Map<String, CitrusRestDocSoapConfigurer> soapConfigurers = beanDefinitionContext.getBeansOfType(CitrusRestDocSoapConfigurer.class);
        Assertions.assertEquals(1, soapConfigurers.size());
        Map<String, RestDocSoapClientInterceptor> soapInterceptors = beanDefinitionContext.getBeansOfType(RestDocSoapClientInterceptor.class);
        Assertions.assertEquals(1, soapInterceptors.size());

        // 2nd configurer
        CitrusRestDocSoapConfigurer soapConfigurer = soapConfigurers.get("documentation2Configurer");
        Assertions.assertNotNull(soapConfigurer.getContextProvider());
        // 2nd interceptor
        RestDocSoapClientInterceptor soapInterceptor = soapInterceptors.get("documentation2Interceptor");
        Assertions.assertNotNull(soapInterceptor.documentationGenerator());

        List<ClientHttpRequestInterceptor> documentation1 = beanDefinitionContext.getBean("documentation1", List.class);
        Assertions.assertEquals(2L, documentation1.size());
        Assertions.assertEquals(configurer, documentation1.get(0));
        Assertions.assertEquals(interceptor, documentation1.get(1));

        List<ClientInterceptor> documentation2 = beanDefinitionContext.getBean("documentation2", List.class);
        Assertions.assertEquals(2L, documentation2.size());
        Assertions.assertEquals(soapConfigurer, documentation2.get(0));
        Assertions.assertEquals(soapInterceptor, documentation2.get(1));
    }
}
