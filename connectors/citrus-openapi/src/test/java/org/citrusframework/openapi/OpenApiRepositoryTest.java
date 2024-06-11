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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import org.testng.annotations.Test;

@Test
public class OpenApiRepositoryTest {

    public static final String ROOT = "/root";

    public void initializeOpenApiRepository() {
        OpenApiRepository openApiRepository = new OpenApiRepository();
        openApiRepository.setRootContextPath(ROOT);
        openApiRepository.setLocations(List.of("org/citrusframework/openapi/petstore/petstore**.json"));
        openApiRepository.initialize();

        List<OpenApiSpecification> openApiSpecifications = openApiRepository.getOpenApiSpecifications();

        assertEquals(openApiRepository.getRootContextPath(), ROOT);
        assertNotNull(openApiSpecifications);
        assertEquals(openApiSpecifications.size(),3);

        assertEquals(openApiSpecifications.get(0).getRootContextPath(), ROOT);
        assertEquals(openApiSpecifications.get(1).getRootContextPath(), ROOT);

        assertTrue(SampleOpenApiProcessor.processedSpecifications.contains(openApiSpecifications.get(0)));
        assertTrue(SampleOpenApiProcessor.processedSpecifications.contains(openApiSpecifications.get(1)));
        assertTrue(SampleOpenApiProcessor.processedSpecifications.contains(openApiSpecifications.get(2)));
    }
}
