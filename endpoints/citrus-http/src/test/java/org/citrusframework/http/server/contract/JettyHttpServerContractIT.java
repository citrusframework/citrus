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

import java.util.function.Consumer;

import org.citrusframework.http.server.AbstractCommonHttpServerBuilder;
import org.citrusframework.http.server.AbstractHttpServer;
import org.citrusframework.http.server.HttpServerBuilder;

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
}
