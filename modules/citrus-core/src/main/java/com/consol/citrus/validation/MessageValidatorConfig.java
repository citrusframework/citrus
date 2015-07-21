/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.validation;

import com.consol.citrus.validation.json.JsonPathMessageValidator;
import com.consol.citrus.validation.json.JsonTextMessageValidator;
import com.consol.citrus.validation.script.GroovyJsonMessageValidator;
import com.consol.citrus.validation.script.GroovyXmlMessageValidator;
import com.consol.citrus.validation.text.PlainTextMessageValidator;
import com.consol.citrus.validation.xhtml.XhtmlMessageValidator;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XpathMessageValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Configuration
public class MessageValidatorConfig {

    private final DomXmlMessageValidator defaultXmlMessageValidator = new DomXmlMessageValidator();
    private final XpathMessageValidator defaultXpathMessageValidator = new XpathMessageValidator();
    private final JsonTextMessageValidator defaultJsonMessageValidator = new JsonTextMessageValidator();
    private final JsonPathMessageValidator defaultJsonPathMessageValidator = new JsonPathMessageValidator();
    private final PlainTextMessageValidator defaultPlaintextMessageValidator = new PlainTextMessageValidator();

    private final XhtmlMessageValidator defaultXhtmlMessageValidator = new XhtmlMessageValidator();

    private final GroovyXmlMessageValidator defaultGroovyXmlMessageValidator = new GroovyXmlMessageValidator();
    private final GroovyJsonMessageValidator defaultGroovyJsonMessageValidator = new GroovyJsonMessageValidator();

    @Bean(name = "defaultXmlMessageValidator")
    public DomXmlMessageValidator getDefaultXmlMessageValidator() {
        return defaultXmlMessageValidator;
    }

    @Bean(name = "defaultXpathMessageValidator")
    public XpathMessageValidator getDefaultXpathMessageValidator() {
        return defaultXpathMessageValidator;
    }

    @Bean(name = "defaultJsonMessageValidator")
    public JsonTextMessageValidator getDefaultJsonTextMessageValidator() {
        return defaultJsonMessageValidator;
    }

    @Bean(name = "defaultJsonPathMessageValidator")
    public JsonPathMessageValidator getDefaultJsonPathMessageValidator() {
        return defaultJsonPathMessageValidator;
    }

    @Bean(name = "defaultPlaintextMessageValidator")
    public PlainTextMessageValidator getDefaultPlainTextMessageValidator() {
        return defaultPlaintextMessageValidator;
    }

    @Bean(name = "defaultXhtmlMessageValidator")
    public XhtmlMessageValidator getDefaultXhtmlMessageValidator() {
        return defaultXhtmlMessageValidator;
    }

    @Bean(name = "defaultGroovyXmlMessageValidator")
    public GroovyXmlMessageValidator getDefaultGroovyXmlMessageValidator() {
        return defaultGroovyXmlMessageValidator;
    }

    @Bean(name = "defaultGroovyJsonMessageValidator")
    public GroovyJsonMessageValidator getDefaultGroovyJsonMessageValidator() {
        return defaultGroovyJsonMessageValidator;
    }

    @Bean(name = MessageValidatorRegistry.BEAN_NAME)
    public MessageValidatorRegistry getMessageValidatorRegistry() {
        MessageValidatorRegistry citrusMessageValidatorRegistry = new MessageValidatorRegistry();

        citrusMessageValidatorRegistry.getMessageValidators().add(defaultXmlMessageValidator);
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultXpathMessageValidator);
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultGroovyXmlMessageValidator);
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultJsonMessageValidator);
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultJsonPathMessageValidator);
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultPlaintextMessageValidator);

        citrusMessageValidatorRegistry.getMessageValidators().add(defaultGroovyJsonMessageValidator);
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultXhtmlMessageValidator);

        return citrusMessageValidatorRegistry;
    }
}
