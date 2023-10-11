/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.config.xml;

import org.citrusframework.container.Iterate;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for assert action in test case.
 *
 * @author Christoph Deppisch
 */
public class IterateParser extends AbstractIterationTestActionParser {

    @Override
	public BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(IterateFactoryBean.class);

        String start = element.getAttribute("start");
        if (StringUtils.hasText(start)) {
            builder.addPropertyValue("start", Integer.valueOf(start));
        }

        String step = element.getAttribute("step");
        if (StringUtils.hasText(step)) {
            builder.addPropertyValue("step", Integer.valueOf(step));
        }

        return builder;
    }

    /**
     * Test action factory bean.
     */
    public static class IterateFactoryBean extends AbstractIteratingTestContainerFactoryBean<Iterate, Iterate.Builder> {

        private final Iterate.Builder builder = new Iterate.Builder();

        /**
         * Step to increment.
         * @param step the step to set
         */
        public void setStep(int step) {
            builder.step(step);
        }

        @Override
        public Iterate getObject() throws Exception {
            return getObject(builder.build());
        }

        @Override
        public Class<?> getObjectType() {
            return Iterate.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public Iterate.Builder getBuilder() {
            return builder;
        }
    }
}
