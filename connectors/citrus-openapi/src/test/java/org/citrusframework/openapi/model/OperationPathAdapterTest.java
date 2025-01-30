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

package org.citrusframework.openapi.model;

import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import org.citrusframework.openapi.util.OpenApiUtils;
import org.testng.annotations.Test;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;

public class OperationPathAdapterTest {

    @Test
    public void shouldReturnFormattedStringWhenToStringIsCalled() {
        // Given
        Oas30Operation oas30Operation = new Oas30Operation("get");
        oas30Operation.operationId = "operationId";

        OperationPathAdapter adapter = new OperationPathAdapter("/api/path", "/context/path", "/full/path", oas30Operation, oas30Operation.operationId);

        // When
        String expectedString = format("%s (%s)", OpenApiUtils.getMethodPath("GET", "/api/path"), "operationId");

        // Then
        assertEquals(adapter.toString(), expectedString);
    }
}
