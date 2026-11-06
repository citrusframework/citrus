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

package org.citrusframework.poc.mcp.quarkus;

import io.quarkiverse.mcp.server.McpMethod;
import io.quarkiverse.mcp.server.runtime.McpRequest;
import io.quarkiverse.mcp.server.runtime.McpRequestValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Named diagnostic variant for the whole-list gap (T6): denies
 * {@code resources/list} ONLY when {@code -Dpoc.denyResourceList=true} is passed to the
 * runner. Inert otherwise (all other launches unaffected).
 *
 * <p>This is NOT a whole-list solution: the validator returns confirm/deny with no
 * reply channel, so a denial suppresses normal dispatch and the framework emits its
 * own validation-error diagnostics. The IT records that exact wire behavior.
 */
@ApplicationScoped
public class DiagnosticListDenyValidator implements McpRequestValidator {

    @Override
    public Future<Boolean> validate(JsonObject message, McpRequest request, McpMethod method) {
        if (method == McpMethod.RESOURCES_LIST && Boolean.getBoolean("poc.denyResourceList")) {
            System.err.println("validator-diagnostic: denying " + method);
            return Future.succeededFuture(false);
        }
        return Future.succeededFuture(true);
    }
}
