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

package org.citrusframework.mail.config.annotation;

import java.util.Properties;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.mail.client.MailClient;
import org.citrusframework.mail.client.MailClientBuilder;
import org.citrusframework.mail.message.MailMessageConverter;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class MailClientConfigParser implements AnnotationConfigParser<MailClientConfig, MailClient> {

    @Override
    public MailClient parse(MailClientConfig annotation, ReferenceResolver referenceResolver) {
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
            builder.javaMailProperties(referenceResolver.resolve(annotation.javaMailProperties(), Properties.class));
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), MailMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.marshaller())) {
            builder.marshaller(referenceResolver.resolve(annotation.marshaller(), MailMarshaller.class));
        }

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
