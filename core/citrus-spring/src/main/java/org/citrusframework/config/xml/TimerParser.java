/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.container.Timer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parses the Timer container bean definition.
 *
 * @author Martin Maher
 * @since 2.5
 */
public class TimerParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        // create new bean builder
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TimerFactoryBean.class);

        // see if there is a description
        DescriptionElementParser.doParse(element, builder);

        // add the local name of this element as the name
        builder.addPropertyValue("name", element.getLocalName());

        // set condition, which is mandatory
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("id"), "timerId");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("interval"), "interval");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("delay"), "delay");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("repeatCount"), "repeatCount");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("fork"), "fork");

        // get all internal actions
        ActionContainerParser.doParse(element, parserContext, builder);

        // finally return the complete builder with its bean definition
        return builder.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class TimerFactoryBean extends AbstractTestContainerFactoryBean<Timer, Timer.Builder> {

        private final Timer.Builder builder = new Timer.Builder();

        public void setInterval(long interval) {
            builder.interval(interval);
        }

        public void setDelay(long delay) {
            builder.delay(delay);
        }

        public void setRepeatCount(int repeatCount) {
            builder.repeatCount(repeatCount);
        }

        public void setTimerId(String timerId) {
            builder.timerId(timerId);
        }

        public void setFork(boolean fork) {
            builder.fork(fork);
        }

        @Override
        public Timer getObject() throws Exception {
            return getObject(builder.build());
        }

        @Override
        public Class<?> getObjectType() {
            return Timer.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public Timer.Builder getBuilder() {
            return builder;
        }
    }
}
