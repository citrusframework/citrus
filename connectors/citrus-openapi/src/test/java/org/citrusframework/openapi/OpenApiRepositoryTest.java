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

import org.citrusframework.openapi.validation.OpenApiValidationPolicy;
import org.citrusframework.spi.Resources;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class OpenApiRepositoryTest {

    private static final String ROOT = "/root";

    private OpenApiRepository fixture;

    @BeforeMethod
    void beforeMethodSetup() {
        fixture = new OpenApiRepository();
    }

    @Test
    public void initialize_shouldInitializeOpenApiRepository() {
        fixture.setRootContextPath(ROOT);
        fixture.setLocations(
                singletonList("org/citrusframework/openapi/petstore/petstore**.json"));
        fixture.initialize();

        List<OpenApiSpecification> openApiSpecifications = fixture.getOpenApiSpecifications();

        assertEquals(fixture.getRootContextPath(), ROOT);
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
    public void initialize_shouldInitializeFaultyOpenApiRepositoryByDefault() {
        fixture.setLocations(
                singletonList("org/citrusframework/openapi/faulty/faulty-ping-api.yaml"));
        fixture.initialize();

        List<OpenApiSpecification> openApiSpecifications = fixture.getOpenApiSpecifications();

        assertNotNull(openApiSpecifications);
        assertEquals(openApiSpecifications.size(), 1);

        assertNotNull(openApiSpecifications.get(0).getOpenApiDoc(null));
    }

    @Test
    public void initialize_shouldFailOnFaultyOpenApiRepositoryByStrictValidation() {
        fixture.setValidationPolicy(OpenApiValidationPolicy.STRICT);
        fixture.setLocations(
                singletonList("org/citrusframework/openapi/faulty/faulty-ping-api.yaml"));

        assertThrows(fixture::initialize);
    }

    @Test
    public void shouldSetAndProvideProperties() {
        // When
        fixture.setResponseValidationEnabled(true);
        fixture.setRequestValidationEnabled(true);
        fixture.setRootContextPath("/root");
        fixture.setLocations(List.of("l1", "l2"));

        // Then
        assertTrue(fixture.isResponseValidationEnabled());
        assertTrue(fixture.isRequestValidationEnabled());
        assertEquals(fixture.getRootContextPath(), "/root");
        assertEquals(fixture.getLocations(), List.of("l1", "l2"));

        // When
        fixture.setResponseValidationEnabled(false);
        fixture.setRequestValidationEnabled(false);
        fixture.setRootContextPath("/otherRoot");
        fixture.setLocations(List.of("l3", "l4"));

        // Then
        assertFalse(fixture.isResponseValidationEnabled());
        assertFalse(fixture.isRequestValidationEnabled());
        assertEquals(fixture.getRootContextPath(), "/otherRoot");
        assertEquals(fixture.getLocations(), List.of("l3", "l4"));
    }

    @Test
    public void addRepository_fromOpenApiResource_shouldBeIdempotent() {
        var pingApiResource = new Resources.ClasspathResource("org/citrusframework/openapi/ping/ping-api.yaml");

        // First invocation, new repository should be parsed and added to the list
        fixture.addRepository(pingApiResource);
        assertThatOpenApiSpecificationsContainsExactlyPingApi();

        // Second invocation, new repository should be parsed but *not* be added to the list (idempotency)
        fixture.addRepository(pingApiResource);
        assertThatOpenApiSpecificationsContainsExactlyPingApi();
    }

    private void assertThatOpenApiSpecificationsContainsExactlyPingApi() {
        assertThat(fixture.getOpenApiSpecifications())
                .hasSize(1)
                .satisfiesOnlyOnce(
                        openApiSpecification -> assertThat(openApiSpecification.getUid())
                                .isEqualTo("a55f22318ded6c120bf0e030d2dfcd969c9ede2c6101e3c9de7d9766d10e64f6")
                );
    }

    @Test
    public void addRepository_fromOpenApiSpecificationPojo_shouldBeIdempotent() {
        var openApiSpecificationMock1 = getOpenApiSpecificationWithFixedUid();

        // First invocation, new repository should be added to the list
        fixture.addRepository(openApiSpecificationMock1);
        assertThatOpenApiSpecificationsContainsExactly(openApiSpecificationMock1);

        var openApiSpecificationMock2 = getOpenApiSpecificationWithFixedUid();

        // Second invocation, new repository should *not* be added to the list (idempotency)
        fixture.addRepository(openApiSpecificationMock2);
        assertThatOpenApiSpecificationsContainsExactly(openApiSpecificationMock1);
    }

    private void assertThatOpenApiSpecificationsContainsExactly(OpenApiSpecification openApiSpecificationMock1) {
        assertThat(fixture.getOpenApiSpecifications())
                .hasSize(1)
                .containsExactly(openApiSpecificationMock1);
    }

    @Test
    public void withOpenApiSpecifications_shouldReplacePersistedOpenApiSpecifications() {
        var initialOpenApiSpecifications = fixture.getOpenApiSpecifications();
        assertThat(initialOpenApiSpecifications)
                .isNotNull();

        var openApiSpecificationMock = mock(OpenApiSpecification.class);
        var newOpenApiSpecifications = singletonList(openApiSpecificationMock);

        var updatedOpenApiRepository = fixture.withOpenApiSpecifications(
                newOpenApiSpecifications
        );

        assertThat(updatedOpenApiRepository)
                .isSameAs(fixture);

        assertThat(fixture.getOpenApiSpecifications())
                .isNotNull()
                // Assert that OpenAPI specifications were replaced
                .isNotSameAs(initialOpenApiSpecifications)
                // Assert that given list has been synchronized
                .isNotSameAs(newOpenApiSpecifications)
                .containsExactly(
                        openApiSpecificationMock
                );
    }

    @Test
    public void withOpenApiSpecifications_shouldExecuteReplacementWithoutInterruption() throws InterruptedException {
        var take = new AtomicBoolean(true);

        var singleThreadExecutor = Executors.newSingleThreadExecutor();
        try {
            var assertionTask = runAsync(() -> assertThatFixtureDoesNeverContainNullishOpenApiSpecifications(take), singleThreadExecutor);

            replaceOpenApiSpecificationsWithNewList(take, singleThreadExecutor);

            take.set(false);
            assertThat(assertionTask)
                    .isCompleted();
        } finally {
            singleThreadExecutor.shutdown();
        }
    }

    private void replaceOpenApiSpecificationsWithNewList(AtomicBoolean take, ExecutorService singleThreadExecutor) throws InterruptedException {
        sleep(100);

        fixture.withOpenApiSpecifications(
                singletonList(mock(OpenApiSpecification.class))
        );

        sleep(100);
    }

    private void assertThatFixtureDoesNeverContainNullishOpenApiSpecifications(AtomicBoolean take) {
        while (take.get()) {
            assertThat(fixture.getOpenApiSpecifications())
                    .isNotNull();
        }
    }

    @Test
    void contains_shouldReturnFalse_whenNoOpenApiSpecificationsHaveBeenRegistered() {
        assertThat(fixture.contains(getOpenApiSpecificationWithFixedUid()))
                .isFalse();
    }

    @Test
    void contains_shouldReturnFalse_whenNoOpenApiSpecificationsMatchesUid() {
        var openApiSpecification = getOpenApiSpecificationWithFixedUid();
        fixture.addRepository(openApiSpecification);

        var otherOpenApiSpecification = mock(OpenApiSpecification.class);
        doReturn("anotherUuid").when(otherOpenApiSpecification).getUid();

        assertThat(fixture.contains(otherOpenApiSpecification))
                .isFalse();
    }

    @Test
    void contains_shouldReturnFalse_whenRegisteredOpenApiHasNoUid() {
        var openApiSpecification = mock(OpenApiSpecification.class);
        doReturn(null).when(openApiSpecification).getUid();

        fixture.addRepository(openApiSpecification);

        var otherOpenApiSpecification = getOpenApiSpecificationWithFixedUid();
        assertThat(fixture.contains(otherOpenApiSpecification))
                .isFalse();
    }

    @Test
    void contains_shouldReturnFalse_whenComparingOpenApiHasNoUid() {
        var openApiSpecification = getOpenApiSpecificationWithFixedUid();
        fixture.addRepository(openApiSpecification);

        var otherOpenApiSpecification = mock(OpenApiSpecification.class);
        doReturn(null).when(otherOpenApiSpecification).getUid();

        assertThat(fixture.contains(otherOpenApiSpecification))
                .isFalse();
    }

    @Test
    void contains_detectsExistingOpenApiSpecification() {
        var openApiSpecification = getOpenApiSpecificationWithFixedUid();

        fixture.addRepository(openApiSpecification);

        // Comparison with the same object
        assertThat(fixture.contains(openApiSpecification))
                .isTrue();

        // Comparison with a new object
        assertThat(fixture.contains(getOpenApiSpecificationWithFixedUid()))
                .isTrue();
    }

    private static OpenApiSpecification getOpenApiSpecificationWithFixedUid() {
        var openApiSpecificationMock = mock(OpenApiSpecification.class);
        doReturn("randomUuid").when(openApiSpecificationMock).getUid();
        return openApiSpecificationMock;
    }
}
