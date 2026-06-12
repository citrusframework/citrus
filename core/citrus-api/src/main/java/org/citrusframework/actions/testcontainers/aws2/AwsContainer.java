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

package org.citrusframework.actions.testcontainers.aws2;

import java.net.URI;

/**
 * Common interface for AWS service containers (LocalStack, Floci, etc.)
 */
public interface AwsContainer {

    /**
     * Get the service endpoint URI.
     * @return the service endpoint
     */
    URI getServiceEndpoint();

    /**
     * Get the configured AWS region.
     * @return the region
     */
    String getRegion();

    /**
     * Get the access key.
     * @return the access key
     */
    String getAccessKey();

    /**
     * Get the secret key.
     * @return the secret key
     */
    String getSecretKey();

}
