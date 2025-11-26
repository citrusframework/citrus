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

package org.citrusframework.functions;

import java.util.Arrays;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.functions.core.JsonPatchFunction;
import org.citrusframework.functions.core.JsonPatchFunction.Parameters;
import org.citrusframework.functions.core.JsonPatchFunction.PatchOperation;
import org.citrusframework.functions.core.JsonPathFunction;

public final class JsonFunctions {

    /**
     * Prevent instantiation.
     */
    private JsonFunctions() {
    }

    /**
     * Runs Json path function with arguments.
     */
    public static String jsonPath(String content, String expression, TestContext context) {
        return new JsonPathFunction().execute(Arrays.asList(content, expression), context);
    }


    /**
     * Runs JSON patch function with a single operation.
     * Convenience overload delegating to the multi-operation variant.
     */
    public static String jsonPatch(String content, PatchOperation operation, TestContext context) {
        return jsonPatch(content, List.of(operation), context);
    }

    /**
     * Runs JSON patch function with multiple operations.
     * Operations are applied in the given order.
     */
    public static String jsonPatch(String content,
        List<PatchOperation> operations,
        TestContext context) {
        Parameters parameters = new Parameters();
        parameters.setSource(content);
        parameters.setOperations(operations);
        return new JsonPatchFunction().execute(parameters, context);
    }
}
