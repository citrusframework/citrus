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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class OasModelHelperTest {

    @Test
    public void testToAcceptedRandomizableMediaTypes_WithValidAcceptHeader() {
        // Given
        String accept = "application/json, text/plain, application/xml, application/json;charset=UTF-8";

        // When
        List<String> result = OasModelHelper.toAcceptedRandomizableMediaTypes(accept);

        // Then
        Assert.assertNotNull(result, "Result should not be null");
        Assert.assertTrue(result.contains("application/json"), "Result should contain 'application/json'");
        Assert.assertTrue(result.contains("application/json;charset=UTF-8"), "Result should contain 'text/plain'");
        Assert.assertTrue(result.contains("text/plain"), "Result should contain 'text/plain'");
        Assert.assertFalse(result.contains("application/xml"), "Result should not contain 'application/xml'");
    }

    @Test
    public void testToAcceptedRandomizableMediaTypes_WithEmptyAcceptHeader() {
        // Given
        String accept = "";

        // When
        List<String> result = OasModelHelper.toAcceptedRandomizableMediaTypes(accept);

        // Then
        Assert.assertNotNull(result, "Result should not be null");
        Assert.assertEquals(result, OasModelHelper.DEFAULT_ACCEPTED_MEDIA_TYPES, "Should return the default media types");
    }

    @Test
    public void testToAcceptedRandomizableMediaTypes_WithNullAcceptHeader() {
        // Given
        String accept = null;

        // When
        List<String> result = OasModelHelper.toAcceptedRandomizableMediaTypes(accept);

        // Then
        Assert.assertNotNull(result, "Result should not be null");
        Assert.assertEquals(result, OasModelHelper.DEFAULT_ACCEPTED_MEDIA_TYPES, "Should return the default media types");
    }

    @Test
    public void testToAcceptedRandomizableMediaTypes_WithUnrelatedMediaTypes() {
        // Given
        String accept = "image/png, application/xml";

        // When
        List<String> result = OasModelHelper.toAcceptedRandomizableMediaTypes(accept);

        // Then
        Assert.assertNotNull(result, "Result should not be null");
        Assert.assertTrue(result.isEmpty(), "Result should be empty for unrelated media types");
    }
}
