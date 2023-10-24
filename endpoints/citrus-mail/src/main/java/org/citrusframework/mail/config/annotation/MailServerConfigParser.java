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

import java.util.Arrays;
import java.util.Properties;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.mail.message.MailMessageConverter;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.mail.server.MailServerBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class MailServerConfigParser implements AnnotationConfigParser<MailServerConfig, MailServer> {

    @Override
    public MailServer parse(MailServerConfig annotation, ReferenceResolver referenceResolver) {
        MailServerBuilder builder = new MailServerBuilder();

        builder.autoStart(annotation.autoStart());
        builder.timeout(annotation.timeout());
        builder.debugLogging(annotation.debugLogging());

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(referenceResolver.resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        builder.port(annotation.port());

        builder.authRequired(annotation.authRequired());
        builder.autoAccept(annotation.autoAccept());
        builder.splitMultipart(annotation.splitMultipart());

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), MailMessageConverter.class));
        }

        if (annotation.knownUsers().length > 0) {
            builder.knownUsers(Arrays.asList(annotation.knownUsers()));
        }

        if (StringUtils.hasText(annotation.marshaller())) {
            builder.marshaller(referenceResolver.resolve(annotation.marshaller(), MailMarshaller.class));
        }

        if (StringUtils.hasText(annotation.javaMailProperties())) {
            builder.javaMailProperties(referenceResolver.resolve(annotation.javaMailProperties(), Properties.class));
        }

        return builder.initialize().build();
    }
}
