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

package com.consol.citrus.mail.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.mail.client.MailClient;
import com.consol.citrus.mail.client.MailClientBuilder;
import com.consol.citrus.mail.message.MailMessageConverter;
import com.consol.citrus.mail.model.MailMarshaller;
import org.springframework.util.StringUtils;

import java.util.Properties;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class MailClientConfigParser extends AbstractAnnotationConfigParser<MailClientConfig, MailClient> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public MailClientConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public MailClient parse(MailClientConfig annotation) {
        MailClientBuilder builder = new MailClientBuilder();

        builder.host(annotation.host());
        builder.port(annotation.port());

        builder.protocol(annotation.protocol());

        if (StringUtils.hasText(annotation.username())) {
            builder.username(annotation.username());
        }

        if (StringUtils.hasText(annotation.password())) {
            builder.password(annotation.password());
        }

        if (StringUtils.hasText(annotation.javaMailProperties())) {
            builder.javaMailProperties(getReferenceResolver().resolve(annotation.javaMailProperties(), Properties.class));
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(getReferenceResolver().resolve(annotation.messageConverter(), MailMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.marshaller())) {
            builder.marshaller(getReferenceResolver().resolve(annotation.marshaller(), MailMarshaller.class));
        }

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(getReferenceResolver().resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
