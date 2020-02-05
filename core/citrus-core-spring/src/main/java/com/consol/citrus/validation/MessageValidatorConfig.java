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
import com.consol.citrus.validation.script.*;
import com.consol.citrus.validation.text.*;
import com.consol.citrus.validation.xhtml.XhtmlMessageValidator;
import com.consol.citrus.validation.xhtml.XhtmlXpathMessageValidator;
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

    private final DefaultMessageHeaderValidator defaultMessageHeaderValidator = new DefaultMessageHeaderValidator();
    private final DomXmlMessageValidator defaultXmlMessageValidator = new DomXmlMessageValidator();
    private final XpathMessageValidator defaultXpathMessageValidator = new XpathMessageValidator();
    private final JsonTextMessageValidator defaultJsonMessageValidator = new JsonTextMessageValidator();
    private final JsonPathMessageValidator defaultJsonPathMessageValidator = new JsonPathMessageValidator();
    private final PlainTextMessageValidator defaultPlaintextMessageValidator = new PlainTextMessageValidator();
    private final BinaryMessageValidator defaultBinaryMessageValidator = new BinaryMessageValidator();
    private final BinaryBase64MessageValidator defaultBinaryBase64MessageValidator = new BinaryBase64MessageValidator();
    private final GzipBinaryBase64MessageValidator defaultGzipBinaryBase64MessageValidator = new GzipBinaryBase64MessageValidator();

    private final XhtmlMessageValidator defaultXhtmlMessageValidator = new XhtmlMessageValidator();
    private final XhtmlXpathMessageValidator defaultXhtmlXpathMessageValidator = new XhtmlXpathMessageValidator();

    private final GroovyXmlMessageValidator defaultGroovyXmlMessageValidator = new GroovyXmlMessageValidator();
    private final GroovyJsonMessageValidator defaultGroovyJsonMessageValidator = new GroovyJsonMessageValidator();
    private final GroovyScriptMessageValidator defaultGroovyTextMessageValidator = new GroovyScriptMessageValidator();

    @Bean(name = "defaultXmlMessageValidator")
    public DomXmlMessageValidator getDefaultXmlMessageValidator() {
        return defaultXmlMessageValidator;
    }

    @Bean(name = "defaultMessageHeaderValidator")
    public DefaultMessageHeaderValidator getDefaultMessageHeaderValidator() {
        return defaultMessageHeaderValidator;
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

    @Bean(name = "defaultBinaryMessageValidator")
    public BinaryMessageValidator getDefaultBinaryMessageValidator() {
        return defaultBinaryMessageValidator;
    }

    @Bean(name = "defaultBinaryBase64MessageValidator")
    public BinaryBase64MessageValidator getDefaultBinaryBase64MessageValidator() {
        return defaultBinaryBase64MessageValidator;
    }

    @Bean(name = "defaultGzipBinaryBase64MessageValidator")
    public GzipBinaryBase64MessageValidator getDefaultGzipBinaryBase64MessageValidator() {
        return defaultGzipBinaryBase64MessageValidator;
    }

    @Bean(name = "defaultXhtmlMessageValidator")
    public XhtmlMessageValidator getDefaultXhtmlMessageValidator() {
        return defaultXhtmlMessageValidator;
    }

    @Bean(name = "defaultXhtmlXpathMessageValidator")
    public XhtmlXpathMessageValidator getDefaultXhtmlXpathMessageValidator() {
        return defaultXhtmlXpathMessageValidator;
    }

    @Bean(name = "defaultGroovyXmlMessageValidator")
    public GroovyXmlMessageValidator getDefaultGroovyXmlMessageValidator() {
        return defaultGroovyXmlMessageValidator;
    }

    @Bean(name = "defaultGroovyJsonMessageValidator")
    public GroovyJsonMessageValidator getDefaultGroovyJsonMessageValidator() {
        return defaultGroovyJsonMessageValidator;
    }

    @Bean(name = "defaultGroovyTextMessageValidator")
    public GroovyScriptMessageValidator getDefaultGroovyTextMessageValidator() {
        return defaultGroovyTextMessageValidator;
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
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultMessageHeaderValidator);
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultBinaryMessageValidator);
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultBinaryBase64MessageValidator);
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultGzipBinaryBase64MessageValidator);

        citrusMessageValidatorRegistry.getMessageValidators().add(defaultGroovyJsonMessageValidator);
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultGroovyTextMessageValidator);
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultXhtmlMessageValidator);
        citrusMessageValidatorRegistry.getMessageValidators().add(defaultXhtmlXpathMessageValidator);

        return citrusMessageValidatorRegistry;
    }
}
