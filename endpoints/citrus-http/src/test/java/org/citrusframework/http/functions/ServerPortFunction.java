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

package org.citrusframework.http.functions;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.http.server.AbstractHttpServer;
import org.citrusframework.http.server.vertx.VertxHttpServer;

/**
 * Resolves the port a Http server is listening on. Servers that are created with port
 * {@code 0} are bound to a free port by the operating system. This function returns the
 * port that is actually bound, so that a test may use a dynamic port for the server and
 * still reference it in a request URL.
 */
public class ServerPortFunction implements ParameterizedFunction<ServerPortFunction.Parameters> {

    @Override
    public String execute(Parameters params, TestContext context) {
        AbstractHttpServer server = context.getReferenceResolver().resolve(params.getServerName(), AbstractHttpServer.class);

        if (server instanceof VertxHttpServer vertxHttpServer) {
            return String.valueOf(vertxHttpServer.getActualPort());
        }

        return String.valueOf(server.getPort());
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements ParameterizedFunction.FunctionParameters {
        private String serverName;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.isEmpty()) {
                throw new InvalidFunctionUsageException("Function parameters must not be empty");
            }

            setServerName(parameterList.get(0));
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }
    }
}
