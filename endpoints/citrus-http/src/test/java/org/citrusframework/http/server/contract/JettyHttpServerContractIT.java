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

package org.citrusframework.http.server.contract;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.citrusframework.http.interceptor.LoggingHandlerInterceptor;
import org.citrusframework.http.security.BasicAuthConstraint;
import org.citrusframework.http.security.SecurityHandlerFactory;
import org.citrusframework.http.security.User;
import org.citrusframework.http.server.AbstractCommonHttpServerBuilder;
import org.citrusframework.http.server.AbstractHttpServer;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.report.MessageListeners;

/**
 * Runs the shared HTTP server contract suite against the default Jetty based HTTP server.
 */
public class JettyHttpServerContractIT extends AbstractHttpServerContractIT {

    @Override
    protected AbstractHttpServer createServer(Consumer<AbstractCommonHttpServerBuilder<?, ?>> options) {
        HttpServerBuilder builder = new HttpServerBuilder();
        options.accept(builder);
        return builder.build();
    }

    @Override
    protected boolean isTraceAnsweredByServer() {
        return true;
    }

    @Override
    protected boolean isDuplicateSlashRejected() {
        return true;
    }

    @Override
    protected void configureAuthentication(AbstractHttpServer server) {
        SecurityHandlerFactory securityHandlerFactory = new SecurityHandlerFactory();
        securityHandlerFactory.setRealm(AUTH_REALM);
        securityHandlerFactory.setUsers(List.of(
                new User(AUTH_USER, AUTH_PASSWORD, new String[]{AUTH_ROLE}),
                new User(AUTH_USER_WITHOUT_ROLE, AUTH_PASSWORD_WITHOUT_ROLE, new String[0])));
        securityHandlerFactory.setConstraints(Map.of(SECURED_PATH, new BasicAuthConstraint(new String[]{AUTH_ROLE})));
        securityHandlerFactory.initialize();

        ((HttpServer) server).setSecurityHandler(securityHandlerFactory.getObject());
    }

    @Override
    protected void configureTracing(AbstractHttpServer server, MessageListeners messageListeners) {
        LoggingHandlerInterceptor loggingInterceptor = new LoggingHandlerInterceptor();
        loggingInterceptor.setMessageListener(messageListeners);

        server.getInterceptors().add(loggingInterceptor);
    }
}
