/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.http.server.contract;

import java.util.function.Consumer;

import org.citrusframework.http.server.AbstractCommonHttpServerBuilder;
import org.citrusframework.http.server.AbstractHttpServer;
import org.citrusframework.http.server.vertx.VertxHttpServerBuilder;
import org.citrusframework.report.MessageListeners;

/**
 * Runs the shared Http server contract suite against the Vert.x based Http server.
 */
public class VertxHttpServerContractIT extends AbstractHttpServerContractIT {

    @Override
    protected AbstractHttpServer createServer(Consumer<AbstractCommonHttpServerBuilder<?, ?>> options) {
        VertxHttpServerBuilder builder = new VertxHttpServerBuilder();
        options.accept(builder);
        return builder.build();
    }

    @Override
    protected boolean isTraceAnsweredByServer() {
        return false;
    }

    @Override
    protected boolean isDuplicateSlashRejected() {
        return false;
    }

    @Override
    protected boolean isSupported(Feature feature) {
        return switch (feature) {
            case GZIP, AUTHENTICATION, TLS, TRACING, INTERCEPTORS -> false;
        };
    }

    @Override
    protected void configureAuthentication(AbstractHttpServer server) {
        // basic authentication is not supported yet
    }

    @Override
    protected void configureTracing(AbstractHttpServer server, MessageListeners messageListeners) {
        // tracing is not supported yet
    }
}
