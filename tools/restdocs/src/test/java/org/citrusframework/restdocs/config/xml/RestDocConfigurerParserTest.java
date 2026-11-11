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

import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.restdocs.http.CitrusRestDocConfigurer;
import org.citrusframework.restdocs.soap.CitrusRestDocSoapConfigurer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @since 2.6
 */
public class RestDocConfigurerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testConfigurerParser() {
        Map<String, CitrusRestDocConfigurer> configurers = beanDefinitionContext.getBeansOfType(CitrusRestDocConfigurer.class);
        Assertions.assertEquals(1, configurers.size());

        // 1st configurer
        CitrusRestDocConfigurer configurer = configurers.get("configurer1");
        Assertions.assertNotNull(configurer.getContextProvider());

        Map<String, CitrusRestDocSoapConfigurer> soapConfigurers = beanDefinitionContext.getBeansOfType(CitrusRestDocSoapConfigurer.class);
        Assertions.assertEquals(1, soapConfigurers.size());

        // 2nd configurer
        CitrusRestDocSoapConfigurer soapConfigurer = soapConfigurers.get("configurer2");
        Assertions.assertNotNull(soapConfigurer.getContextProvider());
    }
}
