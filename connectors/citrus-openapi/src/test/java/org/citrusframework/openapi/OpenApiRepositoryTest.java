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

package org.citrusframework.openapi;

import java.util.List;

import org.citrusframework.openapi.validation.OpenApiValidationPolicy;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class OpenApiRepositoryTest {

    private static final String ROOT = "/root";

    @Test
    public void shouldInitializeOpenApiRepository() {
        OpenApiRepository openApiRepository = new OpenApiRepository();
        openApiRepository.setRootContextPath(ROOT);
        openApiRepository.setLocations(
            singletonList("org/citrusframework/openapi/petstore/petstore**.json"));
        openApiRepository.initialize();

        List<OpenApiSpecification> openApiSpecifications = openApiRepository.getOpenApiSpecifications();

        assertEquals(openApiRepository.getRootContextPath(), ROOT);
        assertNotNull(openApiSpecifications);
        assertEquals(openApiSpecifications.size(), 3);

        assertEquals(openApiSpecifications.get(0).getRootContextPath(), ROOT);
        assertEquals(openApiSpecifications.get(1).getRootContextPath(), ROOT);

        assertTrue(
            SampleOpenApiProcessor.processedSpecifications.contains(openApiSpecifications.get(0)));
        assertTrue(
            SampleOpenApiProcessor.processedSpecifications.contains(openApiSpecifications.get(1)));
        assertTrue(
            SampleOpenApiProcessor.processedSpecifications.contains(openApiSpecifications.get(2)));
    }

    @Test
    public void shouldInitializeFaultyOpenApiRepositoryByDefault() {
        OpenApiRepository openApiRepository = new OpenApiRepository();
        openApiRepository.setLocations(
            singletonList("org/citrusframework/openapi/faulty/faulty-ping-api.yaml"));
        openApiRepository.initialize();

        List<OpenApiSpecification> openApiSpecifications = openApiRepository.getOpenApiSpecifications();

        assertNotNull(openApiSpecifications);
        assertEquals(openApiSpecifications.size(), 1);

        assertNotNull(openApiSpecifications.get(0).getOpenApiDoc(null));
    }

    @Test
    public void shouldFailOnFaultyOpenApiRepositoryByStrictValidation() {
        OpenApiRepository openApiRepository = new OpenApiRepository();
        openApiRepository.setValidationPolicy(OpenApiValidationPolicy.STRICT);
        openApiRepository.setLocations(
            singletonList("org/citrusframework/openapi/faulty/faulty-ping-api.yaml"));

        assertThrows(openApiRepository::initialize);
    }

    @Test
    public void shouldSetAndProvideProperties() {
        // Given
        OpenApiRepository openApiRepository = new OpenApiRepository();

        // When
        openApiRepository.setResponseValidationEnabled(true);
        openApiRepository.setRequestValidationEnabled(true);
        openApiRepository.setRootContextPath("/root");
        openApiRepository.setLocations(List.of("l1", "l2"));

        // Then
        assertTrue(openApiRepository.isResponseValidationEnabled());
        assertTrue(openApiRepository.isRequestValidationEnabled());
        assertEquals(openApiRepository.getRootContextPath(), "/root");
        assertEquals(openApiRepository.getLocations(), List.of("l1", "l2"));

        // When
        openApiRepository.setResponseValidationEnabled(false);
        openApiRepository.setRequestValidationEnabled(false);
        openApiRepository.setRootContextPath("/otherRoot");
        openApiRepository.setLocations(List.of("l3", "l4"));

        // Then
        assertFalse(openApiRepository.isResponseValidationEnabled());
        assertFalse(openApiRepository.isRequestValidationEnabled());
        assertEquals(openApiRepository.getRootContextPath(), "/otherRoot");
        assertEquals(openApiRepository.getLocations(), List.of("l3", "l4"));
    }

}
