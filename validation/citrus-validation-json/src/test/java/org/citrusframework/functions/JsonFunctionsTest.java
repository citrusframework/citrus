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

import static org.citrusframework.functions.JsonFunctions.jsonPatch;
import static org.citrusframework.functions.JsonFunctions.jsonPath;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.List;

import org.citrusframework.functions.core.JsonPatchFunction.PatchOperation;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

public class JsonFunctionsTest extends AbstractTestNGUnitTest {

    @Test
    public void testJsonPath() {
        assertEquals(jsonPath("{\"text\": \"Some Text\"}", "$.text", context), "Some Text");
    }

    @Test
    public void testFunctionUtils() {
        context.setFunctionRegistry(new DefaultFunctionRegistry());
        assertEquals(FunctionUtils.resolveFunction("citrus:jsonPath('{\"message\": \"Hello Citrus!\"}', '$.message')", context), "Hello Citrus!");
    }

    @Test
    public void testJsonPatchSingleOperation() {
        String json = "{\"status\":\"Draft\"}";

        PatchOperation op = new PatchOperation();
        op.setOperation("replace");
        op.setPath("$.status");
        op.setValue("Ready");

        String result = jsonPatch(json, op, context);

        assertTrue(result.contains("\"status\":\"Ready\""),
            "Expected patched JSON to contain updated status");
        assertFalse(result.contains("\"status\":\"Draft\""),
            "Original status value should no longer be present");
    }

    @Test
    public void testJsonPatchMultipleOperations() {
        String json = "{ \"name\": \"Test\", \"status\": \"Draft\", \"items\": [\"a\", \"b\"] }";

        PatchOperation replaceStatus = new PatchOperation();
        replaceStatus.setOperation("replace");
        replaceStatus.setPath("$.status");
        replaceStatus.setValue("Ready");

        PatchOperation addItem = new PatchOperation();
        addItem.setOperation("add");
        addItem.setPath("$.items/-");
        addItem.setValue("c");

        String result = jsonPatch(json, List.of(replaceStatus, addItem), context);

        assertTrue(result.contains("\"status\":\"Ready\""),
            "Expected status to be updated to 'Ready'");
        assertTrue(result.contains("\"a\""),
            "Existing item 'a' should still be present");
        assertTrue(result.contains("\"b\""),
            "Existing item 'b' should still be present");
        assertTrue(result.contains("\"c\""),
            "New item 'c' should be added");
    }
}
