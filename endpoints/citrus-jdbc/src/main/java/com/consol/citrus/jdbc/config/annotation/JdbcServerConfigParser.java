/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.jdbc.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AnnotationConfigParser;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.jdbc.server.JdbcServerBuilder;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.spi.ReferenceResolver;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcServerConfigParser implements AnnotationConfigParser<JdbcServerConfig, JdbcServer> {

    @Override
    public JdbcServer parse(JdbcServerConfig annotation, ReferenceResolver referenceResolver) {
        JdbcServerBuilder builder = new JdbcServerBuilder();

        builder.autoStart(annotation.autoStart());

        if (StringUtils.hasText(annotation.host())) {
            builder.host(annotation.host());
        }

        builder.port(annotation.port());

        builder.debugLogging(annotation.debugLogging());

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(referenceResolver.resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        if (StringUtils.hasText(annotation.databaseName())) {
            builder.databaseName(annotation.databaseName());
        }

        builder.autoConnect(annotation.autoConnect());
        builder.autoCreateStatement(annotation.autoCreateStatement());
        builder.autoTransactionHandling(annotation.autoTransactionHandling());

        builder.autoHandleQueries(annotation.autoHandleQueries());

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(referenceResolver.resolve(annotation.correlator(), MessageCorrelator.class));
        }

        builder.maxConnections(annotation.maxConnections());

        builder.pollingInterval(annotation.pollingInterval());

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
